package com.phonecase;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.phonecase.config.AppModule;
import com.phonecase.config.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * Головний клас JavaFX-застосунку.
 * Точка входу для модуля керування дизайнами чохлів для мобільних телефонів.
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static Injector injector;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void init() throws Exception {
        logger.info("Ініціалізація застосунку PhoneCase Manager...");
        injector = Guice.createInjector(new AppModule());
        injector.getInstance(DatabaseInitializer.class).initialize();
        logger.info("DI-контейнер та БД ініціалізовано успішно.");
    }

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Запуск головного вікна...");
        URL fxmlUrl = getClass().getResource("/fxml/login.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Не знайдено FXML файл: /fxml/login.fxml");
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setControllerFactory(injector::getInstance);

        Scene scene = new Scene(loader.load(), 400, 350);
        URL cssUrl = getClass().getResource("/fxml/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setTitle("PhoneCase Manager — Вхід");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        logger.info("Застосунок запущено.");
    }
}