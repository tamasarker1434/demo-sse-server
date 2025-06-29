# ---------- build stage ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /proj
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# ---------- runtime image ----------
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /proj/target/demo-sse-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8071
ENTRYPOINT ["java", "-jar", "app.jar"]
