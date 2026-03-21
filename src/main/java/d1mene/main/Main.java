package d1mene.main;

import d1mene.client.APIClient;
import d1mene.client.CoinGeckoClient;
import d1mene.client.OpenExchangeRatesClient;
import d1mene.client.WeatherAPIClient;
import d1mene.service.AggregatorService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static final Map<String, APIClient> AVAILABLE_CLIENTS = new LinkedHashMap<>();
    public static final AggregatorService AGGREGATOR = new AggregatorService();

    public static void main(String[] args) {
        try {
            AppConfig config = new AppConfig("config.properties");

            AVAILABLE_CLIENTS.put("CoinGecko", new CoinGeckoClient());
            AVAILABLE_CLIENTS.put("WeatherAPI", new WeatherAPIClient(config.get("weatherapi.key")));
            AVAILABLE_CLIENTS.put("OpenExchangeRates", new OpenExchangeRatesClient(config.get("openexchangerates.key")));

        } catch (IOException e) {
            System.err.println("Не найден файл конфигурации config.properties: " + e.getMessage());
            return;
        } catch (IllegalStateException e) {
            System.err.println("Ошибка конфигурации: " + e.getMessage());
            return;
        }

        if (args.length > 0) {
            AutoMode.run(args);
        } else {
            InteractiveMode.run();
        }
    }
}