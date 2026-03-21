package d1mene.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import d1mene.data.OpenExchangeRatesData;

public class OpenExchangeRatesParser {

    private final Gson gson = new Gson();

    public OpenExchangeRatesData parse(JsonObject json) {
        return gson.fromJson(json, OpenExchangeRatesData.class);
    }
}