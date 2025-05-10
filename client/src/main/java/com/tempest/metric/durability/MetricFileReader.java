package com.tempest.metric.durability;

import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;


public class MetricFileReader {
    private static final Logger logger = LoggerFactory.getLogger(MetricFileReader.class);
    private static final DecimalFormat FORMAT = new DecimalFormat("0000");

    private final File dir;
    private final File cursorFile;
    private int currentSegmentIndex;
    private long currentOffset;

    public MetricFileReader(File dir) {
        this.dir = dir;
        this.cursorFile = new File(dir, "read.cursor");
        loadCursor();
    }

    public List<MetricEvent> readNextBatch(int maxCount) {
        List<MetricEvent> batch = new ArrayList<>();
        File segment = new File(dir, FORMAT.format(currentSegmentIndex) + ".log");
        if (!segment.exists()) return batch;

        try (RandomAccessFile raf = new RandomAccessFile(segment, "r")) {
            raf.seek(currentOffset);
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(raf.getFD())))) {
                while (batch.size() < maxCount) {
                    try {
                        Object obj = ois.readObject();
                        if (obj instanceof MetricEvent) {
                            batch.add((MetricEvent) obj);
                        }
                        currentOffset = raf.getFilePointer();
                    } catch (EOFException eof) {
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("[MetricReader] Failed to read segment {}: {}", currentSegmentIndex, e.getMessage());
        }

        saveCursor();
        cleanupCompletedSegments();
        return batch;
    }

    private void loadCursor() {
        if (!cursorFile.exists()) {
            this.currentSegmentIndex = 0;
            this.currentOffset = 0;
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(cursorFile))) {
            String[] parts = reader.readLine().split(",");
            this.currentSegmentIndex = Integer.parseInt(parts[0]);
            this.currentOffset = Long.parseLong(parts[1]);
        } catch (IOException | NumberFormatException e) {
            logger.warn("[MetricReader] Failed to load cursor, defaulting to 0: {}", e.getMessage());
            this.currentSegmentIndex = 0;
            this.currentOffset = 0;
        }
    }

    private void saveCursor() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cursorFile))) {
            writer.write(currentSegmentIndex + "," + currentOffset);
        } catch (IOException e) {
            logger.warn("[MetricReader] Failed to save cursor: {}", e.getMessage());
        }
    }

    private void cleanupCompletedSegments() {
        File currentFile = new File(dir, FORMAT.format(currentSegmentIndex) + ".log");
        if (!currentFile.exists()) {
            return;
        }

        if (currentOffset >= currentFile.length()) {
            if (currentFile.delete()) {
                logger.info("[MetricReader] Deleted fully read segment: {}", currentFile.getName());
                currentSegmentIndex++;
                currentOffset = 0;
                saveCursor();
            } else {
                logger.warn("[MetricReader] Failed to delete segment: {}", currentFile.getName());
            }
        }
    }
}