package space.typro.typicallauncher.controllers;

import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.utils.LauncherAlert;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public abstract class BaseController implements Initializable {

    public void initialize() {
        log.info("Initializing {}", this.getClass());
    }
    public abstract void initialize(URL url, ResourceBundle resourceBundle);

    protected void showErrorAlert(String message) {
        new LauncherAlert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}