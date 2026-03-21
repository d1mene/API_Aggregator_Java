package d1mene.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import d1mene.client.APIClient;
import d1mene.data.APIRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregatorService {

    private final CoinGeckoParser coinGeckoParser = new CoinGeckoParser();
    private final WeatherAPIParser weatherAPIParser = new WeatherAPIParser();
    private final OpenExchangeRatesParser openExchangeRatesParser = new OpenExchangeRatesParser();

    public Map<String, List<APIRecord>> aggregate(
            List<APIClient> clients,
            Map<String, Map<String, String>> paramsPerClient
    ) {
        Map<String, List<APIRecord>> result = new HashMap<>();

        for (APIClient client : clients) {
            Map<String, String> params = paramsPerClient.getOrDefault(client.getSourceName(), new HashMap<>());
            List<APIRecord> records = new ArrayList<>();

            try {
                JsonObject json = client.fetchData(params);
                List<APIRecord> parsed = parse(client.getSourceName(), json);
                records.addAll(parsed);
            } catch (IOException e) {
                System.err.println("Ошибка при запросе " + client.getSourceName() + ": " + e.getMessage());
            }

            result.put(client.getSourceName(), records);
        }

        return result;
    }

    private List<APIRecord> parse(String sourceName, JsonObject json) {
        List<APIRecord> records = new ArrayList<>();

        switch (sourceName) {
            case "CoinGecko" -> {
                JsonArray array = json.getAsJsonArray("data");
                records.addAll(coinGeckoParser.parse(array));
            }
            case "WeatherAPI" -> records.add(weatherAPIParser.parse(json));
            case "OpenExchangeRates" -> records.add(openExchangeRatesParser.parse(json));
            default -> System.err.println("Неизвестный источник: " + sourceName);
        }

        return records;
    }
}