package com.tempest.aggregation.watcher;

import com.tempest.aggregation.model.ConsistentHashRing;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static io.etcd.jetcd.watch.WatchEvent.EventType.PUT;
import static io.etcd.jetcd.watch.WatchEvent.EventType.DELETE;
import static org.mockito.Mockito.*;

public class EtcdHashRingWatcherTest {

    private Client client;
    private Watch.Watcher watcher;
    private Watch watchClient;
    private ConsistentHashRing ring;
    private EtcdHashRingWatcher hashRingWatcher;

    @BeforeEach
    public void setup() {
        client = mock(Client.class);
        watchClient = mock(Watch.class);
        when(client.getWatchClient()).thenReturn(watchClient);
        ring = spy(new ConsistentHashRing(Collections.emptyList()));
        hashRingWatcher = new EtcdHashRingWatcher(client, "/nodes/", ring, 1);
    }

    @Test
    public void testPutEventUpdatesRing() {
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);

        hashRingWatcher.start();
        verify(watchClient).watch(any(ByteSequence.class), any(WatchOption.class), listenerCaptor.capture());

        Watch.Listener listener = listenerCaptor.getValue();

        WatchEvent putEvent = mock(WatchEvent.class);
        when(putEvent.getEventType()).thenReturn(PUT);
        when(putEvent.getKeyValue()).thenReturn(newKeyValue("/nodes/node-A"));

        WatchResponse response = mock(WatchResponse.class);
        when(response.getEvents()).thenReturn(List.of(putEvent));

        listener.onNext(response);
        verify(ring).setNodes(argThat(nodes -> nodes.contains("node-A")));
    }

    @Test
    public void testDeleteEventRemovesNode() {
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);

        hashRingWatcher.start();
        verify(watchClient).watch(any(ByteSequence.class), any(WatchOption.class), listenerCaptor.capture());

        Watch.Listener listener = listenerCaptor.getValue();

        // Add first
        WatchEvent putEvent = mock(WatchEvent.class);
        when(putEvent.getEventType()).thenReturn(PUT);
        when(putEvent.getKeyValue()).thenReturn(newKeyValue("/nodes/node-A"));
        WatchResponse putResponse = mock(WatchResponse.class);
        when(putResponse.getEvents()).thenReturn(List.of(putEvent));
        listener.onNext(putResponse);

        // Then delete
        WatchEvent deleteEvent = mock(WatchEvent.class);
        when(deleteEvent.getEventType()).thenReturn(DELETE);
        when(deleteEvent.getKeyValue()).thenReturn(newKeyValue("/nodes/node-A"));
        WatchResponse deleteResponse = mock(WatchResponse.class);
        when(deleteResponse.getEvents()).thenReturn(List.of(deleteEvent));
        listener.onNext(deleteResponse);

        verify(ring, times(2)).setNodes(any());
    }

    private KeyValue newKeyValue(String fullKey) {
        KeyValue kv = mock(KeyValue.class);
        when(kv.getKey()).thenReturn(ByteSequence.from(fullKey, StandardCharsets.UTF_8));
        return kv;
    }
}
