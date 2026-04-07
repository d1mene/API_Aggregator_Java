package d1mene.client;


import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OpenExchangeRatesClient extends AbstractAPIClient {

    private static final String PROD_URL = "https://openexchangerates.org/api/latest.json";
    private final String apiKey;

    public OpenExchangeRatesClient(String apiKey) {
        super(PROD_URL);
        this.apiKey = apiKey;
    }

    OpenExchangeRatesClient(String apiKey, String baseUrl) {
        super(baseUrl);
        this.apiKey = apiKey;
    }

    @Override
    public String getSourceName() { return "OpenExchangeRates"; }

    @Override
    public JsonObject fetchData(Map<String, String> params) throws IOException {
        Map<String, String> mutableParams = new HashMap<>(params);
        mutableParams.put("app_id", apiKey);
        return buildRequestAndExecute(mutableParams).getAsJsonObject();
    }
}
