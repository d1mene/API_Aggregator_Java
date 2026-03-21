package d1mene.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import d1mene.client.APIClient;
import d1mene.storage.*;

public class ParamsHandler {

    public static Map<String, Map<String, String>> buildDefaultParams(List<APIClient> clients) {
        Map<String, Map<String, String>> params = new HashMap<>();
        for (APIClient client : clients) {
            params.put(client.getSourceName(), defaultParamsFor(client.getSourceName()));
        }
        return params;
    }

    public static Map<String, String> defaultParamsFor(String apiName) {
        return switch (apiName) {
            case "CoinGecko" -> {
                Map<String, String> p = new HashMap<>();
                p.put("vs_currency", "usd");
                p.put("ids", "bitcoin,ethereum");
                yield p;
            }
            case "WeatherAPI" -> {
                Map<String, String> p = new HashMap<>();
                p.put("q", "Moscow");
                p.put("aqi", "yes");
                yield p;
            }
            case "OpenExchangeRates" -> {
                Map<String, String> p = new HashMap<>();
                p.put("symbols", "EUR,RUB,GBP,CNY");
                yield p;
            }
            default -> new HashMap<>();
        };
    }

    public static FileStorage createStorage(String format, String outputPath) {
        return switch (format.toLowerCase()) {
            case "json" -> new JSONStorage(outputPath);
            case "csv" -> new CSVStorage(outputPath);
            default -> throw new IllegalArgumentException(
                    "Неизвестный формат: " + format + ". Используйте json или csv.");
        };
    }
}