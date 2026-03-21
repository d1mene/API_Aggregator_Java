package d1mene.data;

import java.util.Map;

public class RawRecord implements APIRecord {

    private final String sourceName;
    private final Map<String, String> fields;

    public RawRecord(String sourceName, Map<String, String> fields) {
        this.sourceName = sourceName;
        this.fields = fields;
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public Map<String, String> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return sourceName + " " + fields;
    }
}