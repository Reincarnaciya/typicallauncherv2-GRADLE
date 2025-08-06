package space.typro.typicallauncher.controllers.scenes;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.Main;
import space.typro.typicallauncher.ResourceHelper;
import space.typro.typicallauncher.controllers.BaseController;

import java.net.URI;
import java.net.URL;
import java.util.*;

@Slf4j
public class LauncherController extends BaseController {
    private static LauncherController instance;

    public Pane contentPane;
    public Label sceneTitle;
    public StackPane settingsButton;
    public StackPane newsButton;

    public StackPane playButton;
    public StackPane messageButton;
    public StackPane contactButton;
    public StackPane forumButton;
    public StackPane profileButton;

    private double xOffset = 0;
    private double yOffset = 0;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    public void loadContent(Subscenes subscenes) {
        try {
            sceneTitle.setText(subscenes.title + " - " + "TyLauncher");


            FXMLLoader fxmlLoader = new FXMLLoader(subscenes.fxmlUrl);
            Parent content = fxmlLoader.load();

            contentPane.getChildren().setAll(content);

            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }


    public void handleWindowPress(MouseEvent mouseEvent) {
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }

    public void handleWindowDrag(MouseEvent mouseEvent) {
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

        stage.setX(mouseEvent.getScreenX() - xOffset);
        stage.setY(mouseEvent.getScreenY() - yOffset);
    }

    public void handleMinimize(ActionEvent actionEvent) {
        Main.hideLauncher();
    }

    public void handleClose(ActionEvent actionEvent) {
        Main.exit();
    }

    public void handleNewsClick(MouseEvent mouseEvent) {
        resetButtonStyle();
        newsButton.getStyleClass().add("selected");
        loadContent(Subscenes.NEWS);
    }

    public void handleProfileClick(MouseEvent mouseEvent) {
        resetButtonStyle();
        profileButton.getStyleClass().add("selected");
        loadContent(Subscenes.LOGIN);
    }

    public void handleContactsClick(MouseEvent mouseEvent) {
        resetButtonStyle();
        contactButton.getStyleClass().add("selected");
    }

    public void handleForumClick(MouseEvent mouseEvent) {
        resetButtonStyle();
        forumButton.getStyleClass().add("selected");
    }

    public void handleMessagesClick(MouseEvent mouseEvent) {
        resetButtonStyle();
        messageButton.getStyleClass().add("selected");
    }

    public void handleSettingsClick(MouseEvent mouseEvent) {
        resetButtonStyle();
        settingsButton.getStyleClass().add("selected");
        loadContent(Subscenes.SETTINGS);
    }

    public void handlePlayClick(MouseEvent mouseEvent) {
        resetButtonStyle();
        playButton.getStyleClass().add("selected");
        loadContent(Subscenes.PLAY);
    }

    private void resetButtonStyle() {
        settingsButton.getStyleClass().remove("selected");
        newsButton.getStyleClass().remove("selected");
        profileButton.getStyleClass().remove("selected");
        contactButton.getStyleClass().remove("selected");
        forumButton.getStyleClass().remove("selected");
        messageButton.getStyleClass().remove("selected");
        playButton.getStyleClass().remove("selected");
    }

    public enum Subscenes {
        NEWS(
                ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SUB_SCENES, "news.fxml"),
                "Новости"
        ),
        PROFILE(
                ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SUB_SCENES, "profile.fxml"),
                "Профиль"
        ),
        FORUM(
                ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SUB_SCENES, "forum.fxml"),
                "Форум"
        ),
        CONTACTS(
                ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SUB_SCENES, "contacts.fxml"),
                "Контакты"
        ),
        MESSAGES(
                ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SUB_SCENES, "messages.fxml"),
                "Сообщения"
        ),
        SETTINGS(
                ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SUB_SCENES, "settings.fxml"),
                "Настройки"
        ),
        PLAY(ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SUB_SCENES, "play.fxml"), "Играть"),
        LOGIN(ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SUB_SCENES, "login.fxml"), "Авторизация"),;

        private final URL fxmlUrl;
        private final String title;

        Subscenes(URL fxmlUrl, String title) {
            this.fxmlUrl = fxmlUrl;
            this.title = title;
        }
    }
}