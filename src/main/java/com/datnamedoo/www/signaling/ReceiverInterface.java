package com.datnamedoo.www.signaling;
import com.datnamedoo.www.signaling.Events.EventData;



// interface implemented by classes receiving signals
public interface ReceiverInterface {

    // incoming events go here
    public <T> void processEvent(EventData<T> event);

    // used for listing self to receive events of a certain type
    public void subscribeToEvents();

    
}
