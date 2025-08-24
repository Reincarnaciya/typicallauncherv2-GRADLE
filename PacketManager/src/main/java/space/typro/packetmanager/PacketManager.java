package space.typro.packetmanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import space.typro.Download.DownloadListener;
import space.typro.Download.DownloadManager;
import space.typro.Download.DownloadTask;
import space.typro.directorymanager.DirectoryManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PacketManager {
    private final DownloadManager downloadManager = new DownloadManager(1);
    private final String serverDownloadUrl = "https://typro.space/api/download";

    public static void main(String[] args) {
        System.err.println(new PacketManager().validateClient("TyTest"));
    }

    public boolean validateClient(String server) {
        File clientDir = new File(DirectoryManager.clientDir.getDir() + File.separator + server);
        File metadataFile = getMetadataFile(server);

        if (metadataFile == null) {
            log.error("Failed to get metadata file for server: {}", server);
            return false;
        }

        try (FileReader r = new FileReader(metadataFile)) {

            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> map = gson.fromJson(r, type);

            @SuppressWarnings("unchecked")
            Map<String, Object> fileMap = (Map<String, Object>) map.get("files");

            CountDownLatch downloadLatch = new CountDownLatch(fileMap.size());
            AtomicInteger downloadCount = new AtomicInteger(0);
            AtomicInteger verificationFailures = new AtomicInteger(0);

            downloadManager.addListener(new DownloadListener() {
                @Override
                public void onDownloadComplete(DownloadTask task) {
                    downloadLatch.countDown();
                    int completed = downloadCount.incrementAndGet();
                    log.info("Download completed: {}/{}", completed, fileMap.size());
                }

                @Override
                public void onDownloadError(DownloadTask task, Exception e) {
                    downloadLatch.countDown();
                    log.error("Download failed: {}", task.getSavePath(), e);
                }
            });

            fileMap.forEach((filePath, fileData) -> {
                String expectedHash = (String) fileData;
                File currentFile = new File(clientDir.getAbsolutePath() + filePath);

                if (currentFile.exists()) {
                    String actualHash = calculateFileHash(currentFile);
                    if (actualHash != null && actualHash.equals(expectedHash)) {
                        log.info("File verified successfully: {}", filePath);
                        downloadLatch.countDown();
                        downloadCount.incrementAndGet();
                        return;
                    } else {
                        log.warn("File hash mismatch for: {}. Expected: {}, Actual: {}. Re-downloading...",
                                filePath, expectedHash, actualHash);
                        if (!currentFile.delete()) {
                            log.error("Failed to delete invalid file: {}", filePath);
                        }
                    }
                }

                new File(currentFile.getParent()).mkdirs();
                String downloadUrl = serverDownloadUrl + "/clients/" + server + "/" + filePath;
                DownloadTask task = new DownloadTask(downloadUrl, currentFile.getAbsolutePath());

                // Добавляем проверку хэша после загрузки
                task.addPostDownloadAction(() -> {
                    String downloadedHash = calculateFileHash(currentFile);
                    if (downloadedHash == null || !downloadedHash.equals(expectedHash)) {
                        log.error("Downloaded file hash mismatch for: {}. Expected: {}, Actual: {}",
                                filePath, expectedHash, downloadedHash);
                        verificationFailures.incrementAndGet();
                        // Удаляем невалидный файл
                        if (!currentFile.delete()) {
                            log.error("Failed to delete invalid downloaded file: {}", filePath);
                        }
                    } else {
                        log.info("Downloaded file verified successfully: {}", filePath);
                    }
                });

                downloadManager.addDownloadTask(task);
                log.info("Added download task: {}", filePath);
            });

            try {
                downloadLatch.await();

                if (verificationFailures.get() > 0) {
                    log.error("Validation failed: {} files had hash mismatches", verificationFailures.get());
                    return false;
                }

                log.info("All files validated successfully!");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Download waiting interrupted", e);
                return false;
            }

        } catch (IOException e) {
            log.error("Error reading metadata file", e);
            return false;
        }

        return true;
    }

    /**
     * Вычисляет SHA-256 хэш файла
     */
    private String calculateFileHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] byteArray = new byte[1024];
                int bytesCount;

                while ((bytesCount = fis.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }
            }

            byte[] bytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Error calculating hash for file: {}", file.getAbsolutePath(), e);
            return null;
        }
    }

    public File getMetadataFile(String server) {
        String fileName = server + ".json";
        File file = new File(DirectoryManager.metadataDir.getDir().getAbsoluteFile() + File.separator + fileName);

        if (downloadFileDirectly(server, fileName)) {
            if (file.exists() && file.length() > 0) {
                log.info("Файл {} успешно загружен: {} bytes",file.getAbsoluteFile(), file.length());
                return file;
            }
        }
        return null;
    }

    private boolean downloadFileDirectly(String server, String fileName) {
        try {
            DownloadTask task = new DownloadTask(
                    "https://typro.space/api/download/metadata/" + fileName,
                    DirectoryManager.metadataDir.getDir().getAbsolutePath() + File.separator + fileName
            );

            log.info("Размер файла на сервере: {} bytes", task.getRemoteFileSize());

            task.download();

            if (task.verifyFileSize()) {
                log.info("✓ Размер файла проверен и совпадает");
                return true;
            } else {
                log.warn("✗ Размер файла не совпадает! Сервер: {}, Локальный: {}", task.getRemoteFileSize(), task.getLocalFileSize());
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to download metadata for {}: {}", server, e.getMessage());
            return false;
        }
    }
}