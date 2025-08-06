package space.typro.typicallauncher.managers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Slf4j
public class DirManager {

    public static final DirManager launcherDir = new DirManager("TypicalLauncher");
    public static final DirManager logDir = new DirManager(launcherDir, "logs");
    public static final DirManager assetsDir = new DirManager(launcherDir, "assets");

    private final File dir;

    /**
     * Создает папку относительно родительской директории
     */
    public DirManager(DirManager parent, String dirName) {
        this(parent.getDir().toPath().resolve(dirName).toString());
    }

    /**
     * Создает папку в системной директории по умолчанию
     */
    public DirManager(String dirName) {
        Path basePath = getBasePath();
        Path resolvedPath = basePath.resolve(dirName);
        this.dir = createDirectory(resolvedPath);
    }

    public static void deleteAllFilesInFolder(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteAllFilesInFolder(f);
                }
            }
        }

        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            log.error("Failed to delete file: {}", file.getAbsolutePath(), e);
        }
    }

    private Path getBasePath() {
        String userHome = System.getProperty("user.home", ".");

        return switch (UserPC.USER_OS) {
            case WINDOWS -> {
                String appData = System.getenv("APPDATA");
                if (appData != null) {
                    yield Paths.get(appData);
                } else {
                    log.warn("APPDATA not found, using user.home: {}", userHome);
                    yield Paths.get(userHome);
                }
            }
            case LINUX -> Paths.get(userHome, ".local", "share");
            case MACOS -> Paths.get(userHome, "Library", "Application Support");
            default -> Paths.get(userHome);
        };
    }

    private File createDirectory(Path path) {
        try {
            Files.createDirectories(path);
            return path.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + path, e);
        }
    }

    public void openInExplorer() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir);
            } else {
                openFallback();
            }
        } catch (IOException e) {
            log.error("Failed to open directory", e);
            openFallback();
        }
    }

    private void openFallback() {
        String os = UserPC.USER_OS.name().toLowerCase();
        String command = switch (UserPC.USER_OS) {
            case WINDOWS -> "explorer";
            case LINUX -> "xdg-open";
            case MACOS -> "open";
            default -> null;
        };

        if (command == null) {
            log.error("Unsupported OS: {}", os);
            return;
        }

        try {
            new ProcessBuilder(command, dir.getAbsolutePath()).start();
        } catch (IOException e) {
            log.error("Failed to open directory with fallback method", e);
        }
    }

    public String getName() {
        return dir.getName();
    }

    @Override
    public String toString() {
        return dir.toString();
    }
}