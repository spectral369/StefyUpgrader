package com.stefy.upgrader.stefyupgrader;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/FXMLDocument.fxml"));

        Scene scene = new Scene(root);

        root.setId("root1");

        scene.getStylesheets().clear();
        scene.getStylesheets().add("/styles/mainView.css");

        stage.setTitle("StefyMusicUpgrader");
        
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/icon.png")));
        stage.setResizable(false);
        stage.setScene(scene);

        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
