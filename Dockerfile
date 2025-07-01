# Dockerfile for demo-sse-server

# Use an official JDK image as base
FROM openjdk:21

# Set the working directory
WORKDIR /app

# Copy the Maven build output (adjust name if needed)
COPY target/demo-sse-server-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot app listens on
EXPOSE 8071

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
