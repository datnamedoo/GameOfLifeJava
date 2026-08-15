package com.datnamedoo.www.gamelogic;

public class ConfGrid {  // a wrapper for configuring the initial grid

    public int longsPerRow;
    public int WIDTH;
    public int HEIGHT;
    private int[] grid;

    public ConfGrid(int longsPerRow, int Height) {  //values pertaining to grid, will be used
        this.HEIGHT = Height;
        this.WIDTH = (longsPerRow*64);
        this.longsPerRow = longsPerRow;
        this.grid = new int[WIDTH*Height];

    }

    // a simple array with info relevant to current grid
    public int[] getInfoArray() {
        int[] tempMap = new int[3];
        tempMap[0] = longsPerRow;
        tempMap[1] = WIDTH;
        tempMap[2] = HEIGHT;
        return tempMap;
    }

    public void enableCell(int row, int column) {  //sets cell at pos to be alive
        int index = (WIDTH*row) + column;
        grid[index] = 1;
    }

    public void disableCell(int row, int column) {  //sets cell at pos to be dead
        int index = (WIDTH*row) + column;
        grid[index] = 1;
    }

    public long[] toLongArray() {  //converts integer grid to long array representation
        int totalLongs = longsPerRow*HEIGHT;
        long[] longArray = new long[totalLongs];

        int gridIdx = 0;
        for (int i = 0; i < totalLongs; i++) {  //create and add every long to array
            long currentLong = 0;  // initialize empty long to be added to
            for (int buffer = 63; buffer >= 0; buffer--) {  //this will fill the long in reverse so the grid is left to right
                currentLong |= (long) grid[gridIdx] << buffer;
                gridIdx += 1;
            }
            longArray[i] = currentLong;
            continue;
        }
        return longArray;
    }



    
}
