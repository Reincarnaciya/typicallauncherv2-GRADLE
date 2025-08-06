package space.typro.typicallauncher.managers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {
    private final ConcurrentLinkedQueue<DownloadTask> downloadQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isRunning = false;
    private DownloadTask currentTask;

    public void addDownloadTask(DownloadTask task) {
        downloadQueue.add(task);
        if (!isRunning) {
            startDownloading();
        }
    }

    private void startDownloading() {
        isRunning = true;
        executorService.execute(() -> {
            while (!downloadQueue.isEmpty()) {
                currentTask = downloadQueue.poll();
                try {
                    currentTask.download();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Можно добавить логику повторной попытки
                }
            }
            isRunning = false;
            currentTask = null;
        });
    }

    public void pauseCurrentDownload() {
        if (currentTask != null) {
            currentTask.pause();
        }
    }

    public void resumeDownloads() {
        if (!isRunning && !downloadQueue.isEmpty()) {
            startDownloading();
        }
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}