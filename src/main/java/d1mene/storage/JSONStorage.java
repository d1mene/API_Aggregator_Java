package d1mene.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import d1mene.data.APIRecord;
import d1mene.data.CoinGeckoData;
import d1mene.data.OpenExchangeRatesData;
import d1mene.data.WeatherAPIData;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JSONStorage implements FileStorage {

    private final Path filePath;
    private final Gson gson;
    private static final DateTimeFormatter ISO = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
            .withZone(ZoneOffset.UTC);

    public JSONStorage(String filePath) {
        this.filePath = Path.of(filePath);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void save(List<APIRecord> records, WriteMode mode) throws IOException {
        JsonArray existing = new JsonArray();

        if (mode == WriteMode.APPEND && Files.exists(filePath)) {
            existing = readRawArray();
        }

        int nextId = existing.size() + 1;
        String timestamp = ISO.format(Instant.now());

        for (APIRecord record : records) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", nextId++);
            entry.addProperty("source", record.getSourceName());
            entry.addProperty("timestamp", timestamp);
            entry.add("data", gson.toJsonTree(record));
            existing.add(entry);
        }

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(existing, writer);
        }
    }

    @Override
    public List<APIRecord> readAll() throws IOException {
        List<APIRecord> result = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return result;
        }

        for (JsonElement element : readRawArray()) {
            JsonObject entry = element.getAsJsonObject();
            String source = entry.get("source").getAsString();
            JsonObject data = entry.getAsJsonObject("data");
            APIRecord record = deserialize(source, data);
            if (record != null) {
                result.add(record);
            }
        }

        return result;
    }

    @Override
    public List<APIRecord> readBySource(String sourceName) throws IOException {
        List<APIRecord> result = new ArrayList<>();
        for (APIRecord record : readAll()) {
            if (record.getSourceName().equals(sourceName)) result.add(record);
        }
        return result;
    }

    private JsonArray readRawArray() throws IOException {
        try (FileReader reader = new FileReader(filePath.toFile())) {
            JsonElement parsed = JsonParser.parseReader(reader);
            return parsed.isJsonArray() ? parsed.getAsJsonArray() : new JsonArray();
        }
    }

    private APIRecord deserialize(String source, JsonObject data) {
        return switch (source) {
            case "CoinGecko" -> gson.fromJson(data, CoinGeckoData.class);
            case "WeatherAPI" -> gson.fromJson(data, WeatherAPIData.class);
            case "OpenExchangeRates" -> gson.fromJson(data, OpenExchangeRatesData.class);
            default -> {
                System.err.println("Неизвестный источник при чтении: " + source);
                yield null;
            }
        };
    }
}