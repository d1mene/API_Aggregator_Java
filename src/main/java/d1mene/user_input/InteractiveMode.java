package d1mene.user_input;

import d1mene.client.APIClient;
import d1mene.data.APIRecord;
import d1mene.main.Main;
import d1mene.main.ParamsHandler;
import d1mene.service.PullingManager;
import d1mene.storage.*;
import d1mene.storage.ThreadStorage;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InteractiveMode {

    @Setter
    private static Scanner scanner = new Scanner(System.in);

    private static void startPulling(PullingManager manager, List<APIClient> clients) {
        if (manager.isActive()) {
            System.out.println("Опрос уже запущен.");
            return;
        }
        manager.start(clients);
    }

    private static void stopPulling(PullingManager manager) {
        if (!manager.isActive()) {
            System.out.println("Опрос не запущен.");
            return;
        }
        manager.stop();
    }

    public static void run() {
        System.out.println("=== Агрегатор данных ===");

        List<APIClient> clients = selectClients();
        if (clients.isEmpty()) {
            System.err.println("Не выбран ни один API.");
            return;
        }

        String format = selectFormat();
        String outputPath = selectOutputPath(format);
        WriteMode writeMode = selectWriteMode();
        int n = selectN();
        long t = selectT();

        FileStorage baseStorage;
        try {
            baseStorage = ParamsHandler.createStorage(format, outputPath);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }
        ThreadStorage storage = new ThreadStorage(baseStorage);

        Map<String, Map<String, String>> params = ParamsHandler.buildDefaultParams(clients);
        PullingManager pullingManager = new PullingManager(n, t, storage, params, writeMode);

        boolean running = true;
        while (running) {
            System.out.println("\n--- Меню ---");
            System.out.println("1. Запустить опрос");
            System.out.println("2. Остановить опрос");
            System.out.println("3. Вывести всё содержимое файла");
            System.out.println("4. Вывести данные по конкретному API");
            System.out.println("5. Выход");
            System.out.print("Выберите: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> startPulling(pullingManager, clients);
                case "2" -> stopPulling(pullingManager);
                case "3" -> printAll(storage);
                case "4" -> printBySource(storage);
                case "5" -> running = false;
                default -> System.err.println("Неверный ввод.");
            }
        }

        if (pullingManager.isActive()) {
            pullingManager.stop();
        }
        storage.shutdown();
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

    private static WriteMode selectWriteMode() {
        System.out.print("Режим записи — создать новый (new) или дозаписать (append): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("append") ? WriteMode.APPEND : WriteMode.CREATE;
    }

    private static int selectN() {
        while (true) {
            System.out.print("Максимальное количество одновременных задач (n): ");
            try {
                int n = Integer.parseInt(scanner.nextLine().trim());
                if (n > 0) return n;
                System.err.println("n должно быть больше 0.");
            } catch (NumberFormatException e) {
                System.err.println("Введите целое число.");
            }
        }
    }

    private static long selectT() {
        while (true) {
            System.out.print("Интервал опроса в секундах (t): ");
            try {
                long t = Long.parseLong(scanner.nextLine().trim());
                if (t > 0) return t;
                System.err.println("t должно быть больше 0.");
            } catch (NumberFormatException e) {
                System.err.println("Введите целое число.");
            }
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
        System.out.print("Введите имя API (CoinGecko / WeatherAPI / OpenExchangeRates): ");
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
}