package space.typro.typicallauncher.controllers.scenes.subscenes;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.ResourceHelper;
import space.typro.typicallauncher.controllers.BaseController;
import space.typro.typicallauncher.controllers.scenes.LauncherController;
import space.typro.typicallauncher.managers.AuthManager;
import space.typro.typicallauncher.models.Account;
import space.typro.typicallauncher.models.Password;
import space.typro.typicallauncher.utils.NodeUtil;

import javax.security.sasl.AuthenticationException;
import java.net.URL;
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
    private ImageView passwordVisibleImage;
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
        passwordVisibleImage.setOnMousePressed(e -> togglePasswordVisibility(true));
        passwordVisibleImage.setOnMouseReleased(e -> togglePasswordVisibility(false));
    }

    private void togglePasswordVisibility(boolean visible) {
        try {
            String imagePath = visible ? "login/Глаз-открытый.png" : "login/Глаз-закрытый.png";
            passwordVisibleImage.setImage(loadImage(imagePath));

            if (visible) {
                showPasswordText();
            } else {
                hidePasswordText();
            }
        } catch (Exception ex) {
            log.error("Error toggling password visibility", ex);
        }
    }

    private Image loadImage(String path) {
        return new Image(ResourceHelper.getResourceByType(
                ResourceHelper.ResourceFolder.IMAGES, path
        ));
    }

    private void initializePasswordShowField() {
        passwordShowField.prefWidthProperty().bind(password.prefWidthProperty());
        passwordShowField.minWidthProperty().bind(password.minWidthProperty());
        passwordShowField.maxWidthProperty().bind(password.maxWidthProperty());

        passwordShowField.getStyleClass().setAll(password.getStyleClass());

        AnchorPane.setTopAnchor(passwordShowField, AnchorPane.getTopAnchor(password));
        AnchorPane.setBottomAnchor(passwordShowField, AnchorPane.getBottomAnchor(password));
        AnchorPane.setLeftAnchor(passwordShowField, AnchorPane.getLeftAnchor(password));
        AnchorPane.setRightAnchor(passwordShowField, AnchorPane.getRightAnchor(password));
    }

    private void showPasswordText() {
        if (!isPasswordVisible) {
            initializePasswordShowField();
            passwordShowField.setText(password.getText());
            replaceNode(password, passwordShowField);
            isPasswordVisible = true;
        }
    }

    private void hidePasswordText() {
        if (isPasswordVisible) {
            password.setText(passwordShowField.getText());
            replaceNode(passwordShowField, password);
            isPasswordVisible = false;
        }
    }

    private void replaceNode(Control oldNode, Control newNode) {
        NodeUtil.replaceNode((Pane) oldNode.getParent(), oldNode, newNode);
        newNode.requestFocus();
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
                        LauncherController.loadSubscene(LauncherController.Subscene.PROFILE);
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