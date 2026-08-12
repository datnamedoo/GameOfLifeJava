package com.datnamedoo.www;


import com.datnamedoo.www.Renderer.PalettePreset;
import com.datnamedoo.www.gamelogic.GameLoop; //import the game module
import javafx.scene.canvas.Canvas;
import com.datnamedoo.www.signaling.PortableReceiver;
import com.datnamedoo.www.signaling.Events.EventDataInt;
import com.datnamedoo.www.mainmenu.GameConfig;;



// primary commication for game and ui
public class ProcessManager  {
    // receiver for grid updates and such
    private static PortableReceiver processReceiver = new PortableReceiver((EventDataInt i) -> {
        processEvent(i);
    }, AppEvents.ProcessEvent.NEWGRID);


    public static GameLoop gameLoop;
    public static Thread gameThread;  //begins game thread loop
    public static final Object lock = new Object();
    private static final long timeLimit = 300; // in ms

    public static Renderer renderer;

    private static volatile long[] latestGrid;  // latest grid submitted
    private  static boolean gridAvailable = false;

    private static int gWidth;
    private static int gHeight;
    private static String threading;
    private static PalettePreset palette;
    private static Canvas renderingCanvas;
    

    //creates the threads for rendering and game logic (does not start them)
    public static void createThreads(){
        gameLoop = new GameLoop(gWidth, gHeight, 75, lock, threading);
        gameThread = new Thread(() -> {  // thread for game
            gameLoop.initializaLoop();});  //begins game thread loop
        renderer = new Renderer(1, gWidth, gHeight, palette);

    }

    //method called when program is launched to initlaize game and rendering
    public static void initStartAll() {
        createThreads();
        renderer.setCanvas(renderingCanvas);
        startProcesses();
    }

    // restarts game loop with new grid, rendering loop stays alive here
    // is somewhat slow, as it must wait for game to be in a safe state to restart
    public static void restartGameLoop() {
        long startTime = System.currentTimeMillis();
        
        // tries to soft reset the game thread
        gameLoop.killCurrentGame();
        while (!gameLoop.canRestart()) {

            if (System.currentTimeMillis()-startTime > timeLimit) {  // fallback if timeout
                gameThread.interrupt();
                gameThread = new Thread(() -> {
                gameLoop.initializaLoop();});  // if times limit is reached, forcefully create a new game thread
                gameThread.start();
                return;

            }
        } // waits for game to reach a restartable state
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    //starts the game thread and rendering threads
    public static void startProcesses() {

        gameThread.start();

        // renderer runs on javafx thread
        renderer.startDisplayLoop();
    }

    //runs when the user closes the game
    public static void killProcesses() {
        if (gameThread.isAlive()) {
            gameThread.interrupt();
        }
    }

    //sets the renderer to use a certain Canvas for displaying buffer
    public static void setNewCanvas(Canvas imgView) {
        renderingCanvas=imgView;
    }

    //returns boolean status of grid
    public static boolean getGridAvailability() {
        return gridAvailable;
    }

    //sets latest grid (done by game thread)
    public static void setLatestGrid(long[] grid) { 
        latestGrid = grid;
        if (gridAvailable == false) {
            gridAvailable = true;
        }
    }


    // gets latest grid available (called by canvas renderer)
    public static long[] getLatestGrid () {
        return latestGrid;
    }

    public static void loadConfig(GameConfig config) {
                gHeight=config.getDimensions().y();
                gWidth=config.getDimensions().x();
                palette=config  .getPalette();
                threading=config.getThreading();
                initStartAll();
                
    }

    // Portable Receiver forwards events to be processed here
    private static void processEvent(EventDataInt event) {
        switch (event.getEvent()) {
            case AppEvents.ProcessEvent.NEWGRID:
                setLatestGrid((long[]) event.getData());
                break;
            default:
                break;
        }
    }



    


}

