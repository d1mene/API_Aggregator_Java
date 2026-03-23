package d1mene.service;

import d1mene.client.APIClient;
import d1mene.storage.FileStorage;
import d1mene.storage.WriteMode;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PullingManager {
    private final ThreadPoolExecutor executor;
    private final ScheduledExecutorService dispatcher;
    private final FileStorage storage;
    private final Map<String, Map<String, String>> paramsPerClient;
    private final long intervalSeconds;
    private final WriteMode writeMode;

    private final ConcurrentHashMap<String, Long> lastFinishedTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> runningTasks = new ConcurrentHashMap<>();

    @Getter
    private volatile boolean active = false;

    public PullingManager(int n, long t, FileStorage storage,
                          Map<String, Map<String, String>> paramsPerClient,
                          WriteMode writeMode) {
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(n);
        this.dispatcher = Executors.newSingleThreadScheduledExecutor();
        this.storage = storage;
        this.paramsPerClient = paramsPerClient;
        this.intervalSeconds = t;
        this.writeMode = writeMode;
    }

    public void start(List<APIClient> clients) {
        active = true;
        dispatcher.scheduleWithFixedDelay(() -> dispatch(clients), 0, 1, TimeUnit.SECONDS);
        System.out.println("Опрос запущен.");
    }

    public void stop() {
        active = false;
        dispatcher.shutdown();
        try {
            if (!dispatcher.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Диспатчер не завершился за 5 секунд");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Опрос остановлен.");
    }

    public void onTaskFinished(String sourceName) {
        lastFinishedTime.put(sourceName, System.currentTimeMillis());
        runningTasks.put(sourceName, false);
    }

    private void dispatch(List<APIClient> clients) {
        if (!active) {
            return;
        }

        long now = System.currentTimeMillis();

        for (APIClient client : clients) {
            String sourceName = client.getSourceName();

            boolean isRunning = runningTasks.getOrDefault(sourceName, false);
            if (isRunning) {
                continue;
            }

            long lastFinished = lastFinishedTime.getOrDefault(sourceName, 0L);
            long elapsed = (now - lastFinished) / 1000;
            if (elapsed < intervalSeconds) {
                continue;
            }

            if (executor.getActiveCount() >= executor.getMaximumPoolSize()) {
                continue;
            }

            runningTasks.put(sourceName, true);
            Map<String, String> params = paramsPerClient.getOrDefault(sourceName, Map.of());
            executor.submit(new APIPullTask(client, params, storage, this, writeMode));
        }
    }

}