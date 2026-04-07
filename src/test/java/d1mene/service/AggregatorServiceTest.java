package d1mene.service;

import d1mene.TestDataFactory;
import d1mene.client.APIClient;
import d1mene.data.APIRecord;
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
class AggregatorServiceTest {

    @Mock
    private APIClient mockCoinGecko;

    @Mock
    private APIClient mockWeather;

    @Test
    void givenWorkingClient_whenAggregate_thenReturnsRecords() throws IOException {
        when(mockCoinGecko.getSourceName()).thenReturn("CoinGecko");
        when(mockCoinGecko.fetchData(any())).thenReturn(TestDataFactory.coinGeckoWrapper());

        Map<String, List<APIRecord>> result = new AggregatorService().aggregate(
                List.of(mockCoinGecko),
                Map.of("CoinGecko", Map.of("vs_currency", "usd"))
        );

        assertFalse(result.get("CoinGecko").isEmpty());
        verify(mockCoinGecko).fetchData(any());
    }

    @Test
    void givenFailingClient_whenAggregate_thenReturnsEmptyList() throws IOException {
        when(mockWeather.getSourceName()).thenReturn("WeatherAPI");
        when(mockWeather.fetchData(any())).thenThrow(new IOException("Connection failed"));

        Map<String, List<APIRecord>> result = new AggregatorService().aggregate(
                List.of(mockWeather),
                Map.of("WeatherAPI", Map.of())
        );

        assertTrue(result.get("WeatherAPI").isEmpty());
    }

    @Test
    void givenMultipleClients_whenAggregate_thenReturnsAllResults() throws IOException {
        when(mockCoinGecko.getSourceName()).thenReturn("CoinGecko");
        when(mockCoinGecko.fetchData(any())).thenReturn(TestDataFactory.coinGeckoWrapper());
        when(mockWeather.getSourceName()).thenReturn("WeatherAPI");
        when(mockWeather.fetchData(any())).thenReturn(TestDataFactory.weatherJson());

        Map<String, List<APIRecord>> result = new AggregatorService().aggregate(
                List.of(mockCoinGecko, mockWeather),
                Map.of("CoinGecko", Map.of(), "WeatherAPI", Map.of())
        );

        assertEquals(2, result.size());
        assertFalse(result.get("CoinGecko").isEmpty());
        assertFalse(result.get("WeatherAPI").isEmpty());
    }

    @Test
    void givenOneFailingOneWorking_whenAggregate_thenPartialResults() throws IOException {
        when(mockCoinGecko.getSourceName()).thenReturn("CoinGecko");
        when(mockCoinGecko.fetchData(any())).thenThrow(new IOException("timeout"));
        when(mockWeather.getSourceName()).thenReturn("WeatherAPI");
        when(mockWeather.fetchData(any())).thenReturn(TestDataFactory.weatherJson());

        Map<String, List<APIRecord>> result = new AggregatorService().aggregate(
                List.of(mockCoinGecko, mockWeather),
                Map.of("CoinGecko", Map.of(), "WeatherAPI", Map.of())
        );

        assertTrue(result.get("CoinGecko").isEmpty());
        assertFalse(result.get("WeatherAPI").isEmpty());
    }
}