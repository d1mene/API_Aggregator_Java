package d1mene.main;

import d1mene.client.APIClient;
import d1mene.service.PullingManager;
import d1mene.storage.FileStorage;
import d1mene.storage.ThreadStorage;
import d1mene.storage.WriteMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoMode {

    public static void run(String[] args) {
        String apisArg = null;
        String formatArg = null;
        String outputArg = null;
        int n = 3;
        long t = 10;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--apis" -> apisArg = args[++i];
                case "--format" -> formatArg = args[++i];
                case "--output" -> outputArg = args[++i];
                case "--n" -> {
                    try {
                        n = Integer.parseInt(args[++i]);
                        if (n <= 0) {
                            System.err.println("n должно быть больше 0, используется значение по умолчанию: 3");
                            n = 3;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Некорректное значение n, используется значение по умолчанию: 3");
                    }
                }
                case "--t" -> {
                    try {
                        t = Long.parseLong(args[++i]);
                        if (t <= 0) {
                            System.err.println("t должно быть больше 0, используется значение по умолчанию: 10");
                            t = 10;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Некорректное значение t, используется значение по умолчанию: 10");
                    }
                }
            }
        }

        if (apisArg == null || formatArg == null || outputArg == null) {
            System.err.println("Использование: --apis <api1,api2> --format <json|csv> --output <file> --n <число> --t <секунды>");
            return;
        }

        if (!outputArg.contains(".")) {
            outputArg = outputArg + "." + formatArg.toLowerCase();
        }

        String[] apiNames = apisArg.split(",");
        List<APIClient> clients = new ArrayList<>();

        for (String name : apiNames) {
            APIClient client = Main.AVAILABLE_CLIENTS.get(name.trim());
            if (client == null) {
                System.err.println("Неизвестный API: " + name.trim());
                return;
            }
            clients.add(client);
        }

        FileStorage baseStorage;
        try {
            baseStorage = ParamsHandler.createStorage(formatArg, outputArg);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        ThreadStorage storage = new ThreadStorage(baseStorage);
        Map<String, Map<String, String>> params = ParamsHandler.buildDefaultParams(clients);
        PullingManager pullingManager = new PullingManager(n, t, storage, params, WriteMode.APPEND);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nПолучен сигнал остановки...");
            pullingManager.stop();
            storage.shutdown();
            System.out.println("Ресурсы освобождены.");
        }));

        System.out.println("Запуск опроса: APIs=" + apisArg + ", n=" + n + ", t=" + t + "с, файл=" + outputArg);
        pullingManager.start(clients);

        System.out.println("Опрос запущен. Для остановки нажмите Ctrl+C.");
    }

    private static Map<String, String> defaultParamsFor(String apiName) {
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
}