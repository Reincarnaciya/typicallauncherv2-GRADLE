package space.typro.typicallauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import space.typro.typicallauncher.controllers.scenes.subscenes.SettingsController;
import space.typro.typicallauncher.utils.LauncherAlert;
import space.typro.typicallauncher.utils.LogbackConfigurator;

import java.awt.*;
import java.io.IOException;

@Slf4j
public class Main extends Application {
    public static final String LAUNCHER_VERSION = "DEV_BUILD_0";
    private double xOffset;
    private double yOffset;

    public static Stage GLOBAL_STAGE;
    public static void main(String[] args) {


        launch();
    }

    public static void hideLauncher() {
        GLOBAL_STAGE.hide();
    }

    private void generatePopupMenu() {
        java.awt.Image trayIconImage = Toolkit.getDefaultToolkit().getImage(
                ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.IMAGES, "ico.png"));

        if (!SystemTray.isSupported()) {
            log.warn("SystemTray is not supported");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon = getTrayIcon(trayIconImage);

        trayIcon.addActionListener(e -> Platform.runLater(() -> {
            if (GLOBAL_STAGE != null) {
                GLOBAL_STAGE.show();
                GLOBAL_STAGE.toFront();
            }
        }));

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            log.error("TrayIcon could not be added", e);
        }
    }

    private static TrayIcon getTrayIcon(java.awt.Image trayIconImage) {
        PopupMenu popup = new PopupMenu();

        MenuItem openItem = new MenuItem("Show Launcher");
        openItem.addActionListener(e -> Platform.runLater(() -> {
            if (GLOBAL_STAGE != null) {
                GLOBAL_STAGE.show();
                GLOBAL_STAGE.toFront();
            }
        }));

        MenuItem hideItem = new MenuItem("Hide Hide");
        hideItem.addActionListener(e -> Platform.runLater(() -> {
            if (GLOBAL_STAGE != null) {
                GLOBAL_STAGE.hide();
            }
        }));

        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.addActionListener(e -> {
            System.out.println("Settings clicked");
        });

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.addActionListener(e -> Platform.runLater(() ->
                LauncherAlert.create(Alert.AlertType.INFORMATION, String.format("Launcher version: %s. Made by TypicalProject", LAUNCHER_VERSION), ButtonType.OK).showAndWait()));



        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> exit());

        popup.add(openItem);
        popup.add(hideItem);
        popup.addSeparator();
        popup.add(settingsItem);
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(trayIconImage, "TypicalLauncher", popup);
        trayIcon.setImageAutoSize(true);
        return trayIcon;
    }
    public static void exit(){
        Platform.runLater(()->{
            GLOBAL_STAGE.close();
            Platform.exit();
            System.exit(0);
        });
    }

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("scenes/launcher-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("TypicalLauncher");
        stage.setScene(scene);
        stage.setResizable(true);
        //stage.getIcons().add(new Image(ResourceHelper.getResourceByType(ResourceHelper.ResourceFolder.IMAGES, "ico.png")));
        stage.initStyle(StageStyle.TRANSPARENT);
        GLOBAL_STAGE = stage;

        LogbackConfigurator.configure();

        SettingsController.GameSettings.settings.loadSettings();

        Platform.setImplicitExit(false);
        if (SystemTray.isSupported()){
            //generatePopupMenu();
        }


        stage.show();
    }
}
