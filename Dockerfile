FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine
VOLUME /tmp
ARG JAR_FILE
ARG MS_PORT
COPY ${JAR_FILE} app.jar
RUN apk --no-cache update && apk --no-cache add fontconfig libfreetype6
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE ${MS_PORT}