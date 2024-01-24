FROM ubuntu:latest
LABEL authors="rahul"

ENTRYPOINT ["top", "-b"]

FROM adoptopenjdk/openjdk11:alpine

COPY build/libs/MapEngine-1.0-SNAPSHOT.jar /app.jar

CMD ["java", "-jar", "/app.jar"]
