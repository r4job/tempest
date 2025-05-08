package com.tempest.metric.io;

import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class MetricWriter implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(MetricWriter.class);
    private static final DecimalFormat FORMAT = new DecimalFormat("0000");

    private final File dir;
    private final int maxSegmentSizeBytes;
    private final int batchSize;
    private final long flushIntervalMs;

    private final List<MetricEvent> buffer;
    private final ScheduledExecutorService flusher;
    private final Object lock = new Object();

    private int segmentIndex;
    private File currentFile;
    private ObjectOutputStream out;

    public MetricWriter(File dir, int maxSegmentSizeBytes, int batchSize, long flushIntervalMs) throws IOException {
        this.dir = dir;
        this.maxSegmentSizeBytes = maxSegmentSizeBytes;
        this.batchSize = batchSize;
        this.flushIntervalMs = flushIntervalMs;
        this.buffer = new ArrayList<>(batchSize);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        this.segmentIndex = findLastSegmentIndex() + 1;
        openSegment(segmentIndex);

        this.flusher = Executors.newSingleThreadScheduledExecutor();
        this.flusher.scheduleAtFixedRate(this::flushIfNeeded, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS);
    }

    private int findLastSegmentIndex() {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".log"));
        int max = -1;
        if (files != null) {
            for (File f : files) {
                try {
                    String base = f.getName().replace(".log", "");
                    max = Math.max(max, Integer.parseInt(base));
                } catch (NumberFormatException ignored) {}
            }
        }
        return max;
    }

    private void openSegment(int index) throws IOException {
        currentFile = new File(dir, FORMAT.format(index) + ".log");
        boolean exists = currentFile.exists();
        FileOutputStream fos = new FileOutputStream(currentFile, true);
        this.out = exists ? new AppendingObjectOutputStream(fos) : new ObjectOutputStream(fos);
    }

    public void append(MetricEvent event) throws IOException {
        synchronized (lock) {
            buffer.add(event);
            if (buffer.size() >= batchSize || currentFile.length() >= maxSegmentSizeBytes) {
                flushBuffer();
                if (currentFile.length() >= maxSegmentSizeBytes) {
                    rotateSegment();
                }
            }
        }
    }

    private void flushIfNeeded() {
        synchronized (lock) {
            if (!buffer.isEmpty()) {
                try {
                    flushBuffer();
                } catch (IOException e) {
                    logger.error("[SegmentedDurableWriter] Flush failed: {}", e.getMessage());
                }
            }
        }
    }

    private void flushBuffer() throws IOException {
        for (MetricEvent event : buffer) {
            out.writeObject(event);
        }
        out.flush();
        buffer.clear();
    }

    private void rotateSegment() throws IOException {
        out.close();
        segmentIndex++;
        openSegment(segmentIndex);
    }

    @Override
    public void close() throws IOException {
        flusher.shutdown();
        synchronized (lock) {
            flushBuffer();
            out.close();
        }
    }

    private static class AppendingObjectOutputStream extends ObjectOutputStream {
        AppendingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }
    }
}