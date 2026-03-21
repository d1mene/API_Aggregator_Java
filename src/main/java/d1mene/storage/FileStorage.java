package d1mene.storage;

import d1mene.data.APIRecord;

import java.io.IOException;
import java.util.List;

public interface FileStorage {
    void save(List<APIRecord> records, WriteMode mode) throws IOException;
    List<APIRecord> readAll() throws IOException;
    List<APIRecord> readBySource(String sourceName) throws IOException;
}