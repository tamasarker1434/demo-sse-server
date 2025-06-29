# Use a lightweight JDK base image
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy Maven build artifact
COPY target/demo-sse-server-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8071

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
