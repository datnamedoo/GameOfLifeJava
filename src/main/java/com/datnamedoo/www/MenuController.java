package com.datnamedoo.www;
import java.io.IOException;

import com.datnamedoo.www.mainmenu.DemoPlayer;
import com.datnamedoo.www.mainmenu.GameConfig;
import com.datnamedoo.www.mainmenu.GameConfig.AvailableDimensions;
import com.datnamedoo.www.mainmenu.GameConfig.Threading;

import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;


public class MenuController {
    @FXML
    private ComboBox<Threading> threadingSelection;

    private GameConfig gameConfig = new GameConfig();


    @FXML
    private ComboBox<AvailableDimensions> dimensionsSelection;

    @FXML
    private Button exitButton;

    @FXML
    private VBox settingsContainer;

    @FXML
    private Button closeSettingsButton;

    private DemoPlayer demoPlayer;

    @FXML
    private Canvas demoReplay;

    @FXML
    private Button settings;

    @FXML
    private Button launchGame;

    @FXML
    private StackPane mainMenu;

    @FXML
    private void startGame(ActionEvent event) {
        try{
        demoPlayer.stop();
        gameConfig.setPalette(demoPlayer.getPalette());
        App.switchToGame(gameConfig);

        
        } catch(IOException e) {
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getStackTrace());
        }
    }

    @FXML
    private void showSettings(ActionEvent event) {
        settingsContainer.setVisible(true);
    }

    @FXML
    private void closeSettings(ActionEvent event) {
        settingsContainer.setVisible(false);
    }

    @FXML
    private void exitMainMenu(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void newDimensionSelection(ActionEvent event) {
        gameConfig.setDimensions(dimensionsSelection.getValue());
    }
    
    @FXML
    private void newThreadingSelection(ActionEvent event) {
        gameConfig.setThreading(threadingSelection.getValue());
    }


    @FXML
    private void initialize() {
        launchGame.setOnMouseEntered(event -> {
            launchGame.setStyle("-fx-background-color: #57b10e; -fx-font-size: 28");
        });
        launchGame.setOnMouseExited(event -> {
            launchGame.setStyle("-fx-background-color: #57b10eb0; -fx-font-size: 25");
        });
        settings.setOnMouseEntered(event -> {
            settings.setStyle("-fx-background-color: #a7e0b1; -fx-font-size: 28");
        });
        settings.setOnMouseExited(event -> {
            settings.setStyle("-fx-background-color: #a7e0b1b0; -fx-font-size: 25");
        });
        closeSettingsButton.setOnMouseEntered(event -> {
            closeSettingsButton.setStyle("-fx-background-color: #ee2d0be1; -fx-font-size: 16");
        });
        closeSettingsButton.setOnMouseExited(event -> {
            closeSettingsButton.setStyle("-fx-background-color: #ee2d0ba6; -fx-font-size: 16");
        });
        exitButton.setOnMouseEntered(event -> {
            exitButton.setStyle("-fx-background-color: #ee2d0be1; -fx-font-size: 28");
        });
        exitButton.setOnMouseExited(event -> {
            exitButton.setStyle("-fx-background-color: #ee2d0ba6; -fx-font-size: 25");
        });
        for (AvailableDimensions preset : AvailableDimensions.values())
            dimensionsSelection.getItems().add(preset);
        for (Threading threading : Threading.values())
            threadingSelection.getItems().add(threading);


        if (demoReplay.getWidth() > 0) {
            demoPlayer = new DemoPlayer(demoReplay);
            demoPlayer.loadDemo();
            demoPlayer.start();
        }


    }
}
