package com.datnamedoo.www.mainmenu;
// java class for holding frames of grid demo
// useful for json serialization
public class DemoFrame {
    private long[] grid;
    private int frameNum;

    public DemoFrame() {}

    public int getFrameNum() {
    return this.frameNum;
    }

    public long[] getGrid() {
        return this.grid;
    }

    public void setGrid(long[] grid) {
        this.grid = grid;
    }

    public void setFrameNum(int frameNum) {
    this.frameNum = frameNum;
}


}
