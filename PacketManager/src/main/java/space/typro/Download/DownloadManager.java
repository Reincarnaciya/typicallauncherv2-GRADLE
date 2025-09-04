package space.typro.Download;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DownloadManager {
    private final Queue<DownloadTask> downloadQueue = new LinkedList<>();
    private final ExecutorService executor;
    private final List<DownloadListener> listeners = new ArrayList<>();
    private final ConcurrentMap<DownloadTask, Boolean> activeDownloads = new ConcurrentHashMap<>();
    private final int maxConcurrentDownloads;
    private final AtomicInteger totalTasks = new AtomicInteger(0);
    private final AtomicInteger completedTasks = new AtomicInteger(0);

    public DownloadManager() {
        this(1);
    }

    public DownloadManager(int maxConcurrentDownloads) {
        this.maxConcurrentDownloads = maxConcurrentDownloads;
        this.executor = Executors.newFixedThreadPool(maxConcurrentDownloads);
    }

    public void addDownloadTask(DownloadTask task) {
        synchronized (downloadQueue) {
            downloadQueue.add(task);
            totalTasks.incrementAndGet();
            startDownloads();
        }
    }

    public void addListener(DownloadListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeAllListener() {
        synchronized (listeners) {
            listeners.clear();
        }
    }

    private void notifyDownloadStart(DownloadTask task) {
        synchronized (listeners) {
            listeners.forEach(l -> l.onDownloadStart(task));
        }
    }

    private void notifyProgressUpdate(DownloadTask task, int progress) {
        synchronized (listeners) {
            listeners.forEach(l -> l.onProgressUpdate(task, progress));
        }
    }

    private void notifyDownloadComplete(DownloadTask task) {
        synchronized (listeners) {
            listeners.forEach(l -> l.onDownloadComplete(task));
        }
    }

    private void notifyDownloadError(DownloadTask task, Exception e) {
        synchronized (listeners) {
            listeners.forEach(l -> l.onDownloadError(task, e));
        }
    }

    private void notifyAllDownloadsCompleted() {
        synchronized (listeners) {
            listeners.forEach(DownloadListener::onAllDownloadsCompleted);
        }
    }

    private void startDownloads() {
        synchronized (downloadQueue) {
            while (activeDownloads.size() < maxConcurrentDownloads && !downloadQueue.isEmpty()) {

                DownloadTask task = downloadQueue.poll();
                activeDownloads.put(task, true);
                notifyDownloadStart(task);

                executor.execute(() -> {
                    try {
                        task.download();
                        if (task.verifyFileSize()) {
                            notifyDownloadComplete(task);
                        } else {
                            throw new IOException("File size mismatch! Remote: " +
                                    task.getRemoteFileSize() + ", Local: " + task.getLocalFileSize() + "\n Path: " + task.getSavePath());
                        }
                    } catch (IOException | URISyntaxException e) {
                        notifyDownloadError(task, e);
                        log.error("Download failed: {}", e.getMessage());
                    } finally {
                        activeDownloads.remove(task);
                        completedTasks.incrementAndGet();

                        checkAllDownloadsCompleted();

                        startDownloads();
                    }
                });

                new Thread(() -> {
                    while (activeDownloads.containsKey(task)) {
                        notifyProgressUpdate(task, task.getProgress());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }).start();
            }
        }
    }

    private void checkAllDownloadsCompleted() {
        synchronized (downloadQueue) {
            if (completedTasks.get() == totalTasks.get() && activeDownloads.isEmpty() && downloadQueue.isEmpty()) {
                notifyAllDownloadsCompleted();
            }
        }
    }

    public void shutdown() {
        executor.shutdown();
    }

    public int getTotalTasks() {
        return totalTasks.get();
    }

    public int getCompletedTasks() {
        return completedTasks.get();
    }

    public int getActiveDownloadsCount() {
        return activeDownloads.size();
    }

    public int getQueuedTasksCount() {
        synchronized (downloadQueue) {
            return downloadQueue.size();
        }
    }
}