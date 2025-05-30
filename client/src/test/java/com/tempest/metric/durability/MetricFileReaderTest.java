package com.tempest.metric.durability;

import com.tempest.metric.MetricEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class MetricFileReaderTest {

    private final File logDir = new File("/tmp/tmpg94o8gcd");

    @AfterEach
    void cleanUp() {
        for (File file : Objects.requireNonNull(logDir.listFiles())) {
            file.delete();
        }
        logDir.delete();
    }

    @Test
    void testReadNextBatchReadsSerializedEvents() {
        MetricFileReader reader = new MetricFileReader(logDir);
        List<MetricEvent> batch = reader.readNextBatch(10);

        assertEquals(3, batch.size());
        assertEquals("id1", batch.get(0).getItemId());
        assertEquals("id2", batch.get(1).getItemId());
        assertEquals("id3", batch.get(2).getItemId());
    }

    @Test
    void testReadNextBatchPartialLimit() {
        MetricFileReader reader = new MetricFileReader(logDir);
        List<MetricEvent> batch = reader.readNextBatch(2);

        assertEquals(2, batch.size());
        assertEquals("id1", batch.get(0).getItemId());
        assertEquals("id2", batch.get(1).getItemId());
    }

    @Test
    void testReadNextBatchCursorAdvancesAndDeletesSegment() {
        MetricFileReader reader = new MetricFileReader(logDir);

        // Read all events to trigger segment cleanup
        List<MetricEvent> batch = reader.readNextBatch(10);
        assertEquals(3, batch.size());

        File segmentFile = new File(logDir, "0000.log");
        assertFalse(segmentFile.exists(), "Segment file should be deleted after full read");
    }
}

