package space.typro.typicallauncher.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.ResourceHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class LauncherAlert extends Alert {
    private final Stage stage;
    private double xOffset;
    private double yOffset;

    // Приватный конструктор (только для FX-потока)
    private LauncherAlert(AlertType alertType, String content, ButtonType... buttonTypes) {
        super(alertType, content, buttonTypes); // SAFE: вызывается только в FX-потоке

        this.getDialogPane().getStylesheets().setAll(
                ResourceHelper.getResourceByType(ResourceHelper.ResourceFolder.ROOT, "alert.css")
        );

        stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.TRANSPARENT);

        setHeaderText(getHeaderTextByType(alertType));

        stage.getScene().setOnMousePressed(event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });

        stage.getScene().setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });
    }

    // Фабричный метод для создания в любом потоке
    public static LauncherAlert create(AlertType alertType, String content, ButtonType... buttonTypes) {
        if (alertType == null || content == null) {
            throw new IllegalArgumentException("AlertType and content cannot be null");
        }

        if (Platform.isFxApplicationThread()) {
            return new LauncherAlert(alertType, content, buttonTypes);
        } else {
            AtomicReference<LauncherAlert> alertRef = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                alertRef.set(new LauncherAlert(alertType, content, buttonTypes));
                latch.countDown();
            });

            try {
                latch.await();
                return alertRef.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Alert creation interrupted", e);
            }
        }
    }

    private String getHeaderTextByType(AlertType alertType) {
        return switch (alertType) {
            case ERROR -> "Ошибка";
            case CONFIRMATION -> "Подтверждение действия";
            case WARNING -> "Предупреждение";
            case INFORMATION -> "Информация";
            case NONE -> "";
        };
    }
}