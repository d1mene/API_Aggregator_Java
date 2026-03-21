package d1mene.main;

import d1mene.client.APIClient;
import d1mene.data.APIRecord;
import d1mene.storage.CSVStorage;
import d1mene.storage.FileStorage;
import d1mene.storage.JSONStorage;
import d1mene.storage.WriteMode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoMode {

    public static void run(String[] args) {
        String apisArg = null;
        String formatArg = null;
        String outputArg = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--apis" -> apisArg = args[++i];
                case "--format" -> formatArg = args[++i];
                case "--output" -> outputArg = args[++i];
            }
        }

        if (apisArg == null || formatArg == null || outputArg == null) {
            System.err.println("Использование: --apis <api1,api2> --format <json|csv> --output <file>");
            return;
        }

        if (!outputArg.contains(".")) {
            outputArg = outputArg + "." + formatArg.toLowerCase();
        }

        List<String> apiNames = Arrays.asList(apisArg.split(","));
        List<APIClient> clients = new ArrayList<>();

        for (String name : apiNames) {
            APIClient client = Main.AVAILABLE_CLIENTS.get(name.trim());
            if (client == null) {
                System.err.println("Неизвестный API: " + name.trim());
                return;
            }
            clients.add(client);
        }

        Map<String, Map<String, String>> params = buildDefaultParams(apiNames);
        Map<String, List<APIRecord>> aggregated = Main.AGGREGATOR.aggregate(clients, params);

        List<APIRecord> allRecords = new ArrayList<>();
        aggregated.values().forEach(allRecords::addAll);

        FileStorage storage = createStorage(formatArg, outputArg);
        if (storage == null) {
            return;
        }

        try {
            storage.save(allRecords, WriteMode.CREATE);
            System.out.println("Данные сохранены в " + outputArg);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private static FileStorage createStorage(String format, String outputPath) {
        return switch (format.toLowerCase()) {
            case "json" -> new JSONStorage(outputPath);
            case "csv" -> new CSVStorage(outputPath);
            default -> {
                System.err.println("Неизвестный формат: " + format + ". Используйте json или csv.");
                yield null;
            }
        };
    }

    private static Map<String, Map<String, String>> buildDefaultParams(List<String> apiNames) {
        Map<String, Map<String, String>> params = new HashMap<>();
        for (String name : apiNames) {
            params.put(name.trim(), defaultParamsFor(name.trim()));
        }
        return params;
    }

    private static Map<String, String> defaultParamsFor(String apiName) {
        return switch (apiName) {
            case "CoinGecko" -> Map.of("vs_currency", "usd", "ids", "bitcoin,ethereum");
            case "WeatherAPI" -> {
                Map<String, String> weatherMap = new HashMap<>();
                weatherMap.put("q", "Moscow");
                weatherMap.put("aqi", "yes");
                yield weatherMap;
            }
            case "OpenExchangeRates" -> {
                Map<String, String> ratesMap = new HashMap<>();
                ratesMap.put("symbols", "EUR,RUB,GBP,CNY");
                yield ratesMap;
            }
            default -> new HashMap<>();
        };
    }
}