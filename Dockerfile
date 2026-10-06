# syntax=docker/dockerfile:1

# ---------- build stage ----------
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /build
COPY saludya/pom.xml .
COPY saludya/src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- runtime stage ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/target/saludya-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
