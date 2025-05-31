package com.tempest.metric.durability;

import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.*;
import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class MetricFileWriterTest {

    private File tempDir;
    private MetricFileWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "metric_test_" + System.nanoTime());
        assertTrue(tempDir.mkdirs());
        writer = new MetricFileWriter(tempDir, 1024 * 1024, 2, 100);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (writer != null) writer.close();
        for (File file : Objects.requireNonNull(tempDir.listFiles())) file.delete();
        tempDir.delete();
    }

    @Test
    void testAppendAndFlush() throws Exception {
        MetricEvent e1 = new TestMetricEvent("type1", "id1", System.currentTimeMillis(), 1);
        MetricEvent e2 = new TestMetricEvent("type2", "id2", System.currentTimeMillis(), 2);

        writer.append(e1);
        writer.append(e2);
        Thread.sleep(200); // allow background flush

        MetricFileReader reader = new MetricFileReader(tempDir);
        List<MetricEvent> events = reader.readNextBatch(10);

        assertEquals(2, events.size());
        assertEquals("id1", events.get(0).getItemId());
        assertEquals("id2", events.get(1).getItemId());
    }

    @Test
    void testRotation() throws Exception {
        // very small file limit to force rotation
        MetricFileWriter tinyWriter = new MetricFileWriter(tempDir, 50, 1, 100);

        MetricEvent event = new TestMetricEvent("typeX", "idX", System.currentTimeMillis(), 9);
        tinyWriter.append(event);
        tinyWriter.append(event); // should rotate after second write
        tinyWriter.close();

        long count = Objects.requireNonNull(tempDir.listFiles((dir, name) -> name.endsWith(".log"))).length;
        assertTrue(count >= 2);
    }
}
