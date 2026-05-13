package com.phonecase.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Утилітний клас для відображення JavaFX Alert-діалогів.
 * Патерн Facade — спрощує виклик Alert.
 */

public final class AlertUtil {

    private AlertUtil() {}
    public static void info(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static void warning(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }


    public static void error(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        return alert.showAndWait()
                .map(bt -> bt == ButtonType.YES)
                .orElse(false);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}