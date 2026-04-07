package d1mene.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;

public class CoinGeckoClient extends AbstractAPIClient {

    private static final String PROD_URL = "https://api.coingecko.com/api/v3/coins/markets";

    public CoinGeckoClient() {
        super(PROD_URL);
    }

    CoinGeckoClient(String baseUrl) {
        super(baseUrl);
    }

    @Override
    public String getSourceName() { return "CoinGecko"; }

    @Override
    public JsonObject fetchData(Map<String, String> params) throws IOException {
        JsonElement result = buildRequestAndExecute(params);
        JsonArray array = result.getAsJsonArray();
        JsonObject wrapper = new JsonObject();
        wrapper.add("data", array);
        return wrapper;
    }
}
