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
import java.util.Scanner;

public class InteractiveMode {

    private static final Scanner scanner = new Scanner(System.in);

    public static void run() {
        System.out.println("=== Агрегатор данных ===");

        List<APIClient> clients = selectClients();
        if (clients.isEmpty()) {
            System.err.println("Не выбран ни один API.");
            return;
        }

        String format = selectFormat();
        String outputPath = selectOutputPath(format);
        WriteMode writeMode = selectWriteMode(outputPath);

        FileStorage storage = createStorage(format, outputPath);

        boolean running = true;
        while (running) {
            System.out.println("\n=== Меню ===");
            System.out.println("1. Запросить данные и сохранить");
            System.out.println("2. Вывести всё содержимое файла");
            System.out.println("3. Вывести данные по конкретному API");
            System.out.println("4. Выход");
            System.out.print("Выберите: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> fetchAndSave(clients, storage, writeMode);
                case "2" -> printAll(storage);
                case "3" -> printBySource(storage);
                case "4" -> running = false;
                default -> System.err.println("Неверный ввод.");
            }
        }

        System.out.println("Завершение работы.");
    }

    private static List<APIClient> selectClients() {
        List<String> keys = new ArrayList<>(Main.AVAILABLE_CLIENTS.keySet());

        System.out.println("\nДоступные API:");
        for (int i = 0; i < keys.size(); i++) {
            System.out.println((i + 1) + ". " + keys.get(i));
        }
        System.out.print("Выберите через запятую (пример: 1,3): ");
        String input = scanner.nextLine().trim();

        List<APIClient> selected = new ArrayList<>();
        for (String part : input.split(",")) {
            try {
                int index = Integer.parseInt(part.trim()) - 1;
                if (index >= 0 && index < keys.size()) {
                    selected.add(Main.AVAILABLE_CLIENTS.get(keys.get(index)));
                } else {
                    System.err.println("Нет API с номером: " + (index + 1));
                }
            } catch (NumberFormatException e) {
                System.err.println("Некорректный ввод: " + part.trim());
            }
        }
        return selected;
    }

    private static String selectFormat() {
        while (true) {
            System.out.print("\nФормат файла (json/csv): ");
            String format = scanner.nextLine().trim().toLowerCase();
            if (format.equals("json") || format.equals("csv")) {
                return format;
            }
            System.err.println("Введите json или csv.");
        }
    }

    private static String selectOutputPath(String format) {
        System.out.print("Имя выходного файла: ");
        String name = scanner.nextLine().trim();

        if (!name.endsWith("." + format)) {
            name = name + "." + format;
        }

        return name;
    }

    private static WriteMode selectWriteMode(String outputPath) {
        while (true) {
            System.out.print("\nРежим записи — создать новый (new) или дозаписать (append): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("append")) {
                return WriteMode.APPEND;
            } else if (input.equals("new")) {
                return WriteMode.CREATE;
            }
            System.err.println("Неизвестная опция: " + input
                        + ". Используйте new или append.");
        }
    }

    private static FileStorage createStorage(String format, String outputPath) {
        return switch (format.toLowerCase()) {
            case "json" -> new JSONStorage(outputPath);
            case "csv" -> new CSVStorage(outputPath);
            default -> throw new IllegalArgumentException("Неизвестный формат: " + format
                    + ". Используйте json или csv.");
        };
    }

    private static void fetchAndSave(List<APIClient> clients, FileStorage storage, WriteMode writeMode) {
        Map<String, Map<String, String>> params = buildDefaultParams(clients);
        Map<String, List<APIRecord>> aggregated = Main.AGGREGATOR.aggregate(clients, params);

        List<APIRecord> allRecords = new ArrayList<>();
        aggregated.values().forEach(allRecords::addAll);

        try {
            storage.save(allRecords, writeMode);
            System.out.println("Данные сохранены.");
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private static void printAll(FileStorage storage) {
        try {
            List<APIRecord> records = storage.readAll();
            if (records.isEmpty()) {
                System.out.println("Файл пуст или не существует.");
                return;
            }
            records.forEach(r -> System.out.println("[" + r.getSourceName() + "] " + r));
        } catch (IOException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
        }
    }

    private static void printBySource(FileStorage storage) {
        System.out.print("Введите имя API (CoinGecko/WeatherAPI/ OpenExchangeRates): ");
        String source = scanner.nextLine().trim();
        try {
            List<APIRecord> records = storage.readBySource(source);
            if (records.isEmpty()) {
                System.out.println("Данных по " + source + " нет.");
                return;
            }
            records.forEach(r -> System.out.println("[" + r.getSourceName() + "] " + r));
        } catch (IOException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
        }
    }

    private static Map<String, Map<String, String>> buildDefaultParams(List<APIClient> clients) {
        Map<String, Map<String, String>> params = new HashMap<>();
        for (APIClient client : clients) {
            params.put(client.getSourceName(), defaultParamsFor(client.getSourceName()));
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