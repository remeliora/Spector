package com.example.spector.modules.event.channels;

import com.example.spector.modules.event.EventMessage;

public interface EventChannel {
    void handle(EventMessage event);
}
