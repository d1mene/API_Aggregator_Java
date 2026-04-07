package d1mene.client;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WeatherAPIClient extends AbstractAPIClient {

    private static final String PROD_URL = "https://api.weatherapi.com/v1/current.json";
    private final String apiKey;

    public WeatherAPIClient(String apiKey) {
        super(PROD_URL);
        this.apiKey = apiKey;
    }

    WeatherAPIClient(String apiKey, String baseUrl) {
        super(baseUrl);
        this.apiKey = apiKey;
    }

    @Override
    public String getSourceName() { return "WeatherAPI"; }

    @Override
    public JsonObject fetchData(Map<String, String> params) throws IOException {
        Map<String, String> mutableParams = new HashMap<>(params);
        mutableParams.put("key", apiKey);
        return buildRequestAndExecute(mutableParams).getAsJsonObject();
    }
}
