package space.typro.typicallauncher.controllers.scenes.subscenes;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import space.typro.typicallauncher.controllers.BaseController;
import space.typro.typicallauncher.controllers.Server;
import space.typro.typicallauncher.models.ServerCardUI;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PlayController extends BaseController {


    public VBox serversContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<HBox> serversContainers = new ArrayList<>();

        serversContainers.add(createServerCard(new Server("TyMMO", "Massive multiplayer online experience", Server.ServerStatus.ONLINE, 500, 200)));
        serversContainers.add(createServerCard(new Server("TyKoban", "Competitive gameplay server", Server.ServerStatus.MAINTENANCE, 100, 0)));
        serversContainers.add(createServerCard(new Server("TySeryoga", "Massive multiplayer online experience", Server.ServerStatus.OFFLINE, 20, 0)));
        serversContainers.add(createServerCard(new Server("TyBLYEA", "Massive multiplayer online experience", Server.ServerStatus.STARTING, 300, 0)));
        serversContainers.add(createServerCard(new Server("TyPOZDA", "Massive multiplayer online experience", Server.ServerStatus.WHITELIST, 111, 10)));

        serversContainer.getChildren().addAll(serversContainers);
    }


    private HBox createServerCard(Server server) {
        ServerCardUI ui = new ServerCardUI();

        HBox serverCard = new HBox();
        ui.container = serverCard;
        serverCard.getStyleClass().add("server-card");

        VBox serverInfo = new VBox();
        serverInfo.getStyleClass().add("server-info");
        HBox.setHgrow(serverInfo, Priority.ALWAYS);

        // Название сервера
        Label nameLabel = new Label(server.getName());
        nameLabel.getStyleClass().add("server-name");

        // Описание сервера
        Label descLabel = new Label(server.getDescription());
        descLabel.getStyleClass().add("server-description");

        // Статус и игроки
        HBox statusBox = new HBox();
        statusBox.getStyleClass().add("status-box");

        ui.statusLabel = new Label(server.getStatus().getDisplayName());
        ui.statusLabel.getStyleClass().add(server.getStatus().getStyleClass());

        ui.playersLabel = new Label(server.getCurrentPlayers() + "/" + server.getMaxPlayers() + " players");
        ui.playersLabel.getStyleClass().add("player-count");

        statusBox.getChildren().addAll(ui.statusLabel, ui.playersLabel);

        // Кнопка подключения
        ui.actionButton = new Button();
        server.updateActionButton(ui.actionButton);

        // Добавляем все элементы
        serverInfo.getChildren().addAll(nameLabel, descLabel, statusBox);
        serverCard.getChildren().addAll(serverInfo, ui.actionButton);

        server.setUI(ui);
        return serverCard;
    }


}
