package com.phonecase.ui.controller;

import com.phonecase.service.UserService;
import com.phonecase.util.FXMLNavigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Контролер вікна реєстрації нового користувача.
 * Забезпечує валідацію вводу та асинхронну реєстрацію.
 */

public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final UserService userService;

    @Inject
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        if (loadingIndicator != null) loadingIndicator.setVisible(false);

        emailField.textProperty().addListener((obs, old, val) -> {
            if (!val.isBlank() && !val.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
                emailField.setStyle("-fx-border-color: orange;");
            } else {
                emailField.setStyle("");
            }
        });

        confirmPasswordField.textProperty().addListener((obs, old, val) -> {
            if (!val.equals(passwordField.getText())) {
                confirmPasswordField.setStyle("-fx-border-color: red;");
            } else {
                confirmPasswordField.setStyle("");
            }
        });
    }

    @FXML
    public void handleRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            showError("Всі поля обов'язкові для заповнення.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Паролі не збігаються.");
            return;
        }
        if (password.length() < 6) {
            showError("Пароль повинен містити щонайменше 6 символів.");
            return;
        }

        registerButton.setDisable(true);
        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        errorLabel.setVisible(false);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                userService.register(username, password, email);
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            registerButton.setDisable(false);
            successLabel.setText("Реєстрацію успішно завершено! Тепер ви можете увійти.");
            successLabel.setVisible(true);
            clearFields();
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            registerButton.setDisable(false);
            String msg = task.getException().getMessage();
            showError(msg != null ? msg : "Помилка реєстрації.");
            logger.error("Помилка реєстрації: {}", msg);
        }));

        new Thread(task).start();
    }

    @FXML
    public void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLNavigator.navigate(stage, "login.fxml", "PhoneCase Manager — Вхід", 400, 350);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}