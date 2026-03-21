package d1mene.client;


import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Map;

public class OpenExchangeRatesClient implements APIClient {
    private static final String BASE_URL = "https://openexchangerates.org/api/latest.json";
    private final String API_KEY;

    public OpenExchangeRatesClient(String apiKey) {
        API_KEY = apiKey;
    }

    @Override
    public String getSourceName() {
        return "OpenExchangeRates";
    }

    @Override
    public JsonObject fetchData(Map<String, String> params) throws IOException {
        params.put("app_id", API_KEY);
        return buildRequestAndResponse(BASE_URL, params);
    }
}
