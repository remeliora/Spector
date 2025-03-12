package com.example.spector.modules.event;

import com.example.spector.modules.event.channels.EventChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventDispatcher {
    private final List<EventChannel> handlers;

//    @PostConstruct
//    public void init() {
//        System.out.println("Registered Event Channels: " + handlers);
//    }

    public void dispatch(EventMessage event) {
//        System.out.println("Dispatching event: " + event.getMessage());
        handlers.forEach(handler -> handler.handle(event));
    }

}
