package com.datnamedoo.www.gamelogic;
import com.datnamedoo.www.gamelogic.ArrayHelpers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

//IDE throws a warning for my unused import but maven requires it
@SuppressWarnings("unused")  
public class GameOfLife{  //basic high level controls for game and config
    public static long[] prevGrid;  // two arrays for game
    public static long[] newGrid;  // each will be used to represent a generation in conways game of life
    public static int[] gridInfo;

    public static ConfGrid confGrid;

    // stuff for generation/threading
    private static ExecutorService threadManager = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static final Phaser counter = new Phaser(1);
    public static int chunkSize;


   /////Functions for configuring intial grid and game settings////
    public static void confFromConfGrid(ConfGrid config) {  // configueres empty grid to certain dimensions
        confGrid = config;
        prevGrid = confGrid.toLongArray();  // two arrays for game
        newGrid = confGrid.toLongArray();  // each will be used to represent a generation in conways game of life
        chunkSize = (confGrid.HEIGHT/Runtime.getRuntime().availableProcessors());  //updates chunksize per CPU specifications
        gridInfo = confGrid.getInfoArray();

        //close active threads if prev threads are alive
        if (!threadManager.isTerminated()) {
        threadManager.close();
        }
        threadManager = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    }

    public static void confFromDimensions(int longsPerRow, int height) {
        confGrid = new ConfGrid(longsPerRow, height);
        prevGrid = confGrid.toLongArray();
        newGrid = confGrid.toLongArray();
        chunkSize = (confGrid.HEIGHT/Runtime.getRuntime().availableProcessors());  //updates chunksize per CPU specifications
        gridInfo = confGrid.getInfoArray();
    }

    public static void populateGrid(int density) {  //just a wrapper so UI doesn't have to touch ArrayHelpers
            ArrayHelpers.setAllRandom(density, prevGrid, confGrid.getInfoArray());
    }


      ////mostly functions for testing////
    public static float[] getPerformanceTimes(int genLimit, String threadingType) {  //records times for generation up to a certain limit
        float[] allTimes = new float[genLimit];

        for (int i = 0; i < genLimit; i++) {  //generate new generations until limit is hit
            long startTime = System.nanoTime();  //time before operations
            if (threadingType.toLowerCase() == "-s") { // single threaded
                    singleThreadUpdateGrid();
            }
            else if (threadingType.toLowerCase() == "-m") {  //multi threaded
                multiThreadUpdateGrid();
            }
            else { // bad params
                break;
            }
            long endTime = System.nanoTime();  // time after operations
            allTimes[i] = (float) (endTime-startTime)/1_000_000;  // add time to array
        
        }
        return allTimes;  //return all times recorded
    }


   ////base of the main updating functions for the actual game////
    public static long[] singleThreadUpdateGrid() {  // simple singlethreaded run of the row updating
        for (int row = 0; row < confGrid.HEIGHT; row++) {  //update every row
            ArrayHelpers.updateRow(row, prevGrid, newGrid, gridInfo);
        }

        long [] temp = prevGrid;

        prevGrid = newGrid;  // swaps grids to be used again
        newGrid = temp;
        return prevGrid;  //return grid to caller
    }
    public static long[] multiThreadUpdateGrid(){  //experimental, a lot fast for bigger grid sizes
        for (int row = 0; row < gridInfo[2]; row+=chunkSize) {  //iterate through chunks of rows and assign threads to process them
            counter.register();
            int startingRow = row;

            threadManager.submit(() -> {  // submists rows to be processed in chunks
                try {
                    ArrayHelpers.updateRows(startingRow, chunkSize ,prevGrid, newGrid, gridInfo);
                } finally {
                    counter.arriveAndDeregister();
                }
            });
        }
            counter.arriveAndAwaitAdvance();  //waits for all threads before continueing
            
            long [] temp = prevGrid;
            prevGrid = newGrid;  // swaps grids to be used again
            newGrid = temp;
            return prevGrid;  //return grid to caller

    }
}


