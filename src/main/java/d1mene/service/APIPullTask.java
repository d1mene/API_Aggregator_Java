package d1mene.service;

import com.google.gson.JsonSyntaxException;
import d1mene.client.APIClient;
import d1mene.data.APIRecord;
import d1mene.storage.FileStorage;
import d1mene.storage.WriteMode;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class APIPullTask implements Runnable {

    private final APIClient client;
    private final Map<String, String> params;
    private final FileStorage storage;
    private final PullingManager manager;
    private final WriteMode writeMode;

    public APIPullTask(APIClient client, Map<String, String> params,
                       FileStorage storage, PullingManager manager,
                       WriteMode writeMode) {
        this.client = client;
        this.params = params;
        this.storage = storage;
        this.manager = manager;
        this.writeMode = writeMode;
    }

    @Override
    public void run() {
        String sourceName = client.getSourceName();
        try {
            AggregatorService aggregator = new AggregatorService();
            Map<String, List<APIRecord>> result = aggregator.aggregate(
                    List.of(client),
                    Map.of(sourceName, params)
            );

            List<APIRecord> records = result.getOrDefault(sourceName, List.of());

            if (!records.isEmpty()) {
                storage.save(records, writeMode);
                System.out.println("[" + sourceName + "] Получено и сохранено записей: " + records.size());
            }

        } catch (IOException e) {
            System.err.println("[" + sourceName + "] Ошибка IO: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("[" + sourceName + "] Ошибка JSON: " + e.getMessage());
        }
        finally
        {
            manager.onTaskFinished(sourceName);
        }
    }
}