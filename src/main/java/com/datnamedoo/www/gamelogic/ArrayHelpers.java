package com.datnamedoo.www.gamelogic;
import java.util.Random;
import com.datnamedoo.www.gamelogic.ConfGrid;;

//IDE throws a warning for my unused import but maven requires it
@SuppressWarnings("unused")
public class ArrayHelpers {  //important functions for handling long array

    private ArrayHelpers () {}

    public static final int[] neighborOFFSETS = {1, 1,  1, 0,   1, -1,  //flat neighbor offset array
                                                 0, 1,          0, -1,  //layed out here for visuals lol
                                                -1, 1, -1, 0,  -1, -1  //iterations should iterate by 2 and pull both values
    };

    public static final int[] stateLUT = ArrayHelpers.getStateArray();



    public static int getCell(int row, int column, long[] array, int longsPerRow) {  //returns a cell at a given pos on the array
        int desiredLong = (row*longsPerRow) + (column>>6);  //the long in the array being selected
        int longIdx = column&63; //pos of bit along specific long
        
        longIdx = 63-longIdx; // longs increase right to left so we flip our index
        return (int) (array[desiredLong] >> longIdx) & 1; //isolate bit and return it
    }

    public static void setCellAlive(int row, int column, long[] array, int longsPerRow) {  //sets cell alive at coords
        int desiredLong = (row*longsPerRow) + (column>>6);  //the long in the array being selected
        int longIdx = column&63; //pos of bit along specific long
        
        longIdx = 63-longIdx; // longs increase right to left so we flip our index
        array[desiredLong] |= (long) 1 << longIdx;
    }

    public static void setCellDead(int row, int column, long[] array, int longsPerRow) {  //sets cell dead at coords
        int desiredLong = (row*longsPerRow) + (column>>6);  //the long in the array being selected
        int longIdx = column&63; //pos of bit along specific long
        
        longIdx = 63-longIdx; // longs increase right to left so we flip our index
        array[desiredLong] &= ~((long) 1 << longIdx);
    }

    public static void setAllAlive(long[] array, int[] gridInfo) {  //sets all cells to be alive iteratively, good for testing
    
        for (int y = 0; y < gridInfo[2]; y++) {  // just goes through and flips every bit for testing
            for (int x = 0; x < gridInfo[1]; x++) {
                ArrayHelpers.setCellAlive(y, x, array, gridInfo[0]);
            }
        }
    }

    public static void setAllDead(long[] array, int[] gridInfo) {  //same as set all alive but used setCellDead as a base
        for (int y = 0; y < gridInfo[2]; y++) {  // just goes through and flips every bit for testing
            for (int x = 0; x < gridInfo[1]; x++) {
                ArrayHelpers.setCellDead(y, x, array, gridInfo[0]);
            }
        }       
    }

    public static void setAllRandom(float density,long[] array, int[] gridInfo) {  

        Random rng = new Random();  //instance random for random number generation
        for (int row = 0; row < gridInfo[2]; row++) {
            for (int column = 0; column < gridInfo[1]; column++) {
                float rngRoll = rng.nextFloat()*18;  //Generate a random int to compare with density for rng
                if (rngRoll < (Math.clamp(density, 0, 10)*0.4)) {
                    ArrayHelpers.setCellAlive(row, column, array, gridInfo[0]);
                }
            }
        }
    }


    public static int getNeighborHash(int row, int column, long[] array, int[] gridInfo) {
        int neighborHash = 0;  //initialize neighbor has to be added to
        int neighborCount = 0;

        for (int newIdx=0; newIdx<neighborOFFSETS.length; newIdx+=2) {  //iterate through every two indexes of neighbor offets
            int nx = Math.floorMod(column + neighborOFFSETS[newIdx+1], gridInfo[1]);
            int ny = Math.floorMod(row + neighborOFFSETS[newIdx], gridInfo[2]);

            neighborHash |= getCell(ny, nx, array, gridInfo[0]) << (neighborCount+1);  //add in the cell value of every neighbor
            neighborCount += 1;
        }

        neighborHash |= getCell(row, column, array, gridInfo[0]); // add in current bit as state flag for hash

        return neighborHash;  //return generated hash

    }

    public static int[] getStateArray() { // generates initial array for all instances to use
        int[] newArray = new int[512];
        for (int i = 0; i<512; i++) {
            int state = (i & 1); // state of the cell (alive=1/dead=0), it is the starting bit
            int bitCount = Integer.bitCount(i >> 1); // the number of ones in the number ie. the ammount of neighbors
            
            if (state == 0) { // cell is dead
                if (bitCount==3) {  //cell comes alive
                    newArray[i]=1;
            }
        }
            else if (state==1) {
                if (bitCount==2 || bitCount==3) { // note bitcount is increased by one compared to the other here
                    newArray[i]=1;                 // this is due to the state bit being included now                   
                }
            }
        } 
    
    return newArray;  // this is only down once, hopefully...

        }
    

        public static void updateRow(int row, long[] prevGen, long[] newGen ,int[] gridInfo) {
        for (int currentIdx = 0; currentIdx < gridInfo[1]; currentIdx++) {  //iterates through and updates all cells in a row
            int newCellState = stateLUT[ArrayHelpers.getNeighborHash(row, currentIdx, prevGen, gridInfo)];

            if (newCellState==1) {
                ArrayHelpers.setCellAlive(row, currentIdx, newGen, gridInfo[0]);
            }

            else if (newCellState==0) {
                ArrayHelpers.setCellDead(row, currentIdx, newGen, gridInfo[0]);
            }
        }
    }

        public static void updateRows(int startingRow, int numOfRows,long[] prevGen, long[] newGen, int[] gridInfo) {  // updates a number of rows from a given starting row
            for (int row = startingRow; row < (startingRow+numOfRows); row++) {
                updateRow(row, prevGen, newGen, gridInfo);
            }
        }
    }


    




        

