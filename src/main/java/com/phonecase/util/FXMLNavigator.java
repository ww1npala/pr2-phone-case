package com.phonecase.util;

import com.phonecase.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * Утилітний клас для навігації між вікнами JavaFX.
 * Патерн Facade — спрощує перемикання сцен.
 */
public final class FXMLNavigator {

    private static final Logger logger = LoggerFactory.getLogger(FXMLNavigator.class);

    private FXMLNavigator() {}

    public static void navigate(Stage stage, String fxmlPath, String title, double width, double height) {
        try {
            URL url = FXMLNavigator.class.getResource("/fxml/" + fxmlPath);
            if (url == null) {
                logger.error("FXML не знайдено: /fxml/{}", fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(App.injector::getInstance);

            Scene scene = new Scene(loader.load(), width, height);
            URL css = FXMLNavigator.class.getResource("/fxml/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            logger.error("Помилка навігації до {}: {}", fxmlPath, e.getMessage(), e);
        }
    }
}
