package com.example.demoSseProject.simulator;

import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventPublisherSimulator {

    private final Sinks.Many<String> eventSink;
    private final Random random = new Random();

    // Student list
    private final List<Map<String, String>> students = List.of(
            Map.of("id", "std-101", "name", "Alice", "dept", "IT"),
            Map.of("id", "std-102", "name", "Bob", "dept", "Biochemistry"),
            Map.of("id", "std-103", "name", "Charlie", "dept", "Environmental-Science"),
            Map.of("id", "std-104", "name", "Diana", "dept", "IT"),
            Map.of("id", "std-105", "name", "Eve", "dept", "Biochemistry"),
            Map.of("id", "std-106", "name", "Frank", "dept", "Environmental-Science"),
            Map.of("id", "std-107", "name", "Grace", "dept", "IT"),
            Map.of("id", "std-108", "name", "Hank", "dept", "Biochemistry"),
            Map.of("id", "std-109", "name", "Ivy", "dept", "Environmental-Science"),
            Map.of("id", "std-110", "name", "Jack", "dept", "IT"),
            Map.of("id", "std-111", "name", "Karen", "dept", "Biochemistry"),
            Map.of("id", "std-112", "name", "Leo", "dept", "Environmental-Science"),
            Map.of("id", "std-113", "name", "Mona", "dept", "IT"),
            Map.of("id", "std-114", "name", "Nate", "dept", "Biochemistry"),
            Map.of("id", "std-115", "name", "Olivia", "dept", "Environmental-Science")
    );

    private final List<String> midActivities = List.of(
            "requested_clarification",
            "paused_exam",
            "check_online"
    );

    private static class StudentState {
        boolean started = false;
    }

    private final Map<String, StudentState> stateMap = new ConcurrentHashMap<>();

    public EventPublisherSimulator(Sinks.Many<String> sink) {
        this.eventSink = sink;
        for (Map<String, String> s : students) {
            stateMap.put(s.get("id"), new StudentState());
        }
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // emit every second
                } catch (InterruptedException ignored) {}

                Map<String, String> student = students.get(random.nextInt(students.size()));
                StudentState state = stateMap.get(student.get("id"));

                String activity;
                if (!state.started) {
                    activity = "started_exam";
                    state.started = true;
                } else {
                    int decision = random.nextInt(3); // 0, 1 = midActivities, 2 = submit
                    if (decision < 2) {
                        activity = midActivities.get(decision);
                    } else {
                        activity = "submitted_answer";
                        state.started = false; // reset so next activity is "started exam"
                    }
                }

                String event = String.format(
                        "{ \"studentId\": \"%s\", \"name\": \"%s\", \"dept\": \"%s\", \"activity\": \"%s\", \"timestamp\": \"%s\" }",
                        student.get("id"), student.get("name"), student.get("dept"), activity, Instant.now()
                );

                eventSink.tryEmitNext(event);
            }
        }).start();
    }
}
