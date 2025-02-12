package com.example.spector.event.channels;

import com.example.spector.event.EventMessage;

public interface EventChannel {
    void handle(EventMessage event);
}
