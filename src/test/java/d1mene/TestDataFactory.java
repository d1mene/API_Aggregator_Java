package d1mene;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import d1mene.data.CoinGeckoData;
import d1mene.data.OpenExchangeRatesData;
import d1mene.data.WeatherAPIData;

import java.util.HashMap;
import java.util.Map;

public class TestDataFactory {

    public static CoinGeckoData coinGeckoData() {
        CoinGeckoData data = new CoinGeckoData();
        data.setId("bitcoin");
        data.setSymbol("btc");
        data.setName("Bitcoin");
        data.setCurrentPrice(70000.0);
        data.setMarketCap(1.4e12);
        data.setPriceChange24h(400.0);
        data.setLastUpdated("2026-03-21T21:05:02.063Z");
        return data;
    }

    public static WeatherAPIData weatherApiData() {
        WeatherAPIData.Location location = new WeatherAPIData.Location();
        location.setName("Moscow");
        location.setCountry("Russia");
        location.setLocalTime("2026-03-21 23:59");

        WeatherAPIData.Condition condition = new WeatherAPIData.Condition();
        condition.setText("Clear");

        WeatherAPIData.Current current = new WeatherAPIData.Current();
        current.setTempC(2.0);
        current.setFeelsLikeC(0.8);
        current.setHumidity(69);
        current.setWindKph(4.7);
        current.setCondition(condition);

        WeatherAPIData data = new WeatherAPIData();
        data.setLocation(location);
        data.setCurrent(current);
        return data;
    }

    public static OpenExchangeRatesData exchangeRatesData() {
        OpenExchangeRatesData data = new OpenExchangeRatesData();
        data.setBase("USD");
        data.setTimestamp(1741564800L);
        Map<String, Double> rates = new HashMap<>();
        rates.put("EUR", 0.86);
        rates.put("RUB", 82.8);
        rates.put("GBP", 0.75);
        rates.put("CNY", 6.88);
        data.setRates(rates);
        return data;
    }

    public static JsonObject coinGeckoJson() {
        JsonObject coin = new JsonObject();
        coin.addProperty("id", "bitcoin");
        coin.addProperty("symbol", "btc");
        coin.addProperty("name", "Bitcoin");
        coin.addProperty("current_price", 70000.0);
        coin.addProperty("market_cap", 1.4e12);
        coin.addProperty("price_change_24h", 400.0);
        coin.addProperty("price_change_percentage_24h", 0.57);
        coin.addProperty("last_updated", "2026-03-21T21:05:02.063Z");
        return coin;
    }

    public static JsonObject coinGeckoWrapper() {
        JsonArray array = new JsonArray();
        array.add(coinGeckoJson());
        JsonObject wrapper = new JsonObject();
        wrapper.add("data", array);
        return wrapper;
    }

    public static JsonObject weatherJson() {
        JsonObject location = new JsonObject();
        location.addProperty("name", "Moscow");
        location.addProperty("country", "Russia");
        location.addProperty("localtime", "2026-03-21 23:59");

        JsonObject condition = new JsonObject();
        condition.addProperty("text", "Clear");

        JsonObject current = new JsonObject();
        current.addProperty("temp_c", 2.0);
        current.addProperty("feelslike_c", 0.8);
        current.addProperty("humidity", 69);
        current.addProperty("wind_kph", 4.7);
        current.add("condition", condition);

        JsonObject json = new JsonObject();
        json.add("location", location);
        json.add("current", current);
        return json;
    }

    public static JsonObject exchangeRatesJson() {
        JsonObject rates = new JsonObject();
        rates.addProperty("EUR", 0.86);
        rates.addProperty("RUB", 82.8);
        rates.addProperty("GBP", 0.75);
        rates.addProperty("CNY", 6.88);

        JsonObject json = new JsonObject();
        json.addProperty("base", "USD");
        json.addProperty("timestamp", 1741564800L);
        json.add("rates", rates);
        return json;
    }
}