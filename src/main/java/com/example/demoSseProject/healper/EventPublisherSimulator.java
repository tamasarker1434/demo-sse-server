package com.example.demoSseProject.healper;

import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventPublisherSimulator {

    private final Sinks.Many<String> eventSink;
    private final Random random = new Random();

    private final List<Map<String, String>> students = List.of(
            Map.of("id", "std-101", "name", "Alice", "dept", "IT"),
            Map.of("id", "std-102", "name", "Bob", "dept", "Biochemistry"),
            Map.of("id", "std-103", "name", "Charlie", "dept", "Environmental Science"),
            Map.of("id", "std-104", "name", "Diana", "dept", "IT"),
            Map.of("id", "std-105", "name", "Eve", "dept", "Biochemistry")
    );

    private final List<String> activities = List.of(
            "started exam",
            "submitted answer",
            "requested clarification",
            "paused exam",
            "completed exam"
    );

    public EventPublisherSimulator(Sinks.Many<String> eventSink) {
        this.eventSink = eventSink;
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000); // Every 2 seconds
                    Map<String, String> student = students.get(random.nextInt(students.size()));
                    String activity = activities.get(random.nextInt(activities.size()));
                    String timestamp = Instant.now().toString();

                    String event = String.format(
                            "{ \"studentId\": \"%s\", \"name\": \"%s\", \"dept\": \"%s\", \"activity\": \"%s\", \"timestamp\": \"%s\" }",
                            student.get("id"), student.get("name"), student.get("dept"), activity, timestamp);

                    eventSink.tryEmitNext(event);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
