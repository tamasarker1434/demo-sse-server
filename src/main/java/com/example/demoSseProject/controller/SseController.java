package com.example.demoSseProject.controller;

import com.example.demoSseProject.simulator.EventPublisherSimulator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
public class SseController {

    private final EventPublisherSimulator simulator;

    public SseController(Sinks.Many<String> sink) {
        this.simulator = new EventPublisherSimulator(sink);
    }

    @GetMapping(value = "/sse/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream() {
        simulator.startEmitting();
        return simulator.getEventSink().asFlux()
                .doFinally(signalType -> simulator.stopEmitting());
    }
}




