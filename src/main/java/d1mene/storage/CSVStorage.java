package d1mene.storage;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import d1mene.data.APIRecord;
import d1mene.data.RawRecord;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CSVStorage implements FileStorage {

    private final Path filePath;
    private static final DateTimeFormatter ISO = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
            .withZone(ZoneOffset.UTC);

    private static final List<String> BASE_HEADERS = List.of("id", "source", "timestamp");

    public CSVStorage(String filePath) {
        this.filePath = Path.of(filePath);
    }

    @Override
    public void save(List<APIRecord> records, WriteMode mode) throws IOException {
        boolean append = mode == WriteMode.APPEND && Files.exists(filePath);
        String timestamp = ISO.format(Instant.now());

        List<String> allColumns = resolveColumns(records, append);
        int nextId = append ? getNextId() : 1;

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile(), append))) {
            if (!append) {
                writer.writeNext(allColumns.toArray(new String[0]));
            }
            for (APIRecord record : records) {
                writer.writeNext(toRow(record, nextId++, timestamp, allColumns));
            }
        }
    }

    @Override
    public List<APIRecord> readAll() throws IOException {
        return readRawRows(null);
    }

    @Override
    public List<APIRecord> readBySource(String sourceName) throws IOException {
        return readRawRows(sourceName);
    }

    private List<APIRecord> readRawRows(String sourceName) throws IOException {
        if (!Files.exists(filePath)) return new ArrayList<>();

        List<APIRecord> result = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
            List<String[]> rows = reader.readAll();
            if (rows.size() <= 1) {
                return result;
            }

            String[] headers = rows.get(0);
            rows.remove(0);

            for (String[] row : rows) {
                if (sourceName != null && !row[1].equals(sourceName))
                    continue;
                result.add(fromRow(headers, row));
            }
        } catch (CsvException e) {
            throw new IOException("Ошибка чтения CSV: " + e.getMessage());
        }
        return result;
    }

    private List<String> resolveColumns(List<APIRecord> records, boolean append) throws IOException {
        Set<String> columns = new LinkedHashSet<>(BASE_HEADERS);

        if (append && Files.exists(filePath)) {
            try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
                String[] existingHeaders = reader.readNext();
                if (existingHeaders != null) columns.addAll(Arrays.asList(existingHeaders));
            } catch (CsvException e) {
                throw new IOException("Ошибка чтения заголовков: " + e.getMessage());
            }
        }

        for (APIRecord record : records) {
            columns.addAll(record.getFields().keySet());
        }

        return new ArrayList<>(columns);
    }

    private String[] toRow(APIRecord record, int id, String timestamp, List<String> columns) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("id", String.valueOf(id));
        fields.put("source", record.getSourceName());
        fields.put("timestamp", timestamp);
        fields.putAll(record.getFields());

        String[] row = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            row[i] = fields.getOrDefault(columns.get(i), "");
        }
        return row;
    }

    private APIRecord fromRow(String[] headers, String[] row) {
        Map<String, String> fieldMap = new LinkedHashMap<>();
        for (int i = 0; i < headers.length && i < row.length; i++) {
            fieldMap.put(headers[i], row[i]);
        }
        return new RawRecord(fieldMap.getOrDefault("source", ""), fieldMap);
    }

    private int getNextId() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
            List<String[]> rows = reader.readAll();
            if (rows.size() <= 1) return 1;
            return Integer.parseInt(rows.get(rows.size() - 1)[0]) + 1;
        } catch (CsvException | NumberFormatException e) {
            return 1;
        }
    }
}