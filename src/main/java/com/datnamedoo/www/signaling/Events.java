package com.datnamedoo.www.signaling;

// stores enums for event types
public class Events {
    // data for event, must assign data type upon creation


    public interface EventDataInt{
        public EventInterface getEvent();
        public Object getData();
    }
    public record EventData<T>  (EventInterface event, T data) implements EventDataInt{

        @Override
        public EventInterface getEvent() {
            return this.event;
        }

        @Override
        public T getData() {
            return this.data;
        }}
}


