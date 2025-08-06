package space.typro.typicallauncher.managers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DownloadTask {
    private final String fileUrl;
    private final Path outputPath;
    private volatile boolean paused = false;
    private volatile boolean cancelled = false;
    private long downloadedBytes = 0;
    private long totalFileSize = -1;
    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB

    public DownloadTask(String fileUrl, Path outputPath) {
        this.fileUrl = fileUrl;
        this.outputPath = outputPath;

        try {
            Files.createDirectories(outputPath.getParent());

            // Чтение сохраненного прогресса
            Path progressFile = outputPath.resolveSibling(outputPath.getFileName() + ".progress");
            if (Files.exists(progressFile)) {
                String[] progress = Files.readString(progressFile).split(",");
                if (progress.length == 2) {
                    downloadedBytes = Long.parseLong(progress[0]);
                    totalFileSize = Long.parseLong(progress[1]);
                }
            }

            if (Files.exists(outputPath)) {
                long actualSize = Files.size(outputPath);
                if (actualSize != downloadedBytes) {
                    downloadedBytes = actualSize;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void download() throws IOException {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        RandomAccessFile outputFile = null;

        try {
            URL url = new URI(fileUrl).toURL();
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("HEAD");
            totalFileSize = connection.getContentLengthLong();
            //connection.disconnect();
            //connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем диапазон для докачки
            if (downloadedBytes > 0) {
                if (downloadedBytes >= totalFileSize) {
                    Files.deleteIfExists(outputPath);
                    downloadedBytes = 0;
                } else {
                    connection.setRequestProperty("Range", "bytes=" + downloadedBytes + "-");
                }
            }

            connection.connect();

            // Проверяем код ответа
            int responseCode = connection.getResponseCode();
            if (!(responseCode == HttpURLConnection.HTTP_OK ||
                    responseCode == HttpURLConnection.HTTP_PARTIAL)) {
                throw new IOException("Server returned HTTP code: " + responseCode);
            }

            if (downloadedBytes > 0 && responseCode == HttpURLConnection.HTTP_OK) {
                downloadedBytes = 0;
                Files.deleteIfExists(outputPath);
            }

            inputStream = connection.getInputStream();
            outputFile = new RandomAccessFile(outputPath.toFile(), "rw");
            outputFile.seek(downloadedBytes);

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1 && !paused && !cancelled) {
                outputFile.write(buffer, 0, bytesRead);
                downloadedBytes += bytesRead;

                updateProgress();
            }

            if (paused) {
                saveProgress();
            } else if (!cancelled && downloadedBytes == totalFileSize) {
                onDownloadComplete();
            }

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) inputStream.close();
            if (outputFile != null) outputFile.close();
            if (connection != null) connection.disconnect();
        }
    }

    private void updateProgress() {
        if (totalFileSize > 0) {
            double progress = (double) downloadedBytes / totalFileSize * 100;
            System.out.printf("Downloaded: %.2f%% of %d bytes%n", progress, totalFileSize);
        } else {
            System.out.printf("Downloaded: %d bytes (total size unknown)%n", downloadedBytes);
        }
    }

    private void onDownloadComplete() {
        System.out.println("Download completed successfully!");
        // Здесь можно добавить проверку целостности файла
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void cancel() {
        cancelled = true;
        try {
            Files.deleteIfExists(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveProgress() {
        try {
            Path progressFile = outputPath.resolveSibling(outputPath.getFileName() + ".progress");
            Files.writeString(progressFile, downloadedBytes + "," + totalFileSize,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}