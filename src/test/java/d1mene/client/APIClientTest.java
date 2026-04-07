package d1mene.client;

import com.google.gson.JsonObject;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class APIClientTest {

    private MockWebServer mockWebServer;
    private final APIClient defaultClient = new APIClient() {
        @Override
        public String getSourceName() {
            return "DefaultClient";
        }

        @Override
        public JsonObject fetchData(Map<String, String> params) throws IOException {
            return new JsonObject();
        }
    };

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void givenValidResponse_whenFetchData_thenReturnsJsonObject() throws IOException {
        String json = "[{\"id\":\"bitcoin\",\"symbol\":\"btc\",\"name\":\"Bitcoin\",\"current_price\":70000}]";
        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        CoinGeckoClient client = new CoinGeckoClient(mockWebServer.url("/").toString());
        JsonObject result = client.fetchData(Map.of("vs_currency", "usd", "ids", "bitcoin"));

        assertNotNull(result);
        assertTrue(result.has("data"));
    }

    @Test
    void givenServerError_whenFetchData_thenThrowsIOException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        CoinGeckoClient client = new CoinGeckoClient(mockWebServer.url("/").toString());

        assertThrows(IOException.class, () ->
                client.fetchData(Map.of("vs_currency", "usd", "ids", "bitcoin")));
    }

    @Test
    void givenRateLimitResponse_whenFetchData_thenThrowsIOException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));

        CoinGeckoClient client = new CoinGeckoClient(mockWebServer.url("/").toString());

        assertThrows(IOException.class, () ->
                client.fetchData(Map.of("vs_currency", "usd", "ids", "bitcoin")));
    }

    @Test
    void givenValidResponse_whenWeatherApiFetchData_thenReturnsJsonObject() throws IOException {
        String json = "{\"location\":{\"name\":\"Moscow\"},\"current\":{\"temp_c\":2.0}}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        WeatherAPIClient client = new WeatherAPIClient("test_key", mockWebServer.url("/").toString());
        JsonObject result = client.fetchData(Map.of("q", "Moscow"));

        assertNotNull(result);
        assertTrue(result.has("location"));
    }

    @Test
    void givenUnauthorized_whenFetchData_thenThrowsIOException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        WeatherAPIClient client = new WeatherAPIClient("test_key", mockWebServer.url("/").toString());

        assertThrows(IOException.class, () ->
                client.fetchData(Map.of("q", "Moscow")));
    }

    @Test
    void givenValidResponse_whenOpenExchangeRatesFetchData_thenReturnsJsonObject()
            throws IOException {
        String json = "{\"base\":\"USD\",\"timestamp\":1741564800,\"rates\":{\"EUR\":0.86,\"RUB\":82.8}}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        OpenExchangeRatesClient client = new OpenExchangeRatesClient(
                "test_key", mockWebServer.url("/").toString());
        JsonObject result = client.fetchData(Map.of("symbols", "EUR,RUB"));

        assertNotNull(result);
        assertTrue(result.has("rates"));
        assertEquals("USD", result.get("base").getAsString());
    }

    @Test
    void givenUnauthorized_whenOpenExchangeRatesFetchData_thenThrowsIOException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        OpenExchangeRatesClient client = new OpenExchangeRatesClient(
                "invalid_key", mockWebServer.url("/").toString());

        assertThrows(IOException.class, () ->
                client.fetchData(Map.of("symbols", "EUR,RUB")));
    }

    @Test
    void buildRequest_thenThrowInvalidURL() {
        APIClient badClient = new APIClient() {
            @Override
            public String getSourceName() { return "BadClient"; }
            @Override
            public JsonObject fetchData(Map<String, String> params) { return null; }
        };

        assertThrows(IOException.class, () ->
                badClient.buildRequestAndResponse("!!!://invalid-url", Map.of()));
    }

    @Test
    void buildRequest_thenThrowConnectionError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        assertThrows(IOException.class, () ->
                defaultClient.buildRequestAndResponse(mockWebServer.url("/").toString(), Map.of()));
    }

    @Test
    void buildRequest_thenEmptyResponseBody() {
        mockWebServer.enqueue(new MockResponse().setBody("").setResponseCode(200));

        IOException exception = assertThrows(IOException.class, () ->
                defaultClient.buildRequestAndResponse(mockWebServer
                        .url("/").toString(), Map.of()));

        assertTrue(exception.getMessage().contains("пустое тело ответа"));
    }


    @Test
    void givenInvalidUrl_whenBuildRequest_thenThrowIOException() {
        assertThrows(IOException.class, () ->
                defaultClient.buildRequestAndResponse("Невалидный URL", Map.of()));
    }

    @Test
    void givenServerError_whenBuildRequest_thenThrowConnectionError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        IOException exception = assertThrows(IOException.class, () ->
                defaultClient.buildRequestAndResponse(mockWebServer
                        .url("/").toString(), Map.of()));

        assertTrue(exception.getMessage().contains("HTTP 500"));
    }

    @Test
    void givenEmptyBody_whenBuildRequest_thenEmptyResponseBody() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(""));

        IOException exception = assertThrows(IOException.class, () ->
                defaultClient.buildRequestAndResponse(mockWebServer
                        .url("/").toString(), Map.of()));

        assertTrue(exception.getMessage().contains("пустое тело ответа"));
    }

    @Test
    void givenValidParamsAndResponse_whenBuildRequest_thenReturnsJsonObject() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"success\"}"));

        JsonObject result = defaultClient.buildRequestAndResponse(
                mockWebServer.url("/").toString(),
                Map.of("param1", "value1")
        );

        assertNotNull(result);
        assertTrue(result.has("status"));
        assertEquals("success", result.get("status").getAsString());
        assertEquals("/?param1=value1", mockWebServer.takeRequest().getPath());
    }
}