FROM openjdk:8-jdk-alpine
RUN apk add --no-cache postgresql-client
VOLUME /tmp
ARG JAR_FILE
ARG MS_PORT
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=production","-jar","/app.jar"]
EXPOSE ${MS_PORT}