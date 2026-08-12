package com.datnamedoo.www;
import com.datnamedoo.www.signaling.Events.EventData;
import com.datnamedoo.www.signaling.ReceiverInterface;
import com.datnamedoo.www.signaling.Signaling;
import com.datnamedoo.www.gamelogic.ArrayHelpers;
import com.datnamedoo.www.AppEvents.RendererEvent;


import java.nio.IntBuffer;
import javafx.animation.AnimationTimer;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;


//class for handling canvas updates
public class Renderer implements ReceiverInterface{
public record cellColorConfig(Color dead, Color alive) {}
public record colorARGB(int dead, int alive) {}
// reference to the window canvas, as well as initial desired scale and starting square for rendering grid
private Canvas display;
private int gridWidth;
private int gridHeight;

//buffer things
private int[] pixelArray;
private PixelBuffer<IntBuffer> pixelBuffer;
private WritableImage displayImage;

//anchor for zooming
public record Position(int x, int y) {}
private int anchorX = 0;
private int anchorY = 0;
private Position newAnchor = new Position(anchorX, anchorY);
private int panAxisX=0;
private int panAxisY=0;


private int viewportWidth;
private int viewportHeight;
private int startingX;
private int startingY;
private int yMax;
private int xMax;


private volatile int newScale;
private int scale;
private int maxScale;  //steps in even multiples
private int minScale; //can not scale below 


// stuff for viewPanel velocity
private boolean applyVelocity=false;
private double velocityX = 0;
private double velocityY = 0;
private double acceleration = 5;
private double defaultCutoffFactor = 0.6;
private double defaultScaleFactor = 0.7;


private Position lastOffsetVector = new Position(0, 0);



// some premade palettes for tha grid display, I like Maple : )
public enum PalettePreset {
    DEFAULT(new cellColorConfig(Color.BLACK, Color.GREEN)),
    CLASSIC(new cellColorConfig(Color.BLACK, Color.WHITESMOKE)),
    CANVAS(new cellColorConfig(Color.WHITESMOKE, Color.BLACK)),
    SKY(new cellColorConfig(Color.DARKBLUE, Color.ALICEBLUE)),
    FIREANTS(new cellColorConfig(Color.WHEAT, Color.RED)),
    GALAXY(new cellColorConfig(Color.BLACK, Color.BLUEVIOLET)),
    STARRYSKY(new cellColorConfig(Color.BLACK, Color.GOLD)),
    PINK(new cellColorConfig(Color.BLACK, Color.DEEPPINK)),
    ORIENTAL(new cellColorConfig(Color.BROWN, Color.BEIGE));


    private cellColorConfig colors;

    private PalettePreset(cellColorConfig colors) {
        this.colors=colors;
    }

    public cellColorConfig getColors() {
        return this.colors;
    }

    


}

private cellColorConfig cellColors;
private colorARGB colorTemp;
private colorARGB newColorTemp;





// initialize all variables needed for later updates
public Renderer(int scale, int width, int height, PalettePreset colorPalette) {  
    this.scale=scale;
    this.newScale=scale;
    this.minScale = 1;
    this.maxScale = getMaxScale(width);
    this.gridHeight=height;
    this.gridWidth=width;
    recalculateScaleValues();


    this.cellColors = colorPalette.getColors();
    subscribeToEvents();
}

// main display update loop for grid rendering
public void startDisplayLoop() {

    colorTemp = new colorARGB(colorToARGB(cellColors.dead), colorToARGB(cellColors.alive));  //calculate argb for colors
    newColorTemp=colorTemp;

    // updates buffer to current dimensions
    updateBufferComponents();

    //gets canvas graphics context for displaying buffer
    GraphicsContext gc = display.getGraphicsContext2D();

    //clears screen and turns of image smoothing 
    gc.clearRect(0, 0, display.getWidth(), display.getHeight());
    gc.setImageSmoothing(false);


    //draws image to canvas
    gc.drawImage(displayImage, 0, 0, display.getWidth(), display.getHeight());
    




    AnimationTimer updateTimer = new AnimationTimer() {

        @Override
        public void handle(long now) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                this.stop();
            }


            updateVelocity(applyVelocity);

            // updates grid anchor points
            if (newAnchor.x != anchorX | newAnchor.y != anchorY) {
                anchorX=newAnchor.x;
                anchorY=newAnchor.y;
                updateBufferComponents();
                generateOffsetValues();
            }



            // if new scale desired, set scale to new scale
            if (scale != newScale) {
                recalculateScaleValues();
                updateBufferComponents();
            }

            //applies new skin in changed
            if (newColorTemp != colorTemp) {
                colorTemp=newColorTemp;
            }

            //if grid exist then render it
            if (ProcessManager.getGridAvailability() == true)
            updateRawBuffer(pixelArray);

            //queue new render to be updated
            pixelBuffer.updateBuffer(pb -> { //tells javafx to update pixelBuffer
                return null;
            });
            // draws image to canvas
            gc.drawImage(displayImage, 0, 0, display.getWidth(), display.getHeight());
        }
    
    };

    updateTimer.start();
}

public void updateRawBuffer(int[] buffer) {
    long[] latestGrid = ProcessManager.getLatestGrid();

    

    //iterate through grid and check cell state
    //update buffer based on cell state and scale
    for (int row = startingY; row < yMax; row++) {
        for (int column = startingX; column < xMax; column++) {

            int cell = ArrayHelpers.getCell(row, column, latestGrid, Math.ceilDiv(gridWidth, 64));
            int cellARGB=colorTemp.dead;
            if (cell == 1) {
                cellARGB=colorTemp.alive;
            }
            buffer[((row-startingY)*viewportWidth)+(column-startingX)] = cellARGB;
        }
    }



}

//coonverts javafx color class to valid argb for pixel buffer
public static int colorToARGB(Color color) {
    double alpha = color.getOpacity();

    //convert from 0.0 - 1.0 scale to 0-255 for argb
    //premultiplied alpha
    int r = (int) Math.round(color.getRed()*alpha*255);
    int g = (int) Math.round(color.getGreen()*alpha*255);
    int b = (int) Math.round(color.getBlue()*alpha*255);
    int a = (int) Math.round(alpha*255);
    return (a << 24) | (r << 16) | (g << 8) | (b); // combine values into one argb number


}

// configures pixel buffer with current scaled grid dimensions
private void updateBufferComponents() {
    pixelArray = new int[viewportWidth*viewportHeight]; //allocate pixel array for buffer
    IntBuffer intBuffer = IntBuffer.wrap(pixelArray); // wrap pixel array in int buffer obj
    PixelFormat<IntBuffer>  format = PixelFormat.getIntArgbPreInstance();

    pixelBuffer = new PixelBuffer<>(viewportWidth, viewportHeight, intBuffer, format);
    displayImage = new WritableImage(pixelBuffer);
}



// recalculates all required scaling values based on a new scale
private void recalculateScaleValues() {
    if (newScale > maxScale) {newScale = maxScale;}
    if (newScale < minScale) {newScale = minScale;}


    if (newScale != minScale) {
    viewportWidth = (gridWidth>>(newScale-1));
    viewportHeight = (gridHeight>>(newScale-1));
    anchorX=(panAxisX-(viewportWidth>>1));
    anchorY=(panAxisY-(viewportWidth>>1));


    } else {
        viewportHeight=gridHeight;
        viewportWidth=gridWidth;
        anchorX=0;
        anchorY=0;
    }

    generateOffsetValues();
    scale=newScale;
}
    

private void generateOffsetValues() {
    if (anchorX<0) anchorX=0;
    else if ((anchorX + viewportWidth) >= gridWidth) {  
        startingX = (gridWidth-viewportWidth);
    }
    else {startingX = anchorX;}

    
    // bounds anchor pos by grid size
    if(anchorY<0) anchorY=0;
    else if ((anchorY+viewportHeight) >= gridHeight) {
        startingY = (gridHeight-viewportHeight);
    }
    else {startingY = anchorY;}


    // the square to stop at when iterating
    xMax = startingX+viewportWidth;
    yMax = startingY+viewportHeight;


}

// checks how many times we can divide the grid in half for the max scale
private int getMaxScale(int width) {
    int newScale = 0;
    int newWidth = width;
    while (true) {
        newScale++;
        if (newWidth<=64) {
            return newScale;
        }
        newWidth=newWidth>>1;
    }
}
public void setCanvas(Canvas imgView) {
    this.display = imgView;
}

public Canvas getImageView() {
    return this.display;
}

// setter method for scale of cells
public void updateScale(int factor) {  
    this.newScale=scale+factor;
}


//getter method for scale
public int getScale(){
    return this.scale;
}

public void setNewColorTemp(PalettePreset newCellConfig) {
    newColorTemp = new colorARGB(colorToARGB(newCellConfig.getColors().dead), colorToARGB(newCellConfig.getColors().alive));
}

public void setPanAxis(Position axisPos) {
    panAxisX=axisPos.x;
    panAxisY=axisPos.y;
}

// updates viewport velocity if necessary
private void updateVelocity(boolean apply) {
    int newPosY = startingY;
    int newPosX = startingX;
    double factor = Math.pow(defaultScaleFactor, (maxScale-(scale*0.7)));
    double cuttoff = Math.pow(defaultCutoffFactor, scale*0.7);


    // exit if either velocity fall below cuttoff number


        if ((lastOffsetVector.x != 0) && (velocityX != 0)) {
            velocityX-=(velocityX*factor);
            if (velocityX > cuttoff) {
            if (lastOffsetVector.x < 0) {
                newPosX = (int) ((double)anchorX+(velocityX));
            }
            else if (lastOffsetVector.x > 0) {
                    newPosX = (int) ((double)anchorX-(velocityX));
            }
        } else {
            apply=false;
        }
        } 

        if (lastOffsetVector.y != 0 && velocityY != 0) {
            velocityY-=(velocityY*factor);
                if (velocityY>cuttoff) {
                    
            if(lastOffsetVector.y<0) {
                newPosY = (int) ((double)anchorY+(velocityY));
            }
            else if (lastOffsetVector.y > 0) {
                    newPosY = (int) ((double)anchorY-(velocityY));
            }
        } else {
            apply=false;
        }
        } 

            
    // apply velocities if desired
    if (apply) {
    if (newPosX<0) newPosX=0;
    if (newPosY<0) newPosY=0;
    if (newPosY>(gridHeight-viewportHeight)) newPosY=(gridHeight-viewportHeight);
    if (newPosX>(gridWidth-viewportWidth)) newPosX=(gridWidth-viewportWidth);
    newAnchor = new Position(newPosX, newPosY);
    }
}



@Override
public <T> void processEvent(EventData<T> event) {
    switch (event.event()) {
        case RendererEvent.SETSKIN:
            setNewColorTemp((PalettePreset) event.data());
            break;
        case RendererEvent.SETSCALE:
            updateScale((Integer) event.data());
            break;
        case RendererEvent.INCREMENTANCHOR:
            break;
        case RendererEvent.PANFROMPOS:
            Position newPos = (Position) event.getData();
            Position relativePos = getRelativePosition(newPos, panAxisX, panAxisY);
            newAnchor=relativePos;

            // increment velocity for drift after panning
            
            velocityX+=(0.1*acceleration);
            velocityY+=(0.1*acceleration);

            break;
        case RendererEvent.SETPAN:
                Position offset = (Position) event.getData();
                double factor = (double)viewportWidth/(double)950;
                Position newOffset = new Position((int) (anchorX+(offset.x*factor)),(int) (anchorY+(offset.y*factor)));
                panAxisX=newOffset.x;
                panAxisY=newOffset.y;

                // do not apply additional velocity to frame
                applyVelocity=false;
                velocityX=0;
                velocityY=0;
                break;            
        case RendererEvent.PANSTOPPED:
            // begin applying lingering velocity after pan is over
            applyVelocity=true;
        default:
            break;
    }
}   

private Position getRelativePosition(Position offset, int panAxisX, int panAxisY) {  // gets position relative to 
    double factor = (double)viewportWidth/(double)display.getWidth();

    Position newOffset = new Position((int) (anchorX+(offset.x*factor)),(int) (anchorY+(offset.y*factor)));
    lastOffsetVector = new Position(newOffset.x-panAxisX, newOffset.y-panAxisY);
    


    int posX = (int) (anchorX-(lastOffsetVector.x*0.1));
    int posY = (int) (anchorY-(lastOffsetVector.y*0.1));

    if (posX<0) posX=0;
    if (posY<0) posY=0;
    if (posY>(gridHeight-viewportHeight)) posY=(gridHeight-viewportHeight);
    if (posX>(gridWidth-viewportWidth)) posX=(gridWidth-viewportWidth);

    return new Position(posX, posY);

}

@Override
public void subscribeToEvents() {
    Signaling.subscribeToEvent(RendererEvent.SETSCALE, this);
    Signaling.subscribeToEvent(RendererEvent.SETSKIN, this);
    Signaling.subscribeToEvent(RendererEvent.PANFROMPOS, this);
    Signaling.subscribeToEvent(RendererEvent.SETPAN, this);
    Signaling.subscribeToEvent(RendererEvent.INCREMENTANCHOR, this);
    Signaling.subscribeToEvent(RendererEvent.PANSTOPPED, this);



}



}
