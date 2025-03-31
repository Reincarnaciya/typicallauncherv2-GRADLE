package space.typro.typicallauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.controllers.scenes.subscenes.SettingsController;
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

    private void generatePopupMenu() { //TODO Переписать к хуям всю эту хуйню

    }

    public static void exit(){
        GLOBAL_STAGE.close();
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("scenes/launcher-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        /*
            Делаем так, чтоб окно можно было передвигать в любой точке
        */
        scene.setOnMousePressed(event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });
        scene.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });
        stage.setTitle("TypicalLauncher");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(ResourceHelper.getResourceByType(ResourceHelper.ResourceFolder.IMAGES, "ico.png")));
        stage.initStyle(StageStyle.TRANSPARENT);
        GLOBAL_STAGE = stage;

        LogbackConfigurator.configure();

        SettingsController.GameSettings.settings.loadSettings();

        Platform.setImplicitExit(false); // Делаем, чтоб приложение не клоузалось при скрытие всех пэйнов
        if (SystemTray.isSupported()){
            generatePopupMenu();
        }


        stage.show();
    }
}
