package space.typro.typicallauncher.controllers.scenes;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.Main;
import space.typro.typicallauncher.ResourceHelper;
import space.typro.typicallauncher.controllers.BaseController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

@Slf4j
public class LauncherController extends BaseController {
    private static final Deque<Subscene> sceneHistory = new ArrayDeque<>();
    private static LauncherController instance;

    @FXML
    private VBox mainPane;
    @FXML
    private AnchorPane leftPane;
    @FXML
    private Pane accountLeftPane;
    @FXML
    private Pane newsLeftPane;
    @FXML
    private Pane forumLeftPane;
    @FXML
    private Pane friendsLeftPane;
    @FXML
    private Pane settingsLeftPane;
    @FXML
    private Pane playLeftPane;
    @FXML
    private VBox textVBox;
    @FXML
    public Text upperText;
    @FXML
    private AnchorPane lineBelowText;
    @FXML
    private Pane exitButton;
    @FXML
    private Pane subscene;
    @FXML
    private Pane hideLauncherButton;
    private Subscene currentSubscene = null;

    public static void loadSubscene(Subscene newScene) {
        if (newScene == instance.currentSubscene) return;
        log.info("Open scene: {}", newScene);
        Platform.runLater(() -> {
            try {
                Parent sceneContent = loadFxmlContent(newScene);
                instance.updateUI(newScene, sceneContent);
                updateSceneHistory(newScene);
            } catch (Exception e) {
                log.error(String.format("Error loading subscene: {%s}", newScene), e);
                instance.showErrorAlert("Scene loading error: " + e.getMessage());
            }
        });
    }

    private static Parent loadFxmlContent(Subscene scene) throws URISyntaxException, IOException {
        URI fxmlLocation = new URI(ResourceHelper.getResourceByType(
                ResourceHelper.ResourceFolder.SUB_SCENES,
                scene.getFxml()
        ));
        return FXMLLoader.load(fxmlLocation.toURL());
    }

    private static void updateSceneHistory(Subscene newScene) {
        sceneHistory.push(newScene);
        instance.currentSubscene = newScene;
    }

    public static void loadPreviousScene() {
        if (sceneHistory.size() <= 1) return;

        sceneHistory.pop(); // Remove current scene
        Subscene previous = sceneHistory.peek();
        loadSubscene(previous);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        configureLeftPanels();
        configureTopPane();
        setupSceneHistory();
        loadInitialSubscene();
    }

    private void setupSceneHistory() {
        sceneHistory.push(Subscene.NONE);
    }

    private void loadInitialSubscene() {
        // TODO: Add auth check logic
        loadSubscene(Subscene.LOGIN);
    }

    private void configureTopPane() {
        exitButton.setOnMouseClicked(event -> Main.exit());
    }

    private void configureLeftPanels() {
        Map<Pane, Subscene> panelScenes = new LinkedHashMap<>();
        panelScenes.put(accountLeftPane, Subscene.LOGIN);
        panelScenes.put(newsLeftPane, Subscene.NEWS);
        panelScenes.put(forumLeftPane, Subscene.FORUM);
        panelScenes.put(friendsLeftPane, Subscene.FRIENDS);
        panelScenes.put(settingsLeftPane, Subscene.SETTINGS);
        panelScenes.put(playLeftPane, Subscene.PLAY);

        panelScenes.forEach((panel, scene) -> {
            panel.setUserData(scene);
            panel.setOnMouseClicked(this::handlePanelClick);
            applyPanelStyle(panel, scene.getImageName());
        });
    }

    private void applyPanelStyle(Pane panel, String imageName) {
        String normalStyle = "-fx-background-image: url('%s'); -fx-background-repeat: no-repeat;".formatted(
                ResourceHelper.getResourceUrlByType(
                        ResourceHelper.ResourceFolder.LEFT_PANEL_IMAGE_NP,
                        imageName
                ));
        String hoverStyle = "-fx-background-image: url('%s'); -fx-background-repeat: no-repeat;".formatted(
                ResourceHelper.getResourceUrlByType(
                        ResourceHelper.ResourceFolder.LEFT_PANEL_IMAGE_P,
                        imageName
                ));

        panel.setStyle(normalStyle);
        panel.hoverProperty().addListener((obs, oldVal, isHovering) -> {
            panel.setStyle(isHovering ? hoverStyle : normalStyle);
        });
    }

    private void handlePanelClick(MouseEvent event) {
        Pane source = (Pane) event.getSource();
        Subscene targetScene = (Subscene) source.getUserData();
        loadSubscene(targetScene);
    }

    private void updateUI(Subscene scene, Parent content) {
        subscene.getChildren().setAll(content);
        upperText.setText(scene.getTitle());
    }

    @FXML
    private void hideLauncher(MouseEvent event) {
        Main.hideLauncher();
    }

    @Getter
    public enum Subscene {
        NONE("", "", ""),
        LOGIN("login-view.fxml", "Авторизация", "account.png"),
        PROFILE("profile-view.fxml", "Профиль", "account.png"),
        REGISTER("registration-view.fxml", "Регистрация", "account.png"),
        NEWS("news-view.fxml", "Новости", "news.png"),
        FORUM("forum-view.fxml", "Форум", "forum.png"),
        FRIENDS("friends-view.fxml", "Друзья", "friends.png"),
        SETTINGS("settings-view.fxml", "Настройки", "settings.png"),
        PLAY("play-view.fxml", "Играть", "play.png");

        private final String fxml;
        private final String title;
        private final String imageName;

        Subscene(String fxml, String title, String imageName) {
            this.fxml = fxml;
            this.title = title;
            this.imageName = imageName;
        }

    }

}