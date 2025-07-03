package com.tempest.metric.durability;

import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileDurabilityStoreTest {

    private File tempDir;
    private FileDurabilityStore store;

    @BeforeAll
    void setupDir() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "durability_test_" + System.nanoTime());
        assertTrue(tempDir.mkdirs(), "Failed to create test directory");
    }

    @BeforeEach
    void setupStore() throws IOException {
        store = new FileDurabilityStore(tempDir, 1024 * 1024, 10, 100);
    }

    @AfterEach
    void cleanup() throws IOException {
        store.close();
        for (File file : tempDir.listFiles()) {
            assertTrue(file.delete(), "Failed to delete file: " + file.getName());
        }
    }

    @AfterAll
    void cleanupDir() {
        assertTrue(tempDir.delete(), "Failed to delete temp directory");
    }

    @Test
    void testAppendAndReadBack() throws IOException, InterruptedException {
        MetricEvent e1 = new TestMetricEvent("video", "item1", System.currentTimeMillis(), 5);
        MetricEvent e2 = new TestMetricEvent("video", "item2", System.currentTimeMillis(), 3);

        store.append(e1);
        store.append(e2);

        // wait for flush to occur
        Thread.sleep(150);

        List<MetricEvent> batch = store.readNextBatch(10);

        assertEquals(2, batch.size());
        assertTrue(batch.contains(e1));
        assertTrue(batch.contains(e2));
    }

    @Test
    void testCloseDoesNotThrow() {
        assertDoesNotThrow(() -> store.close());
    }
}

