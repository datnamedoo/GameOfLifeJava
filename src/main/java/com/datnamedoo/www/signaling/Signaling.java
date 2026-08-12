package com.datnamedoo.www.signaling;

import java.util.HashMap;

import com.datnamedoo.www.signaling.Events.EventData;

public class Signaling { 
    
    // Maps event types to event interfaces
    private static HashMap<Integer, ReceiverInterface> subscriberMap = new HashMap<>();

    public static void subscribeToEvent(EventInterface eventType, ReceiverInterface subscriber) {
        subscriberMap.put(eventType.getEventVal(), subscriber); // subscribers to event type 
    }

    public static void unsubscribeFromEvent(EventInterface eventType, ReceiverInterface subscriber) {
        subscriberMap.remove(eventType.getEventVal()); // unsubscibe from event
    }

    public static <T> void broadcastEvent(EventInterface event, T data) {
        EventData<T> eventData = new EventData<>(event, data);
        ReceiverInterface receiver = subscriberMap.get(eventData.event().getEventVal()); // get receiver for givent event type
        if (receiver == null) {
            System.out.println("No Active Receiver -> " + event);  // receiver not set for sent signal
            return;


        } // if receiver not set return
        receiver.<T>processEvent(eventData);
    }
    



}

