package com.datnamedoo.www;
import com.datnamedoo.www.signaling.EventInterface;

public class AppEvents {
    public static enum GameLoopEvent implements EventInterface {
        SETPAUSE(0),
        SETSPEED(1);

        private int eventVal = 1;

        private GameLoopEvent(int val) {
            this.eventVal = val;
        }


        @Override
        public int getEventVal() {
            return this.eventVal;
        }
    }

      public static enum RendererEvent implements EventInterface {
        SETSKIN(2),
        SETSCALE(3),
        PANFROMPOS(4),
        INCREMENTANCHOR(5),        
        NEWGRID(6),
        SETPAN(7),
        PANSTOPPED(8);

        private int eventVal;

        private RendererEvent(int val) {
            this.eventVal = val;
        }


        @Override
        public int getEventVal() {
            return this.eventVal;
        }
        

        
    }
    public static enum ProcessEvent implements EventInterface {
        NEWGRID(9),
        LOADCONFIG(10);

        
        private int eventVal;

        private ProcessEvent(int val) {
            this.eventVal = val;
        }


        @Override
        public int getEventVal() {
            return this.eventVal;
        }
        

    
}
}
