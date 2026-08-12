package com.datnamedoo.www.signaling;

import java.util.function.Consumer;
import com.datnamedoo.www.signaling.Events.EventData;
import com.datnamedoo.www.signaling.Events.EventDataInt;

// portable versino of receiver interface
// it can be instanced, and allows static class/methods to attach to signals
public class PortableReceiver implements ReceiverInterface {

    private Consumer<EventDataInt> forward;
    private EventInterface event;

    public PortableReceiver(Consumer<EventDataInt> forward, EventInterface event) {
        this.forward = forward;
        this.event = event;
        subscribeToEvents();
    }

    @Override
    public <T> void processEvent(EventData<T> event) {
        // forwards event to linked function for handling
        forwardEvent(event);
    }

    @Override  // we subscribe this class to accept events on the parent classe's behalf
    public void subscribeToEvents() {
        Signaling.subscribeToEvent(event, this);
    }

    // forwards to linked function
    private <T> void forwardEvent(EventData<T> evenData) {
        forward.accept(evenData);
    }
    
}
