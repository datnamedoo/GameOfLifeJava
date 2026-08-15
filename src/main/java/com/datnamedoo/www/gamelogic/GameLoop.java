package com.datnamedoo.www.gamelogic;
import com.datnamedoo.www.AppEvents.GameLoopEvent;
import com.datnamedoo.www.AppEvents.ProcessEvent;
import com.datnamedoo.www.signaling.Signaling;
import com.datnamedoo.www.signaling.Events.EventData;
import com.datnamedoo.www.signaling.ReceiverInterface;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

// basically just the main game loop for the game
public class GameLoop implements ReceiverInterface {
    public int speed; //default speed in ms for sending new grid submissions (ms)
    private int rows;  //columns and rows of grid for game
    private int columns;  
    private Supplier<long[]> threadingMethod = () -> { // threading defaults to multithreading
        return GameOfLife.multiThreadUpdateGrid();
    };
    private ConfGrid confGrid;

    private Object lock;
    private boolean isPaused = false;
    private volatile boolean isRunning = false;
    private volatile boolean isRestartable = false;


    // initialize all desired values from user
    public GameLoop(int columns, int rows, int speed, Object lock, String threading) {
        subscribeToEvents();
        this.lock = lock;
        this.rows=rows;
        this.columns=columns;
        this.speed = speed;
        if (threading.equals("-s")) {
            this.threadingMethod = () -> {
                return GameOfLife.singleThreadUpdateGrid();
            };
        }
        
    }

    //initialize a loop for updating grid based on speed variable
    public void initializaLoop() { 
        configureInitGrid();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (!isRunning) {  // restarts loop is stopped externally
                    synchronized (lock) {
                        isRestartable = true;
                        lock.wait();  // waits for restart
                    }
                            configureInitGrid(); // resets all values, recreates grid a new, then continues lol

                            //send new grid for the frozen screen to update if paused
                            long[] newGrid = threadingMethod.get(); 
                            Signaling.<long[]>broadcastEvent(ProcessEvent.NEWGRID, newGrid);
                            continue;
                }

                if (!isPaused) {  //check if paused, do nothing if so
            
            long[] newGrid = threadingMethod.get();

            Signaling.<long[]>broadcastEvent(ProcessEvent.NEWGRID, newGrid);
            //terminalRender(newGrid, gridData);
            Thread.sleep(speed);
                }
                
            // thread has been interrupted and will be killed
            } catch (InterruptedException | RejectedExecutionException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }



    // configures initial grid and populates it
    // also sets all flags to their defaults
    public void configureInitGrid() {
        confGrid=new ConfGrid(Math.ceilDiv(columns, 64), rows);
        GameOfLife.confFromConfGrid(confGrid);  // rows is divided by 64 becaue the parameter calls for longs per row :)
        GameOfLife.populateGrid(6); 
        isRunning=true;
        isRestartable=false;
    }


    @Override
    public <T> void processEvent(EventData<T> event) {
        switch (event.event()) {
            case GameLoopEvent.SETPAUSE:
                isPaused = (Boolean) event.data();
                break;
            
            case GameLoopEvent.SETSPEED:
                speed = (Integer) event.data();
                break;
        
            default:
                break;
        }
    }


    @Override
    public void subscribeToEvents() {
        Signaling.subscribeToEvent(GameLoopEvent.SETSPEED, this);
        Signaling.subscribeToEvent(GameLoopEvent.SETPAUSE, this);

    }

    // returns whether the thread is currently running or not
    public boolean getGameStatus() { 
        return this.isRunning;
    }

    // tells the gameloop to end
    public void killCurrentGame() {
        isRunning=false;
    }

    public boolean canRestart() {
        return isRestartable;
    }


    //getter method for speed
    public int getSpeed() {
        return this.speed;
    }

    //setter method for speed
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    //getter method for pause val
    public boolean getPause() {
        return this.isPaused;
    }

    //setter method for pause val
    public void setPause(boolean value) {
        this.isPaused = value;
    }

}


