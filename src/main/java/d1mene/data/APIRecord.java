package d1mene.data;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public interface APIRecord {
    String getSourceName();

    default Map<String, String> getFields() {
        Gson gson = new Gson();
        JsonObject json = gson.toJsonTree(this).getAsJsonObject();
        Map<String, String> fields = new LinkedHashMap<>();
        flattenJson(getSourceName().toLowerCase(), json, fields);
        return fields;
    }

    private static void flattenJson(String prefix, JsonObject json, Map<String, String> result) {
        for (Map.Entry<String, JsonElement> entry: json.entrySet()) {
            String key = prefix + "_" + entry.getKey();
            JsonElement val = entry.getValue();
            if (val.isJsonObject()) {
                flattenJson(key, val.getAsJsonObject(), result);
            }
            else if (val.isJsonNull()) {
                result.put(key, "");
            }
            else {
                result.put(key, val.getAsString());
            }
        }
    }
}