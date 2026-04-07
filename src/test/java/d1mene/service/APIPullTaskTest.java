package d1mene.service;

import d1mene.TestDataFactory;
import d1mene.client.APIClient;
import d1mene.storage.FileStorage;
import d1mene.storage.WriteMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class APIPullTaskTest {

    @Mock
    private APIClient mockClient;

    @Mock
    private FileStorage mockStorage;

    @Mock
    private PullingManager mockManager;

    @Test
    void givenWorkingClient_whenRun_thenSavesRecords() throws IOException {
        when(mockClient.getSourceName()).thenReturn("CoinGecko");
        when(mockClient.fetchData(any())).thenReturn(TestDataFactory.coinGeckoWrapper());

        APIPullTask task = new APIPullTask(
                mockClient, Map.of(), mockStorage, mockManager, WriteMode.APPEND);
        task.run();

        verify(mockStorage).save(any(), eq(WriteMode.APPEND));
        verify(mockManager).onTaskFinished("CoinGecko");
    }

    @Test
    void givenFailingClient_whenRun_thenOnTaskFinishedStillCalled() throws IOException {
        when(mockClient.getSourceName()).thenReturn("WeatherAPI");
        when(mockClient.fetchData(any())).thenThrow(new IOException("timeout"));

        APIPullTask task = new APIPullTask(
                mockClient, Map.of(), mockStorage, mockManager, WriteMode.APPEND);
        task.run();

        verify(mockStorage, never()).save(any(), any());
        verify(mockManager).onTaskFinished("WeatherAPI");
    }

    @Test
    void givenStorageFailure_whenRun_thenOnTaskFinishedStillCalled() throws IOException {
        when(mockClient.getSourceName()).thenReturn("OpenExchangeRates");
        when(mockClient.fetchData(any())).thenReturn(TestDataFactory.exchangeRatesJson());
        doThrow(new IOException("disk full")).when(mockStorage).save(any(), any());

        APIPullTask task = new APIPullTask(
                mockClient, Map.of(), mockStorage, mockManager, WriteMode.APPEND);
        task.run();

        verify(mockManager).onTaskFinished("OpenExchangeRates");
    }

    @Test
    void givenWeatherClient_whenRun_thenSavesWithCorrectWriteMode() throws IOException {
        when(mockClient.getSourceName()).thenReturn("WeatherAPI");
        when(mockClient.fetchData(any())).thenReturn(TestDataFactory.weatherJson());

        APIPullTask task = new APIPullTask(
                mockClient, Map.of("q", "Moscow"), mockStorage, mockManager, WriteMode.CREATE);
        task.run();

        verify(mockStorage).save(any(), eq(WriteMode.CREATE));
        verify(mockManager).onTaskFinished("WeatherAPI");
    }

    @Test
    void givenEmptyResult_whenRun_thenDoesNotSave() throws IOException {
        when(mockClient.getSourceName()).thenReturn("CoinGecko");

        com.google.gson.JsonObject emptyWrapper = new com.google.gson.JsonObject();
        emptyWrapper.add("data", new com.google.gson.JsonArray());
        when(mockClient.fetchData(any())).thenReturn(emptyWrapper);

        APIPullTask task = new APIPullTask(
                mockClient, Map.of(), mockStorage, mockManager, WriteMode.APPEND);
        task.run();

        verify(mockStorage, never()).save(any(), any());
        verify(mockManager).onTaskFinished("CoinGecko");
    }
}