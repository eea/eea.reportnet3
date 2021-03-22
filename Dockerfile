FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
ARG MS_PORT
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","Djdk.tls.client.protocols='TLSv1.2'","-jar","/app.jar"]
EXPOSE ${MS_PORT}