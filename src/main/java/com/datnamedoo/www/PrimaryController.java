package com.datnamedoo.www;
import com.datnamedoo.www.Renderer.PalettePreset;
import com.datnamedoo.www.Renderer.Position;
import com.datnamedoo.www.signaling.*;
import com.datnamedoo.www.AppEvents.RendererEvent;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.fxml.FXML;

// controller for grid GUI
public class PrimaryController {
    @FXML
    private VBox mainContainer;

    @FXML
    private StackPane gameContainer;

    @FXML
    private Button systemPauseButton;

    @FXML
    private ComboBox<PalettePreset> paletteSelectionBox;

    @FXML
    private Slider speedSlider;

    @FXML
    private Canvas screenOutput;

    @FXML
    private Button systemExitButton;

    @FXML
    private Button testButtonForGrid;

    @FXML
    private void testDrawGrid(ActionEvent event) {
        ProcessManager.restartGameLoop();
    }

    @FXML
    private void handleNewPaletteSelection(ActionEvent event) {
        PalettePreset newPreset = paletteSelectionBox.getValue();
        Signaling.<PalettePreset>broadcastEvent(AppEvents.RendererEvent.SETSKIN, newPreset);

    }

    @FXML
    private void fullSystemExit(ActionEvent event) {
        ProcessManager.killProcesses();
        System.exit(0);
    }

    @FXML 
    private void pauseCurrentGame(ActionEvent event) {
        String text = systemPauseButton.getText();
        if (text.equals("Pause")){
            Signaling.<Boolean>broadcastEvent(AppEvents.GameLoopEvent.SETPAUSE, true);
            systemPauseButton.setText("Resume");
            systemPauseButton.setStyle("-fx-background-color: #59ac67");
        }
        else if (text.equals("Resume")) {
            Signaling.<Boolean>broadcastEvent(AppEvents.GameLoopEvent.SETPAUSE, false);
            systemPauseButton.setText("Pause");
            systemPauseButton.setStyle("-fx-background-color: #c4a1a1");


        }
    }





    private static boolean isPanning = false;

    @FXML
    private void initialize() {
        speedSlider.valueProperty().addListener((observable, oldVal, newVal) -> {
            Signaling.<Integer>broadcastEvent(AppEvents.GameLoopEvent.SETSPEED, newVal.intValue());
        });

        // sets clip properties for gameContainer
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(gameContainer.widthProperty());
        clip.heightProperty().bind(gameContainer.heightProperty());
        gameContainer.setClip(clip);

        //adds palette presets to selection box
        for (PalettePreset preset : PalettePreset.values())
            paletteSelectionBox.getItems().add(preset);


        // sends signal to renderer to updale scale when possible
        screenOutput.setOnScroll((ScrollEvent event) -> {
            if (event.getDeltaY() > 0) {
                Signaling.<Integer>broadcastEvent(AppEvents.RendererEvent.SETSCALE, -1);
            } else {
                Signaling.<Integer>broadcastEvent(AppEvents.RendererEvent.SETSCALE, 1);
            }
            event.consume();
        });

        // binding for panning
        screenOutput.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                isPanning=true;


                Position newPos = new Position((int) event.getX(), (int) event.getY());
                Signaling.<Position>broadcastEvent(RendererEvent.SETPAN, newPos);
            }
        });

        //drag for panning 
        screenOutput.setOnMouseDragged(event -> {
            if (isPanning==true) {
                Position panOffset = new Position((int) event.getX(), (int) event.getY());
                Signaling.broadcastEvent(RendererEvent.PANFROMPOS, panOffset);
            }
        });
        
        // mouse exits canvas while trying to pan
        screenOutput.setOnMouseExited(event -> {
            if (isPanning) {
                isPanning=false;
            }
             Position panOffset = new Position((int) event.getX(), (int) event.getY());
            Signaling.<Position>broadcastEvent(RendererEvent.PANSTOPPED, panOffset);
        });

        //release for panning
        screenOutput.setOnMouseReleased(event -> {
            if (isPanning) {
                isPanning=false;
            }
            Position panOffset = new Position((int) event.getX(), (int) event.getY());
            Signaling.<Position>broadcastEvent(RendererEvent.PANSTOPPED, panOffset);
        });
         mainContainer.setOnKeyPressed(event -> {
            KeyCode keyPressed=event.getCode();
            switch (keyPressed) {
                case KeyCode.Q:
                    Signaling.<Integer>broadcastEvent(AppEvents.RendererEvent.SETSCALE, -1);
                    break;
                case KeyCode.E:
                    Signaling.<Integer>broadcastEvent(AppEvents.RendererEvent.SETSCALE, 1);
                    break;
                case KeyCode.SPACE:
                    pauseCurrentGame(null);
                    break;
                case KeyCode.R:
                    testDrawGrid(null);
                    break;
                default:
                    break;
            }
        });

        



        screenOutput.getGraphicsContext2D().setImageSmoothing(false);
        // initializes Processes when program screen is available
        if (screenOutput.getWidth() > 0) {
            ProcessManager.setNewCanvas(screenOutput);
        }
    }


}
