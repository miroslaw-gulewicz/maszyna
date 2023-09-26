package pl.mg;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import pl.mg.UI.ApplicationController;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InGameView.fxml"));
        AnchorPane root = loader.load();
        Scene scene = new Scene(root, 1920, 1080);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> Platform.exit());
        ApplicationController appController = loader.getController();
        appController.setPrimaryStage(primaryStage);
        appController.start();
    }
}