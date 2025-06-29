package com.example.demoSseProject;

import com.example.demoSseProject.healper.EventPublisherSimulator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Sinks;

@SpringBootApplication
public class DemoSseProjectApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(DemoSseProjectApplication.class, args);

		// Get the event sink bean
		Sinks.Many<String> eventSink = context.getBean("eventSink", Sinks.Many.class);

		// Start publishing random events
		EventPublisherSimulator simulator = new EventPublisherSimulator(eventSink);
		simulator.start();
	}

	@Bean(name = "eventSink")
	public Sinks.Many<String> eventSink() {
		return Sinks.many().multicast().onBackpressureBuffer();
	}
}

