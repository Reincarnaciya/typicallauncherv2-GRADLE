package space.typro.typicallauncher.managers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import space.typro.Download.DownloadListener;
import space.typro.Download.DownloadTask;
import space.typro.directorymanager.DirectoryManager;
import space.typro.packetmanager.PacketManager;
import space.typro.typicallauncher.controllers.Server;
import space.typro.typicallauncher.models.ServerCardUI;
import space.typro.typicallauncher.utils.LauncherAlert;
import space.typro.typicallauncher.utils.NodeUtil;

import java.nio.file.Paths;


@Slf4j
public class StartManager {
    private static final PacketManager packetManager = new PacketManager();

    /**
     * Какой-либо из серверов запускается
     */
    private static boolean isStarting = false;
    private static Server startingServer;


    public static void start(Server s) {
        if (isStarting) {
            log.info("Current starting {}", startingServer);
            return;
        }
        isStarting = true;
        startingServer = s;

        packetManager.addDownloadListener(generateListener(s.getUi()));
        new Thread(()->{
            try {
                if (packetManager.validateClient(s.getName())) {
                    log.info("Server {} has correct. Starting game..", s.getName());
                    //TODO: Реализовать запуск через античит
                }else {
                    Platform.runLater(()-> LauncherAlert.create(Alert.AlertType.ERROR, "Не удалось проверить целостность файлов клиента", ButtonType.OK));
                    log.error("Client has not validate correctly");
                }
            } catch (Exception e) {
                Platform.runLater(()-> LauncherAlert.create(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait());
            }
        }).start();





    }

    private static DownloadListener generateListener(ServerCardUI ui) {
        Label startStatusLabel = ui.startStatusLabel;
        return new DownloadListener() {
            @Override
            public void onDownloadStart(DownloadTask task) {
                Platform.runLater(()-> startStatusLabel.setText("Начинаем загрузку клиента.."));

            }

            @Override
            public void onProgressUpdate(DownloadTask task, int progress) {
                Platform.runLater(()-> {
                            String fileName = Paths.get(task.getSavePath()).getFileName().toString();
                            fileName = fileName.substring(0, fileName.length() - 4);
                            startStatusLabel.setText(String.format("Скачиваю: %s, %s %%", fileName, progress));
                        }
                );
            }

            @Override
            public void onDownloadError(DownloadTask task, Exception e) {
                Platform.runLater(()->{
                    LauncherAlert.create(Alert.AlertType.ERROR, "Ошибка загрузки: " + e.getMessage(), ButtonType.OK).showAndWait();
                });
            }

            @Override
            public void onAllDownloadsCompleted() {
                Platform.runLater(()-> startStatusLabel.setText("Клиент запущен"));
                isStarting = false;
                DirectoryManager.deleteAllFilesInFolder(DirectoryManager.clientDir.getDir());
            }
        };
    }


}
