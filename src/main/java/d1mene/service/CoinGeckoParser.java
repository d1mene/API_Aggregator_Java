package d1mene.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import d1mene.data.CoinGeckoData;

import java.util.ArrayList;
import java.util.List;

public class CoinGeckoParser {

    private final Gson gson = new Gson();

    public List<CoinGeckoData> parse(JsonArray json) {
        List<CoinGeckoData> result = new ArrayList<>();
        for (int i = 0; i < json.size(); i++) {
            result.add(gson.fromJson(json.get(i), CoinGeckoData.class));
        }
        return result;
    }
}