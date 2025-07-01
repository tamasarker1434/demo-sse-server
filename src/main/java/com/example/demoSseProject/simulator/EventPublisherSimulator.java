package com.example.demoSseProject.simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.FluxSink;

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

    private volatile boolean running = false;
    private Thread emitThread;

    public EventPublisherSimulator(Sinks.Many<String> sink) {
        this.eventSink = sink;
        loadConfig();
    }
    public Sinks.Many<String> getEventSink() {
        return eventSink;
    }
    private void loadConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getClassLoader().getResourceAsStream("dataFiles/stream-config.json");

            if (is == null)
                throw new RuntimeException("Missing stream-config.json in resources/dataFiles");

            JsonNode root = mapper.readTree(is);
            entities = mapper.convertValue(root.get("data"), new TypeReference<List<Map<String, String>>>() {});
            activities = mapper.convertValue(root.get("activities"), new TypeReference<List<String>>() {});
            template = root.path("eventTemplate").asText();
            intervalMs = root.path("streamPattern").path("emitIntervalMs").asInt(1000);

            System.out.printf("✔ Loaded %d entities, %d activities, interval %d ms%n",
                    entities.size(), activities.size(), intervalMs);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load stream-config.json", e);
        }
    }

    /** Called when a subscriber connects */
    public synchronized void startEmitting() {
        if (running) return;

        running = true;
        emitThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    break;
                }

                Map<String, String> ent = entities.get(rnd.nextInt(entities.size()));
                String activity = activities.get(rnd.nextInt(activities.size()));
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
        }, "event-simulator-thread");
        emitThread.setDaemon(true);
        emitThread.start();
    }

    /** Called when the subscriber disconnects */
    public synchronized void stopEmitting() {
        running = false;
        if (emitThread != null) {
            emitThread.interrupt();
            emitThread = null;
        }
    }

    private static final Pattern token = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");

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
