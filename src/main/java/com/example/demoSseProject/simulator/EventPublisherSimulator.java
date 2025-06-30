package com.example.demoSseProject.simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Sinks;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventPublisherSimulator {

    private final Sinks.Many<String> eventSink;
    private final Random rnd = ThreadLocalRandom.current();

    private List<Map<String, String>> entities = List.of();
    private List<String> activities = List.of();
    private String template = "";
    private int intervalMs = 1000;

    public EventPublisherSimulator(Sinks.Many<String> sink) {
        this.eventSink = sink;
        loadConfig();
    }

    private void loadConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getClassLoader().getResourceAsStream("dataFiles/stream-config.json");

            if (is == null)
                throw new RuntimeException("Missing stream-config.json in resources/dataFiles");

            JsonNode root = mapper.readTree(is);

            // Load entities from "data" field
            JsonNode dataNode = root.get("data");
            if (dataNode == null || !dataNode.isArray() || dataNode.size() == 0)
                throw new IllegalStateException("Missing or invalid 'data' array in JSON");

            entities = mapper.convertValue(dataNode, new TypeReference<List<Map<String, String>>>() {});

            // Load activities
            JsonNode activitiesNode = root.get("activities");
            if (activitiesNode != null && activitiesNode.isArray()) {
                activities = mapper.convertValue(activitiesNode, new TypeReference<List<String>>() {});
            }

            // Load event template
            template = Optional.ofNullable(root.get("eventTemplate"))
                    .map(JsonNode::asText)
                    .orElseThrow(() -> new IllegalStateException("Missing 'eventTemplate' in JSON"));

            // Load emit interval
            JsonNode intervalNode = root.path("streamPattern").path("emitIntervalMs");
            if (intervalNode.isInt()) {
                intervalMs = intervalNode.asInt();
            }

            System.out.printf("✔ Loaded %d entities, %d activities, interval %d ms%n",
                    entities.size(), activities.size(), intervalMs);

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load stream-config.json", e);
        }
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException ignored) {}

                Map<String, String> ent = entities.get(rnd.nextInt(entities.size()));
                String activity = activities.isEmpty() ? "unknown_activity" : activities.get(rnd.nextInt(activities.size()));
                String timestamp = Instant.now().toString();

                Map<String, String> context = new HashMap<>(ent);
                context.put("activity", activity);
                context.put("timestamp", timestamp);

                String event = render(template, context);
                Sinks.EmitResult result = eventSink.tryEmitNext(event);

                if (result.isFailure()) {
                    System.err.println("⚠ Failed to emit event: " + result);
                }
            }
        }, "sse-simulator-thread").start();
    }


    // Regex for Mustache-style {{key}} placeholders
    private static final Pattern token = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");

    // Replaces {{key}} with corresponding value from context
    private static String render(String tpl, Map<String, String> ctx) {
        Matcher m = token.matcher(tpl);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1).trim();
            String val = ctx.getOrDefault(key, "");
            m.appendReplacement(sb, Matcher.quoteReplacement(val));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
