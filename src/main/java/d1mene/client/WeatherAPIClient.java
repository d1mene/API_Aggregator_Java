package d1mene.client;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;

public class WeatherAPIClient implements APIClient {
    private static final String BASE_URL = "https://api.weatherapi.com/v1/current.json";
    private final String API_KEY;

    public WeatherAPIClient(String apiKey) {
        API_KEY = apiKey;
    }

    @Override
    public String getSourceName() {
        return "WeatherAPI";
    }

    @Override
    public JsonObject fetchData(Map<String, String> params) throws IOException {
        params.put("key", API_KEY);
        return buildRequestAndResponse(BASE_URL, params);
    }
}
