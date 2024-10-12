FROM ubuntu:latest
LABEL authors="songle"
FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Dspring.햣profiles.active=docker", "-jar", "app.jar"]