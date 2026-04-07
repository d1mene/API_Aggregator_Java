package d1mene.user_input;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import d1mene.client.CoinGeckoClient;
import d1mene.client.WeatherAPIClient;
import d1mene.client.OpenExchangeRatesClient;
import d1mene.main.Main;

import static org.junit.jupiter.api.Assertions.*;

class InteractiveModeTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;

    @BeforeEach
    void setUp() {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
        System.setErr(new PrintStream(errStream));

        Main.AVAILABLE_CLIENTS.clear();
        Main.AVAILABLE_CLIENTS.put("CoinGecko", new CoinGeckoClient());
        Main.AVAILABLE_CLIENTS.put("WeatherAPI", new WeatherAPIClient("test_key"));
        Main.AVAILABLE_CLIENTS.put("OpenExchangeRates", new OpenExchangeRatesClient("test_key"));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        InteractiveMode.setScanner(new Scanner(System.in));
    }

    private void simulateInput(String input) {
        InteractiveMode.setScanner(
                new Scanner(new ByteArrayInputStream(input.getBytes()))
        );
    }

    private String getOutput() {
        return outStream.toString();
    }
    private String getErrOutput() {
        return errStream.toString();
    }

    @Test
    void givenNoApiSelected_whenRun_thenPrintsError() {
        simulateInput("\n");
        InteractiveMode.run();
        assertTrue(getErrOutput().contains("Не выбран ни один API."));
    }

    @Test
    void givenInvalidClientNumber_whenRun_thenPrintsError() {
        simulateInput("99\njson\ntestfile\nnew\n1\n5\n5\n");
        InteractiveMode.run();
        assertTrue(getErrOutput().contains("Нет API с номером"));
    }

    @Test
    void givenNonNumericClient_whenRun_thenPrintsError() {
        simulateInput("abc\njson\ntestfile\nnew\n1\n5\n5\n");
        InteractiveMode.run();
        assertTrue(getErrOutput().contains("Некорректный ввод"));
    }

    @Test
    void givenInvalidFormat_whenRun_thenRepeatsQuestion() {
        simulateInput("1\nxml\njson\ntestfile\nnew\n1\n5\n5\n");
        InteractiveMode.run();
        assertTrue(getErrOutput().contains("Введите json или csv."));
    }

    @Test
    void givenNegativeN_whenRun_thenRepeatsQuestion() {
        simulateInput("1\njson\ntestfile\nnew\n-1\n1\n5\n5\n");
        InteractiveMode.run();
        assertTrue(getErrOutput().contains("n должно быть больше 0."));
    }

    @Test
    void givenNonNumericN_whenRun_thenRepeatsQuestion() {
        simulateInput("1\njson\ntestfile\nnew\nabc\n1\n5\n5\n");
        InteractiveMode.run();
        assertTrue(getErrOutput().contains("Введите целое число."));
    }

    @Test
    void givenNegativeT_whenRun_thenRepeatsQuestion() {
        simulateInput("1\njson\ntestfile\nnew\n1\n-5\n5\n5\n");
        InteractiveMode.run();
        assertTrue(getErrOutput().contains("t должно быть больше 0."));
    }

    @Test
    void givenAppendMode_whenRun_thenAcceptsAppend() {
        simulateInput("1\njson\ntestfile\nappend\n1\n5\n5\n");
        InteractiveMode.run();
        assertTrue(getOutput().contains("Завершение работы."));
    }

    @Test
    void givenChoiceFive_whenRun_thenExitsGracefully() {
        simulateInput("1\njson\ntestfile\nnew\n1\n5\n5\n");
        InteractiveMode.run();
        assertTrue(getOutput().contains("Завершение работы."));
    }

    @Test
    void givenChoiceTwo_whenNotStarted_whenRun_thenPrintsNotStarted() {
        simulateInput("1\njson\ntestfile\nnew\n1\n5\n2\n5\n");
        InteractiveMode.run();
        assertTrue(getOutput().contains("Опрос не запущен."));
    }

    @Test
    void givenChoiceThree_whenRun_thenPrintsEmptyMessage() {
        simulateInput("1\njson\nnonexistentfile\nnew\n1\n5\n3\n5\n");
        InteractiveMode.run();
        assertTrue(getOutput().contains("Файл пуст или не существует."));
    }

    @Test
    void givenChoiceFour_whenRun_thenPrintsNoDataMessage() {
        simulateInput("1\njson\nnonexistentfile\nnew\n1\n5\n4\nCoinGecko\n5\n");
        InteractiveMode.run();
        assertTrue(getOutput().contains("Данных по CoinGecko нет."));
    }

    @Test
    void givenInvalidMenuChoice_whenRun_thenPrintsError() {
        simulateInput("1\njson\ntestfile\nnew\n1\n5\nX\n5\n");
        InteractiveMode.run();
        assertTrue(getErrOutput().contains("Неверный ввод."));
    }
}