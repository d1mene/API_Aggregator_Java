package d1mene.service;

import com.google.gson.JsonArray;
import d1mene.TestDataFactory;
import d1mene.data.CoinGeckoData;
import d1mene.data.OpenExchangeRatesData;
import d1mene.data.WeatherAPIData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void givenValidJson_whenParseCoinGecko_thenReturnsCorrectData() {
        JsonArray array = new JsonArray();
        array.add(TestDataFactory.coinGeckoJson());

        List<CoinGeckoData> result = new CoinGeckoParser().parse(array);

        assertEquals(1, result.size());
        assertEquals("bitcoin", result.get(0).getId());
        assertEquals("btc", result.get(0).getSymbol());
        assertEquals(70000.0, result.get(0).getCurrentPrice());
    }

    @Test
    void givenMultipleCoins_whenParseCoinGecko_thenReturnsAll() {
        JsonArray array = new JsonArray();
        array.add(TestDataFactory.coinGeckoJson());
        array.add(TestDataFactory.coinGeckoJson());
        array.add(TestDataFactory.coinGeckoJson());

        List<CoinGeckoData> result = new CoinGeckoParser().parse(array);

        assertEquals(3, result.size());
    }

    @Test
    void givenEmptyArray_whenParseCoinGecko_thenReturnsEmptyList() {
        List<CoinGeckoData> result = new CoinGeckoParser().parse(new JsonArray());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenValidJson_whenParseWeatherAPI_thenReturnsCorrectData() {
        WeatherAPIData result = new WeatherAPIParser().parse(TestDataFactory.weatherJson());

        assertNotNull(result);
        assertEquals("Moscow", result.getLocation().getName());
        assertEquals(2.0, result.getCurrent().getTempC());
        assertEquals("Clear", result.getCurrent().getCondition().getText());
    }

    @Test
    void givenValidJson_whenParseOpenExchangeRates_thenReturnsCorrectData() {
        OpenExchangeRatesData result = new OpenExchangeRatesParser()
                .parse(TestDataFactory.exchangeRatesJson());

        assertNotNull(result);
        assertEquals("USD", result.getBase());
        assertEquals(0.86, result.getRates().get("EUR"));
    }
}