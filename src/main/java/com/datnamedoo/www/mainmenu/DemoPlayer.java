package com.datnamedoo.www.mainmenu;

import com.datnamedoo.www.Renderer;
import com.datnamedoo.www.Renderer.PalettePreset;
import com.datnamedoo.www.gamelogic.ArrayHelpers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.io.File;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Random;


// plays background demo on main menu
// takes a bit from renderer hehe
public class DemoPlayer {
    private final String fileLocation = "src/main/resources/com/datnamedoo/www/demoReel.json";

    private Random rng = new Random();
    private Renderer.PalettePreset colorConf;

    private Renderer.colorARGB colorTemp;
    private boolean inProgress;
    

    private Canvas canvas;
    private int canvasHeight;
    private int canvasWidth;

    //buffer things
    private int[] pixelArray;
    private PixelBuffer<IntBuffer> pixelBuffer;
    private WritableImage displayImage;
    private int frameLimit = 200;



    private Map<Integer, long[]> demo;


    public DemoPlayer(Canvas bgCanvas){
        this.canvas=bgCanvas;
        this.canvasHeight=(int)bgCanvas.getHeight();
        this.canvasWidth=(int)bgCanvas.getWidth();
    };



    // loads initial demo from json file
    public void loadDemo() {
        try {
            ObjectMapper mapper = new ObjectMapper(); // mapper for reading json demo file
            File demoFile = new File(fileLocation);
            demo = mapper.readValue(demoFile, new TypeReference<Map<Integer, long[]>>(){});
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage()+"\n"+e.getStackTrace());
        }
    }


    // basically a simpler copy of the other rendering script
    // everything is already preprocessed
    // also blur added as an effect to canvas
public void start() {

    //gets canvas graphics context for displaying buffer
    GraphicsContext gc = canvas.getGraphicsContext2D();

    // instance blur object
    GaussianBlur blur = new GaussianBlur();
    blur.setRadius(15);

    // get a random palette for background
    generateRandomPalette();


    //clears screen and turns of image smoothing 
    gc.clearRect(0, 0, canvasWidth, canvasHeight);
    gc.setImageSmoothing(false);
    updateBufferComponents();


    //draws image to canvas
    gc.drawImage(displayImage, 0, 0, canvas.getWidth(), canvas.getHeight());
    gc.setEffect(blur);

    inProgress=true;
    
    // animation timer for updating background
    AnimationTimer updateTimer = new AnimationTimer() {

        private int currentFrame=1;
        private long lastTime = 0;
        private final double waitTime = 0.5;
    

        @Override
        public void handle(long now) {
            if (now-lastTime>(waitTime*1_000_000_000)) {
                lastTime=now;
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                this.stop();
            }


            //if grid exist then render it
            if (!canvas.isDisabled()) {
            updateRawBuffer(pixelArray, currentFrame);
            }

            //queue new render to be updated
            pixelBuffer.updateBuffer(pb -> { //tells javafx to update pixelBuffer
                return null;
            });
            // draws image to canvas
            gc.drawImage(displayImage, 0, 0, canvasWidth, canvasHeight);
            currentFrame++;
            if (!inProgress) {
                this.stop();
            }

            // reset frame if limit reached
            if (currentFrame >= frameLimit) {
                currentFrame=1;
            }
        }
        }
    
    };

    updateTimer.start();
}

public void stop(){
    inProgress=false;
}

// gets a random palette for background of menu
private void generateRandomPalette() {

        PalettePreset[] colors = Renderer.PalettePreset.values();
        int colorNum = rng.nextInt(0, colors.length);
        
        colorConf=colors[colorNum];
        colorTemp = new Renderer.colorARGB(
            Renderer.colorToARGB(colorConf.getColors().dead()),
            Renderer.colorToARGB(colorConf.getColors().alive()));
        return;

}

public PalettePreset getPalette() {
    return this.colorConf;
}


// configures pixel buffer with current scaled grid dimensions
private void updateBufferComponents() {
    pixelArray = new int[canvasWidth*canvasHeight]; //allocate pixel array for buffer
    IntBuffer intBuffer = IntBuffer.wrap(pixelArray); // wrap pixel array in int buffer obj
    PixelFormat<IntBuffer>  format = PixelFormat.getIntArgbPreInstance();

    pixelBuffer = new PixelBuffer<>(64, 64, intBuffer, format);
    displayImage = new WritableImage(pixelBuffer);
}



public void updateRawBuffer(int[] buffer, int frameNum) {
    long[] latestGrid = demo.get(frameNum);

    //iterate through grid and check cell state
    //update buffer based on cell state and scale
    for (int row = 0; row < 64; row++) {
        for (int column = 0; column < 64; column++) {

            int cell = ArrayHelpers.getCell(row, column, latestGrid, 1);
            int cellARGB=colorTemp.dead();
            if (cell == 1) {
                cellARGB=colorTemp.alive();
            }
            buffer[((row-0)*64)+(column-0)] = cellARGB;
        }
    }



}






}
