package d1mene.storage;

import d1mene.TestDataFactory;
import d1mene.data.APIRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    @TempDir
    Path tempDir;

    private Path tempJson;
    private Path tempCsv;

    @BeforeEach
    void setUp() {
        tempJson = tempDir.resolve("test.json");
        tempCsv = tempDir.resolve("test.csv");
    }

    @Test
    void givenRecords_whenSaveJson_thenFileExists() throws IOException {
        JSONStorage storage = new JSONStorage(tempJson.toString());
        storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.CREATE);

        assertTrue(Files.exists(tempJson));
        assertTrue(Files.size(tempJson) > 0);
    }

    @Test
    void givenSavedRecords_whenReadAllJson_thenReturnsRecords() throws IOException {
        JSONStorage storage = new JSONStorage(tempJson.toString());
        storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.CREATE);

        List<APIRecord> result = storage.readAll();

        assertFalse(result.isEmpty());
        assertEquals("CoinGecko", result.get(0).getSourceName());
    }

    @Test
    void givenAppendMode_whenSaveJson_thenRecordsAccumulate() throws IOException {
        JSONStorage storage = new JSONStorage(tempJson.toString());
        storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.CREATE);
        storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.APPEND);

        assertEquals(2, storage.readAll().size());
    }

    @Test
    void givenSourceName_whenReadBySourceJson_thenReturnsFiltered() throws IOException {
        JSONStorage storage = new JSONStorage(tempJson.toString());
        storage.save(List.of(
                TestDataFactory.coinGeckoData(),
                TestDataFactory.weatherApiData()
        ), WriteMode.CREATE);

        assertFalse(storage.readBySource("CoinGecko").isEmpty());
        assertFalse(storage.readBySource("WeatherAPI").isEmpty());
        assertTrue(storage.readBySource("OpenExchangeRates").isEmpty());
    }

    @Test
    void givenRecords_whenSaveCsv_thenFileExists() throws IOException {
        CSVStorage storage = new CSVStorage(tempCsv.toString());
        storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.CREATE);

        assertTrue(Files.exists(tempCsv));
        assertTrue(Files.size(tempCsv) > 0);
    }

    @Test
    void givenSavedRecords_whenReadAllCsv_thenReturnsRecords() throws IOException {
        CSVStorage storage = new CSVStorage(tempCsv.toString());
        storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.CREATE);

        List<APIRecord> result = storage.readAll();

        assertFalse(result.isEmpty());
        assertEquals("CoinGecko", result.get(0).getSourceName());
    }

    @Test
    void givenAppendMode_whenSaveCsv_thenRecordsAccumulate() throws IOException {
        CSVStorage storage = new CSVStorage(tempCsv.toString());
        storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.CREATE);
        storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.APPEND);

        assertEquals(2, storage.readAll().size());
    }

    @Test
    void givenMultipleApiRecords_whenSaveCsv_thenAllSourcesSaved() throws IOException {
        CSVStorage storage = new CSVStorage(tempCsv.toString());
        storage.save(List.of(
                TestDataFactory.coinGeckoData(),
                TestDataFactory.weatherApiData(),
                TestDataFactory.exchangeRatesData()
        ), WriteMode.CREATE);

        assertEquals(3, storage.readAll().size());
    }

    @Test
    void givenThreadSafeStorage_whenSaveConcurrently_thenAllRecordsSaved()
            throws IOException, InterruptedException {
        JSONStorage base = new JSONStorage(tempJson.toString());
        ThreadStorage storage = new ThreadStorage(base);

        Thread t1 = new Thread(() -> {
            try {
                storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.APPEND);
            } catch (IOException e) {
                fail("Ошибка в потоке 1: " + e.getMessage());
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                storage.save(List.of(TestDataFactory.weatherApiData()), WriteMode.APPEND);
            } catch (IOException e) {
                fail("Ошибка в потоке 2: " + e.getMessage());
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        storage.shutdown();

        assertEquals(2, base.readAll().size());
    }

    @Test
    void givenNoFile_whenReadAllCsv_thenReturnsEmptyList() throws IOException {
        CSVStorage storage = new CSVStorage(tempDir.resolve("non_existent.csv").toString());
        List<APIRecord> result = storage.readAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void givenHeaderOnlyFile_whenReadAllCsv_thenReturnsEmptyList() throws IOException {
        Files.writeString(tempCsv, "id,source,data\n");
        CSVStorage storage = new CSVStorage(tempCsv.toString());

        List<APIRecord> result = storage.readAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void givenSavedRecords_whenReadBySourceCsv_thenFiltersCorrectly() throws IOException {
        CSVStorage storage = new CSVStorage(tempCsv.toString());
        storage.save(List.of(
                TestDataFactory.coinGeckoData(),
                TestDataFactory.weatherApiData()
        ), WriteMode.CREATE);

        List<APIRecord> result = storage.readBySource("CoinGecko");

        assertEquals(1, result.size());
        assertEquals("CoinGecko", result.get(0).getSourceName());
    }

    @Test
    void givenMalformedCsv_whenReadAll_thenThrowsIOException() throws IOException {
        Files.writeString(tempCsv, "\"мусор_шлак,column2,column3");
        CSVStorage storage = new CSVStorage(tempCsv.toString());

        assertThrows(IOException.class, storage::readAll);
    }

    @Test
    void givenInvalidIdInCsv_whenSaveAppend_thenResetsId() throws IOException {
        Files.writeString(tempCsv, "id,source\nnot_a_number,CoinGecko\n");
        CSVStorage storage = new CSVStorage(tempCsv.toString());

        storage.save(List.of(TestDataFactory.weatherApiData()), WriteMode.APPEND);

        List<APIRecord> records = storage.readAll();
        assertFalse(records.isEmpty());
    }

    @Test
    void givenExistingFile_whenResolveColumnsInAppendMode_thenAddsNewHeaders() throws IOException {
        Files.writeString(tempCsv, "id,source,old_val\n1,Source1,data\n");
        CSVStorage storage = new CSVStorage(tempCsv.toString());

        storage.save(List.of(TestDataFactory.coinGeckoData()), WriteMode.APPEND);

        List<APIRecord> result = storage.readAll();
        assertTrue(result.size() >= 2);
    }
}