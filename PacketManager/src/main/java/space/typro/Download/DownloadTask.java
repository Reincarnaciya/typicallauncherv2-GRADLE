package space.typro.Download;

import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DownloadTask {
    private final String fileUrl;
    @Getter
    private final String savePath;
    private final AtomicInteger progress = new AtomicInteger(-1);
    @Getter
    private long remoteFileSize = -1;
    @Getter
    private long localFileSize = -1;
    @Getter
    private boolean sizeVerified = false;
    private final List<Runnable> postDownloadActions = new ArrayList<>();
    private final List<Consumer<Exception>> errorHandlers = new ArrayList<>();

    public DownloadTask(String fileUrl, String savePath) {
        this.fileUrl = fileUrl;
        this.savePath = savePath;
    }

    public void addPostDownloadAction(Runnable action) {
        postDownloadActions.add(action);
    }

    public void addErrorHandler(Consumer<Exception> handler) {
        errorHandlers.add(handler);
    }

    public void download() throws IOException, URISyntaxException {
        URL url = new URI(fileUrl.replace(" ", "%20")).toURL();

        // Открываем соединение с явными заголовками
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Accept", "application/octet-stream");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.connect();

        remoteFileSize = connection.getContentLengthLong();
        if (getRemoteFileSize() < 0) {
            throw new IOException("Файл не найден на сервере");
        }

        // Проверяем существующий локальный файл
        File localFile = new File(savePath);
        if (localFile.exists()) {
            localFileSize = localFile.length();

            if (localFileSize == remoteFileSize && remoteFileSize > 0) {
                progress.set(100);
                sizeVerified = true;
                executePostDownloadActions();
                return;
            }
        }

        try (var inputStream = new BufferedInputStream(url.openStream());
             var outputStream = new FileOutputStream(savePath)) {

            byte[] buffer = new byte[8192];
            long totalRead = 0;
            int bytesRead;

            // В методе download() замените:
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                progress.set(calculateProgress(totalRead, remoteFileSize));
            }
            outputStream.flush(); // Гарантируем запись всех данных


            File downloadedFile = new File(savePath);
            long actualSize = downloadedFile.length();
            if (actualSize != remoteFileSize) {
                throw new IOException(String.format(
                        "File size mismatch after download! Expected: %d, Actual: %d",
                        remoteFileSize, actualSize
                ));
            }

            verifyFileSize();
            executePostDownloadActions();
        } catch (Exception e) {
            executeErrorHandlers(e);
            throw e;
        }
    }

    private void executePostDownloadActions() {
        for (Runnable action : postDownloadActions) {
            try {
                action.run();
            } catch (Exception e) {
                System.err.println("Error executing post-download action: " + e.getMessage());
            }
        }
    }

    private void executeErrorHandlers(Exception e) {
        for (Consumer<Exception> handler : errorHandlers) {
            try {
                handler.accept(e);
            } catch (Exception ex) {
                System.err.println("Error executing error handler: " + ex.getMessage());
            }
        }
    }

    public boolean verifyFileSize() {
        File downloadedFile = new File(savePath);
        localFileSize = downloadedFile.exists() ? downloadedFile.length() : -1;
        sizeVerified = (localFileSize == remoteFileSize && remoteFileSize > 0);
        return sizeVerified;
    }

    public int getProgress() {
        return progress.get();
    }


    private int calculateProgress(long downloaded, long total) {
        return total > 0 ? (int) ((downloaded * 100) / total) : 0;
    }

    public String getSizeVerificationStatus() {
        if (!sizeVerified) {
            return "Размер не проверен";
        }
        return String.format("Размер совпадает: %d bytes", localFileSize);
    }
}