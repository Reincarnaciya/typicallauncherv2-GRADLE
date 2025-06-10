package space.typro.typicallauncher.controllers.scenes.subscenes;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.Main;
import space.typro.typicallauncher.controllers.BaseController;
import space.typro.typicallauncher.events.EventDispatcher;
import space.typro.typicallauncher.events.Events;
import space.typro.typicallauncher.managers.DirManager;
import space.typro.typicallauncher.managers.SettingsManager;
import space.typro.typicallauncher.managers.UserPC;
import space.typro.typicallauncher.utils.LauncherAlert;
import space.typro.typicallauncher.utils.RamConverter;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class SettingsController extends BaseController {

    @FXML
    private Slider ramSlider;
    @FXML
    private TextField ramTextField;
    @FXML
    private TextField widthTextField;
    @FXML
    private TextField heightTextField;
    @FXML
    private CheckBox fullscreenCheckBox;
    @FXML
    private CheckBox hideLauncherCheckBox;
    @FXML
    private Button openLauncherFolderButton;
    @FXML
    private Hyperlink clientPathHyperlink;
    @FXML
    private Button saveButton;
    @FXML
    private Button resetButton;
    @FXML
    private Text launcherVersionText;


    private final SettingsManager settingsManager = new SettingsManager();
    private SettingsManager.SettingsUIComponents uiComponents;


    private static final int MAX_WIDTH = UserPC.MONITOR_WIDTH;
    private static final int MIN_WIDTH = 800;
    private static final int MAX_HEIGHT = UserPC.MONITOR_HEIGHT;
    private static final int MIN_HEIGHT = 600;
    private static final String LAUNCHER_VERSION_TEXT = "Версия лаунчера: " + Main.LAUNCHER_VERSION;
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize();

        uiComponents = new SettingsManager.SettingsUIComponents(
                fullscreenCheckBox,
                hideLauncherCheckBox,
                ramSlider,
                widthTextField,
                heightTextField
        );
        launcherVersionText.setText(LAUNCHER_VERSION_TEXT);

        openLauncherFolderButton.setOnMouseClicked(this::openLauncherDir);

        ramSlider.valueProperty().addListener(((observableValue, oldVal, newVal) -> changeRamTextField(newVal.floatValue())));
        saveButton.setOnMouseClicked(this::saveSettings);
        clientPathHyperlink.setOnMouseClicked(this::changeClientDir);
        fullscreenCheckBox.setOnMouseClicked(this::fullscreenCheckBoxClick);
        ramSlider.setMax(Math.round(RamConverter.toGigabytes(UserPC.getAvailableRam())));
        EventDispatcher.subscribe(EventDispatcher.EventType.SETTINGS_EVENT, eventData -> {
            Events.SettingsEvent data = (Events.SettingsEvent) eventData;
            updateVisualSettings(data.getNewSettings());
        });

        updateVisualSettings(GameSettings.settings);
        resetButton.setOnMouseClicked(this::resetSettings);
    }

    private void resetSettings(MouseEvent mouseEvent) {
        LauncherAlert.create(
                Alert.AlertType.CONFIRMATION, "Вы уверены, что хотите сбросить настройки?",
                new ButtonType("Да", ButtonBar.ButtonData.APPLY),
                new ButtonType("Нет", ButtonBar.ButtonData.CANCEL_CLOSE)
        ).showAndWait().ifPresent(result -> {
            if (result.getButtonData() == ButtonBar.ButtonData.APPLY) {
                GameSettings settingsBeforeChange = GameSettings.settings;

                GameSettings.resetToDefault();

                HashMap<String, String> changedSettings;

                GameSettings newSettings = GameSettings.settings;

                changedSettings = getChangedSettings(newSettings);

                EventDispatcher.throwEvent(EventDispatcher.EventType.SETTINGS_EVENT, new Events.SettingsEvent(
                        Events.SettingsEvent.SettingsEventType.RESTORE,
                        changedSettings,
                        settingsBeforeChange,
                        newSettings
                ));
            }
        });
    }


    private void fullscreenCheckBoxClick(MouseEvent mouseEvent) {
        if (fullscreenCheckBox.isSelected()){
            widthTextField.setDisable(true);
            heightTextField.setDisable(true);
        }else {
            widthTextField.setDisable(false);
            heightTextField.setDisable(false);
        }
    }



    private void saveSettings(MouseEvent mouseEvent) {
        LauncherAlert.create(Alert.AlertType.CONFIRMATION, "Вы уверены, что хотите сохранить настройки?",
                new ButtonType("Да", ButtonBar.ButtonData.APPLY),
                new ButtonType("Нет, отменить изменения", ButtonBar.ButtonData.CANCEL_CLOSE)
        )
                .showAndWait()
                .ifPresent(result -> {
                    if (result.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE){
                        return;
                    }
                    if (result.getButtonData() == ButtonBar.ButtonData.APPLY){
                        if (!settingsIsCorrect()){
                            return;
                        }
                        GameSettings settingsBeforeChange = GameSettings.settings;
                        HashMap<String, String> changedSettings;
                        GameSettings newSettings = GameSettings.settings;
                        changedSettings = getChangedSettings(newSettings);
                        try {
                            newSettings.save();
                            EventDispatcher.throwEvent(EventDispatcher.EventType.SETTINGS_EVENT,
                                    new Events.SettingsEvent(
                                            Events.SettingsEvent.SettingsEventType.SAVE,
                                            changedSettings,
                                            settingsBeforeChange,
                                            newSettings
                                    ));
                        }catch (IOException e){
                            showErrorAlert("Чёт ошибка какая-то.. Подробности в консоли: " + e.getMessage());
                            log.error("A?", e);
                        }
                    }
                });
        updateVisualSettings(GameSettings.settings);
    }

    private void changeClientDir(MouseEvent mouseEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(GameSettings.settings.pathToClientDir));
        chooser.setTitle("Выберите папку, в которую будут сохранятся клиенты игры");
        File result = chooser.showDialog(Main.GLOBAL_STAGE);
        if (result == null) {
            return;
        }
        result.mkdirs();
        GameSettings.settings.setPathToClientDir(result.getPath());
        updateVisualSettings(GameSettings.settings);
    }

    private HashMap<String, String> getChangedSettings(GameSettings newSettings) {
        Map<GameSettings.SettingsEnum, String> changes =
                settingsManager.detectChanges(newSettings, uiComponents);

        changes.forEach((setting, value) -> {
            switch (setting) {
                case FULLSCREEN:
                    newSettings.setFullscreen(Boolean.parseBoolean(value));
                    break;
                case HIDE_TO_TRAY:
                    newSettings.setHideToTray(Boolean.parseBoolean(value));
                    break;
                case RAM:
                    newSettings.setRam(Float.parseFloat(value));
                    break;
                case WIDTH:
                    newSettings.setWidth(Integer.parseInt(value));
                    break;
                case HEIGHT:
                    newSettings.setHeight(Integer.parseInt(value));
                    break;
            }
        });

        return changes.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        Map.Entry::getValue,
                        (a, b) -> b,
                        HashMap::new
                ));
    }


    private boolean settingsIsCorrect() {
        int height;
        int width;
//        float ram = (float) ramSlider.getValue();
//        boolean fullscreen = fullscreenCheckBox.isSelected();
//        boolean hideInTray = hideLauncherCheckBox.isSelected();

        try {
            height = Integer.parseInt(heightTextField.getText());
            width = Integer.parseInt(widthTextField.getText());
        } catch (NumberFormatException e) {
            showErrorAlert("Значение высоты или ширины игры не может быть дробным числом.");
            return false;
        }
        if (width > MAX_WIDTH) {
            showErrorAlert(String.format("Ширина не может быть больше %s, вы поставили %s", MAX_WIDTH, width));
            return false;
        }
        if (width < MIN_WIDTH) {
            showErrorAlert(String.format("Ширина не может быть меньше %s, вы поставили %s", MIN_WIDTH, width));
            return false;
        }
        if (height > MAX_HEIGHT) {
            showErrorAlert(String.format("Высота не может быть больше %s, вы поставили %s", MAX_HEIGHT, height));
            return false;
        }
        if (height < MIN_HEIGHT) {
            showErrorAlert(String.format("Высота не может быть меньше %s, вы выставили %s", MIN_HEIGHT, height));
            return false;
        }

        return true;
    }



    private void updateVisualSettings(GameSettings settings) {
        ramSlider.setValue(settings.ram);
        changeRamTextField(settings.ram);
        fullscreenCheckBox.setSelected(settings.fullscreen);
        hideLauncherCheckBox.setSelected(settings.hideToTray);
        widthTextField.setText(String.valueOf(settings.width));
        heightTextField.setText(String.valueOf(settings.height));
        clientPathHyperlink.setText(settings.pathToClientDir);
        fullscreenCheckBoxClick(null);
    }

    public void changeRamTextField(float newVal){
        StringBuilder builder = new StringBuilder(String.valueOf(newVal));

        int temp = (int) Math.floor((newVal*10%10));

        if (temp != 0){
            if (temp == 5){
                ramTextField.setText(builder.substring(
                        0,
                        builder.indexOf(".")+2
                ) + " Gb");
            }
        }else {
            ramTextField.setText(builder.substring(
                    0,
                    builder.indexOf(".")
            ) + " Gb");
        }
    }

    private void openLauncherDir(MouseEvent mouseEvent) {
        DirManager.launcherDir.openInExplorer();
    }

    @Slf4j
    @NoArgsConstructor
    @AllArgsConstructor
    public static @Data class GameSettings implements Serializable {
        public static GameSettings settings = new GameSettings();

        /**
         * Значение, определяющее ширину окна игры,
         * Игнорируется, если параметр fullscreen = true
         */
        private int width = 800;
        /**
         * Значение, определяющее высоту окна игры,
         * Игнорируется, если параметр fullscreen = true
         */
        private int height = 600;
        /**
         * Флаг, который определяет будет ли игра запускаться в полный экран,
         * Взаимоисключает width и height
         */
        private boolean fullscreen = false;
        /**
         * Флаг, который определяет будет ли лаунчер скрываться в трей во время запуска игры
         */
        private boolean hideToTray = false;
        /**
         * Выделенное ОЗУ под игру в GB
         */
        private float ram = 1;
        /**
         * Путь до папки с клиентами
         */
        private String pathToClientDir = DirManager.launcherDir + File.separator + "clients";

        private static File settingsFile = new File(DirManager.launcherDir.getDir() + File.separator + "settings.properties");
        public void loadSettings() throws IOException {

            if (!new File(pathToClientDir).exists()){
                new File(pathToClientDir).mkdirs();
            }
            if (!settingsFile.exists()){
                settingsFile.createNewFile();
                return;
            }

            Properties properties = new Properties();
            properties.load(new FileInputStream(settingsFile));
            try {
                this.fullscreen = Boolean.parseBoolean(properties.getProperty(SettingsEnum.FULLSCREEN.name()));
                this.height = Integer.parseInt(properties.getProperty(SettingsEnum.HEIGHT.name()));
                this.width = Integer.parseInt(properties.getProperty(SettingsEnum.WIDTH.name()));
                this.hideToTray = Boolean.parseBoolean(properties.getProperty(SettingsEnum.HIDE_TO_TRAY.name()));
                this.ram = Float.parseFloat(properties.getProperty(SettingsEnum.RAM.name()));
                this.pathToClientDir = properties.getProperty(SettingsEnum.PATH_TO_CLIENT.name());
            }catch (Exception e){
                log.error("cannot load settings from properties file");
            }



        }


        public void save() throws IOException {
            Properties properties = new Properties();

            properties.setProperty(SettingsEnum.FULLSCREEN.name(), String.valueOf(fullscreen));
            properties.setProperty(SettingsEnum.WIDTH.name(), String.valueOf(width));
            properties.setProperty(SettingsEnum.HEIGHT.name(), String.valueOf(height));
            properties.setProperty(SettingsEnum.HIDE_TO_TRAY.name(), String.valueOf(hideToTray));
            properties.setProperty(SettingsEnum.RAM.name(), String.valueOf(ram));
            properties.setProperty(SettingsEnum.PATH_TO_CLIENT.name(), String.valueOf(pathToClientDir));


            try (OutputStream output = new FileOutputStream(settingsFile)) {
                properties.store(output, "Launcher settings");
            }
        }

        public enum SettingsEnum{
            FULLSCREEN, WIDTH, HEIGHT, HIDE_TO_TRAY, RAM, PATH_TO_CLIENT
        }

        public static void resetToDefault(){
            settings = new GameSettings();
        }


    }
}

