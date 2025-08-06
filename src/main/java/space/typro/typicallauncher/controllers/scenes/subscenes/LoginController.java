package space.typro.typicallauncher.controllers.scenes.subscenes;

import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.Main;
import space.typro.typicallauncher.ResourceHelper;
import space.typro.typicallauncher.controllers.BaseController;
import space.typro.typicallauncher.controllers.scenes.LauncherController;
import space.typro.typicallauncher.managers.AuthManager;
import space.typro.typicallauncher.models.Account;
import space.typro.typicallauncher.models.Password;
import space.typro.typicallauncher.utils.NodeUtil;

import javax.security.sasl.AuthenticationException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

@Slf4j
public class LoginController extends BaseController {
    // Константы для валидации
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MIN_NICKNAME_LENGTH = 6;
    private static final int MAX_NICKNAME_LENGTH = 16;
    private static final String LOGIN_REGEX = "^[a-zA-Z\\d_]*$";
    private static final String PASSWORD_REGEX = "^[a-zA-Z\\d_!]*$";

    @FXML
    private Button passwordVisibleImage;
    @FXML
    private AnchorPane loginPane;
    @FXML
    private TextField login;
    @FXML
    private CheckBox saveAccount;
    @FXML
    private PasswordField password;
    @FXML
    private CheckBox autoLogin;
    @FXML
    private RadioButton withAccount;
    @FXML
    private RadioButton withoutAccount;
    @FXML
    private Hyperlink register;
    @FXML
    private Button auth;
    @FXML
    private ComboBox<Account> accountList;

    private final TextField passwordShowField = new TextField();




    private boolean isPasswordVisible = false;



    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize();
        setupAuthButton();
        setupLoginValidation();
        setupRadioButtons();
        setupPasswordField();
    }


    private void setupAuthButton() {
        auth.setOnMouseClicked(this::handleAuthClick);
    }

    private void setupLoginValidation() {
        addTextFilter(login, LOGIN_REGEX);
    }

    private void setupRadioButtons() {
        ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(withAccount, withoutAccount);
        group.selectToggle(withAccount);

        password.disableProperty().bind(withAccount.selectedProperty().not());
        saveAccount.disableProperty().bind(withAccount.selectedProperty().not());
        autoLogin.disableProperty().bind(withAccount.selectedProperty().not());
        accountList.disableProperty().bind(withAccount.selectedProperty().not());
        passwordVisibleImage.disableProperty().bind(withAccount.selectedProperty().not());
    }

    private void setupPasswordField() {
        configurePasswordVisibilityToggle();
        addTextFilter(password, PASSWORD_REGEX);
    }

    private void configurePasswordVisibilityToggle() {
        passwordVisibleImage.setOnMousePressed(e -> Platform.runLater(() -> togglePasswordVisibility(true)));
        passwordVisibleImage.setOnMouseReleased(e -> Platform.runLater(() -> togglePasswordVisibility(false)));
    }

    private PasswordField tempPasswordField;

    private void togglePasswordVisibility(boolean visible) {
        tempPasswordField = password;

        if (visible) {
            // Копируем текст
            passwordShowField.setText(tempPasswordField.getText());

            // Копируем стили и классы
            passwordShowField.getStyleClass().setAll(tempPasswordField.getStyleClass());



            // Заменяем поле ввода
            Pane parent = (Pane) tempPasswordField.getParent();
            parent.getChildren().add(passwordShowField);

            parent.getChildren().remove(password);
            // Фокусируем новое поле
            passwordShowField.requestFocus();
            passwordShowField.end();
        } else {
            // Возвращаем пароль
            password.setText(passwordShowField.getText());
            password.setVisible(true);
            Pane parent = (Pane) passwordShowField.getParent();
            if (parent != null) {
                parent.getChildren().remove(passwordShowField);
                parent.getChildren().add(tempPasswordField);
            }
            password.requestFocus();
            password.end();
        }
    }



    private void addTextFilter(TextInputControl field, String regex) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!Pattern.matches(regex, newVal)) {
                field.setText(oldVal);
            }
        });
    }

    private void handleAuthClick(MouseEvent event) {
        if (!validateInput()) return;

        if (withAccount.isSelected()) {
            Account.User user = new Account.User(new Password(password.getText()), login.getText());

            try {
                Account account = AuthManager.getInstance().authenticate(user);
                if (account != null) {
                    System.err.println(account);
                    if (account.isAuthorized){

                    }
                }
            } catch (AuthenticationException e) {
                showErrorAlert(e.getMessage());
            }

        } else {
            // Обработка входа без аккаунта
        }
    }

    private boolean validateInput() {
        if (withAccount.isSelected()) {
            if (login.getText().isEmpty() || password.getText().isEmpty()) {
                showErrorAlert("Логин и пароль обязательны для заполнения");
                return false;
            }
            if (password.getText().length() < MIN_PASSWORD_LENGTH) {
                showErrorAlert("Минимальная длина пароля: " + MIN_PASSWORD_LENGTH);
                return false;
            }
        }

        if (login.getText().length() < MIN_NICKNAME_LENGTH ||
                login.getText().length() > MAX_NICKNAME_LENGTH) {
            showErrorAlert(String.format("Никнейм должен быть от %d до %d символов",
                    MIN_NICKNAME_LENGTH, MAX_NICKNAME_LENGTH));
            return false;
        }

        return true;
    }
}