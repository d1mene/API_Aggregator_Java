package d1mene.service;

import d1mene.TestDataFactory;
import d1mene.client.APIClient;
import d1mene.storage.FileStorage;
import d1mene.storage.WriteMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PullingManagerTest {

    @Mock
    private APIClient mockClient;
    @Mock
    private FileStorage mockStorage;
    private PullingManager manager;

    @BeforeEach
    void setUp() {
        Map<String, Map<String, String>> params = Map.of("CoinGecko", Map.of());
        manager = new PullingManager(2, 1, mockStorage, params, WriteMode.APPEND);
    }

    @AfterEach
    void tearDown() {
        if (manager.isActive()) {
            manager.stop();
        }
    }

    @Test
    void givenManager_whenNotStarted_thenIsNotActive() {
        assertFalse(manager.isActive());
    }

    @Test
    void givenManager_whenStart_thenIsActive() {
        manager.start(List.of());
        assertTrue(manager.isActive());
    }

    @Test
    void givenStartedManager_whenStop_thenIsNotActive() {
        manager.start(List.of());
        manager.stop();
        assertFalse(manager.isActive());
    }

    @Test
    void givenStartedManager_whenWait_thenTaskExecuted()
            throws IOException, InterruptedException {
        when(mockClient.getSourceName()).thenReturn("CoinGecko");
        when(mockClient.fetchData(any())).thenReturn(TestDataFactory.coinGeckoWrapper());
        manager.start(List.of(mockClient));
        Thread.sleep(2000);
        manager.stop();

        verify(mockClient, atLeastOnce()).fetchData(any());
    }

    @Test
    void givenManagerWithInterval_whenStart_thenRespectsInterval()
            throws IOException, InterruptedException {
        when(mockClient.getSourceName()).thenReturn("CoinGecko");
        when(mockClient.fetchData(any())).thenReturn(TestDataFactory.coinGeckoWrapper());

        Map<String, Map<String, String>> params = Map.of("CoinGecko", Map.of());
        PullingManager slowManager = new PullingManager(1, 5, mockStorage, params, WriteMode.APPEND);
        slowManager.start(List.of(mockClient));
        Thread.sleep(2000);
        slowManager.stop();

        verify(mockClient, atMostOnce()).fetchData(any());
    }

    @Test
    void givenOnTaskFinished_whenCalled_thenManagerAcceptsNextTask()
            throws IOException, InterruptedException {
        when(mockClient.getSourceName()).thenReturn("CoinGecko");
        when(mockClient.fetchData(any())).thenReturn(TestDataFactory.coinGeckoWrapper());
        manager.start(List.of(mockClient));
        Thread.sleep(3000);
        manager.stop();

        verify(mockStorage, atLeastOnce()).save(any(), any());
    }
}