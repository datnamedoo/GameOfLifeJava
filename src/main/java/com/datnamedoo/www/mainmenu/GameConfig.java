package com.datnamedoo.www.mainmenu;

import com.datnamedoo.www.Renderer.Position;
import com.datnamedoo.www.Renderer.PalettePreset;;


public class GameConfig {

    // enum for available grid dimensions (could probably go higher idk)
    public static enum AvailableDimensions {
        _128x128_(new Position(128, 128)),
        _256x256_(new Position(256, 256)),
        _512x512_(new Position(512, 512)),
        _1024x1024_(new Position(1024, 1024)),
        _2048x2048_(new Position(2048, 2048)),
        _4096x4096_(new Position(4096, 4096)),
        _8192x8192_(new Position(8192, 8192));

        private Position dimensions;

        private AvailableDimensions(Position val) {
            this.dimensions=val;
        }

        public Position getDimensions() {
            return this.dimensions;
        }
    }

    public static enum Threading {
        SINGLETHREADED("-s"),
        MULTITHREADED("-m");

        private String threading;

        private Threading(String threading) {
            this.threading = threading;
        }

        public String getThreading() {
            return this.threading;
        }

    }


    // default values
    private Position gridDimensions = AvailableDimensions._2048x2048_.getDimensions();
    private String threading = "-m";
    private PalettePreset palette = PalettePreset.GALAXY;

    public GameConfig(){} // no constructor

    // setters for settings config
    public void setDimensions(AvailableDimensions dimensions) {
        this.gridDimensions=dimensions.getDimensions();
    }
    public void setThreading(Threading threading) {
        this.threading = threading.getThreading();
    }
    public void setPalette(PalettePreset palette) {
        this.palette=palette;
    }

    // getters for retrieving config
    public Position getDimensions() {
        return this.gridDimensions;
    }
    public String getThreading() {
        return this.threading;
    }
    public PalettePreset getPalette() {
        return this.palette;
    }






}
