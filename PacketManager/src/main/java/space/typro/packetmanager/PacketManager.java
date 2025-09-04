package space.typro.packetmanager;



import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import space.typro.Download.DownloadListener;
import space.typro.Download.DownloadManager;
import space.typro.Download.DownloadTask;
import space.typro.directorymanager.DirectoryManager;


import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PacketManager {
    private final DownloadManager downloadManager = new DownloadManager(1);
    private final String serverDownloadUrl = "https://typro.space/api/download";



    public boolean validateClient(String server) throws Exception {
        return downloadClient(server);
    }



    private boolean downloadClient(String server) throws Exception {

        File clientDir = new File(DirectoryManager.clientDir.getDir() + File.separator + server);
        File metadataFile = getMetadataFile(server);

        if (metadataFile == null) {
            throw new Exception("Failed to get metadata file for server: " + server);
        }

        try (FileReader r = new FileReader(metadataFile)) {

            Map<String, Object> map = new ObjectMapper().readValue(metadataFile, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            Map<String, Object> fileMap = (Map<String, Object>) map.get("files");

            CountDownLatch downloadLatch = new CountDownLatch(fileMap.size());
            AtomicInteger downloadCount = new AtomicInteger(0);
            AtomicInteger verificationFailures = new AtomicInteger(0);

            addDownloadListener(new DownloadListener() {
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
                File currentFile;

                if (isAssets(filePath)) {
                    currentFile = new File(DirectoryManager.assetsDir.getDir().getParentFile(), filePath);
                    log.debug("Assets file: {} -> {}", filePath, currentFile.getAbsolutePath());
                } else if (isJre(filePath)) {
                    currentFile = new File(DirectoryManager.launcherDir.getDir(), filePath);
                    log.debug("JRE file: {} -> {}", filePath, currentFile.getAbsolutePath());
                } else {
                    String normalizedPath = filePath.startsWith("/") ? filePath : "/" + filePath;
                    currentFile = new File(clientDir, normalizedPath);
                    log.debug("Client file: {} -> {}", filePath, currentFile.getAbsolutePath());
                }

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

                String downloadUrl;

                if (isJre(filePath) || isAssets(filePath)) downloadUrl = serverDownloadUrl + "/" + filePath;
                else downloadUrl = serverDownloadUrl + "/clients/" + server + "/" + filePath;

                DownloadTask task = new DownloadTask(downloadUrl, currentFile.getAbsolutePath());

                task.addPostDownloadAction(() -> {
                    String downloadedHash = calculateFileHash(currentFile);
                    if (downloadedHash == null || !downloadedHash.equals(expectedHash)) {
                        log.error("Downloaded file hash mismatch for: {}. Expected: {}, Actual: {}",
                                filePath, expectedHash, downloadedHash);
                        verificationFailures.incrementAndGet();

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

    private boolean isAssets(String filePath){
        return filePath.startsWith("assets");
    }
    private boolean isJre(String filePath){
        return filePath.startsWith("jre");
    }


    public void addDownloadListener(DownloadListener listener){
        downloadManager.addListener(listener);
    }

    /**
     * Вычисляет SHA-256 хэш файла
     */
    private String calculateFileHash(File file) {
        try (InputStream is = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;

            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }

            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Error calculating hash for file: {}", file.getAbsolutePath(), e);
            return null;
        }
    }

    public File getMetadataFile(String server) throws Exception {
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

    private boolean downloadFileDirectly(String server, String fileName) throws Exception {
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
            throw new Exception(String.format("Failed to download metadata for %s: %s", server, e.getMessage()));
        }
    }
}