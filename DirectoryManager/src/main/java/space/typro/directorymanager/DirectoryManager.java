package space.typro.directorymanager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import space.typro.common.UserPC;


@Slf4j
@Getter
public class DirectoryManager {

    public static final DirectoryManager launcherDir = new DirectoryManager("TypicalLauncher");
    public static final DirectoryManager logDir = new DirectoryManager(launcherDir, "logs");
    public static final DirectoryManager assetsDir = new DirectoryManager(launcherDir, "assets");
    public static final DirectoryManager metadataDir = new DirectoryManager(launcherDir, "metadata");
    public static final DirectoryManager clientDir = new DirectoryManager(launcherDir, "clients");

    private final File dir;

    /**
     * Создает папку относительно родительской директории
     */
    public DirectoryManager(DirectoryManager parent, String dirName) {
        this(parent.getDir().toPath().resolve(dirName).toString());
    }

    /**
     * Создает папку в системной директории по умолчанию
     */
    public DirectoryManager(String dirName) {
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
            e.printStackTrace();
        }
    }

    private Path getBasePath() {
        String userHome = System.getProperty("user.home", ".");

        return switch (UserPC.USER_OS) {
            case UserPC.OS.WINDOWS -> {
                String appData = System.getenv("APPDATA");
                if (appData != null) {
                    yield Paths.get(appData);
                } else {
                    yield Paths.get(userHome);
                }
            }
            case UserPC.OS.LINUX -> Paths.get(userHome, ".local", "share");
            case UserPC.OS.MACOS -> Paths.get(userHome, "Library", "Application Support");
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
            e.printStackTrace();
            openFallback();
        }
    }

    private void openFallback() {
        String command = switch (UserPC.USER_OS) {
            case UserPC.OS.WINDOWS -> "explorer";
            case UserPC.OS.LINUX -> "xdg-open";
            case UserPC.OS.MACOS -> "open";
            default -> null;
        };

        if (command == null) {
            return;
        }

        try {
            new ProcessBuilder(command, dir.getAbsolutePath()).start();
        } catch (IOException e) {
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