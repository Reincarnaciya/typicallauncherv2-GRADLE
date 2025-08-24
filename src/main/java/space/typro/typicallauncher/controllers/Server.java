package space.typro.typicallauncher.controllers;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.models.ServerCardUI;

import java.net.http.HttpClient;

@Slf4j
@Getter
public class Server {
    private final String name;
    private final String description;
    private ServerStatus status;
    private int currentPlayers;
    private final int maxPlayers;
    private ServerCardUI ui;

    public Server(String name, String description, ServerStatus status, int maxPlayers, int currentPlayers) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
    }
    public void setUI(ServerCardUI ui) {
        this.ui = ui;
        updateUI(); // Обновляем UI при первом присвоении
    }

    public void updateStatus(ServerStatus newStatus, int newPlayerCount) {
        this.status = newStatus;
        this.currentPlayers = newPlayerCount;
        updateUI();
    }

    private void updateUI() {
        if (ui != null) {
            Platform.runLater(() -> {
                ui.statusLabel.setText(status.getDisplayName());
                ui.statusLabel.getStyleClass().clear();
                ui.statusLabel.getStyleClass().add(status.getStyleClass());

                ui.playersLabel.setText(currentPlayers + "/" + maxPlayers + " players");

                updateActionButton(ui.actionButton);
            });
        }
    }

    public void updateActionButton(Button button) {
        button.getStyleClass().clear();

        switch (status) {
            case ONLINE:
                button.setText("Connect");
                button.getStyleClass().add("connect-button");
                button.setDisable(false);
                button.setOnAction(e -> connectToServer());
                break;
            case MAINTENANCE:
            case OFFLINE:
            case STARTING:
                button.setText(status.getDisplayName());
                button.getStyleClass().add("maintenance-button");
                button.setDisable(true);
                break;
            case WHITELIST:
                button.setText("Join (Whitelist)");
                button.getStyleClass().add("connect-button");
                button.setDisable(false);
                button.setOnAction(e -> connectToServer());
                break;
        }
    }

    private void connectToServer() {
        log.info("Connecting to server {}", name);


        
    }

    @Getter
    public enum ServerStatus {
        ONLINE("online", "status-online", "#4ade80"),
        MAINTENANCE("maintenance", "status-maintenance", "#facc15"),
        OFFLINE("offline", "status-offline", "#ef4444"),
        STARTING("starting", "status-starting", "#f59e0b"),
        WHITELIST("whitelist", "status-whitelist", "#6366f1");

        private final String displayName;
        private final String styleClass;
        private final String color;

        ServerStatus(String displayName, String styleClass, String color) {
            this.displayName = displayName;
            this.styleClass = styleClass;
            this.color = color;
        }

    }
}
