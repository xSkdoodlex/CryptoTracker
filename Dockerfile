FROM openjdk:latest

ARG JAR_FILE=target/*.jar

COPY target/KryptoTracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app.jar"]