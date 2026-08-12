package com.datnamedoo.www;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

import com.datnamedoo.www.mainmenu.GameConfig;




/**
 * JavaFX App
 */
public class App extends Application {

    private record SceneConfig(int width, int height, boolean resizable, StageStyle style){}
    private static final SceneConfig mainMenu = new SceneConfig(800, 800, false, StageStyle.UNIFIED);
    private static final SceneConfig gameConf = new SceneConfig(1024, 1080, false, StageStyle.UNIFIED);

    private static Scene scene;
    private static Stage stageRef;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("menu"), mainMenu.width, mainMenu.height);
        stageRef=stage;

        // binding for closing window, just kill everything :)
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            System.exit(0);
        });;

        stage.initStyle(mainMenu.style);
        stage.setResizable(mainMenu.resizable);
        stage.show();
    }

    public static void switchToGame(GameConfig config) throws IOException{
        int width = gameConf.width;
        int height = gameConf.height;

        scene = new Scene(loadFXML("primary"), gameConf.width, gameConf.height);
        stageRef.setScene(scene);
        ProcessManager.loadConfig(config);



        stageRef.setX((int)Screen.getPrimary().getBounds().getWidth()>>2);
        stageRef.setY(1);
        stageRef.setWidth(width);
        stageRef.setHeight(height);

        stageRef.setResizable(gameConf.resizable);


    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false"); 
        launch();
    }

}