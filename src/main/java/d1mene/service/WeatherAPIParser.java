package d1mene.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import d1mene.data.WeatherAPIData;

public class WeatherAPIParser {

    private final Gson gson = new Gson();

    public WeatherAPIData parse(JsonObject json) {
        return gson.fromJson(json, WeatherAPIData.class);
    }
}