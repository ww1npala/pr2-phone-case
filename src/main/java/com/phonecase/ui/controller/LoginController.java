package com.phonecase.ui.controller;

import com.phonecase.dto.UserDTO;
import com.phonecase.service.UserService;
import com.phonecase.util.FXMLNavigator;
import com.phonecase.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Контролер вікна авторизації.
 * Реалізує MVVM: відокремлення логіки від представлення.
 * Забезпечує асинхронну аутентифікацію (щоб не блокувати UI thread).
 * Реалізує валідацію вводу та миттєвий зворотний зв'язок.
 */

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final UserService userService;

    @Inject
    public LoginController(UserService userService) {
        this.userService = userService;
    }


    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isBlank()) {
                usernameField.setStyle("-fx-border-color: red;");
            } else {
                usernameField.setStyle("");
            }
            errorLabel.setVisible(false);
        });


        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleLogin();
        });

        logger.debug("LoginController ініціалізовано.");
    }


    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();


        if (username.isBlank()) {
            showError("Введіть ім'я користувача.");
            usernameField.requestFocus();
            return;
        }
        if (password.isBlank()) {
            showError("Введіть пароль.");
            passwordField.requestFocus();
            return;
        }


        loginButton.setDisable(true);
        loadingIndicator.setVisible(true);
        errorLabel.setVisible(false);

        Task<Optional<UserDTO>> task = new Task<>() {
            @Override
            protected Optional<UserDTO> call() {
                return userService.authenticate(username, password);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);

            Optional<UserDTO> userOpt = task.getValue();
            if (userOpt.isPresent()) {
                SessionManager.setCurrentUser(userOpt.get());
                logger.info("Успішний вхід: {}", username);
                navigateToMain();
            } else {
                showError("Невірне ім'я користувача або пароль.");
                passwordField.clear();
                passwordField.requestFocus();
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
            showError("Помилка з'єднання з базою даних.");
            logger.error("Помилка аутентифікації: {}", task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    @FXML
    public void handleRegister() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        FXMLNavigator.navigate(stage, "register.fxml", "Реєстрація", 420, 380);
    }

    private void navigateToMain() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        FXMLNavigator.navigate(stage, "main.fxml", "PhoneCase Manager — Головна", 1050, 680);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}