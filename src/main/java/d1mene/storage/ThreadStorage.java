package d1mene.storage;

import d1mene.data.APIRecord;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ThreadStorage implements FileStorage {

    private final FileStorage delegate;
    private final BlockingQueue<List<APIRecord>> queue = new LinkedBlockingQueue<>();
    private final Thread writerThread;
    private volatile boolean running = true;

    public ThreadStorage(FileStorage delegate) {
        this.delegate = delegate;
        this.writerThread = new Thread(this::writerLoop, "storage-writer");
        this.writerThread.setDaemon(true);
        this.writerThread.start();
    }

    @Override
    public void save(List<APIRecord> records, WriteMode mode) throws IOException {
        try {
            queue.put(records);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Запись прервана: " + e.getMessage());
        }
    }

    @Override
    public List<APIRecord> readAll() throws IOException {
        return delegate.readAll();
    }

    @Override
    public List<APIRecord> readBySource(String sourceName) throws IOException {
        return delegate.readBySource(sourceName);
    }

    public void shutdown() {
        running = false;
        writerThread.interrupt();
        try {
            writerThread.join(10009);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void writerLoop() {
        while (running || !queue.isEmpty()) {
            try {
                List<APIRecord> records = queue.poll(1, TimeUnit.SECONDS);
                if (records != null) {
                    delegate.save(records, WriteMode.APPEND);
                }
            } catch (InterruptedException ignored) {}
            catch (IOException e) {
                System.err.println("Ошибка записи в файл: " + e.getMessage());
            }
        }
    }
}