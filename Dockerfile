FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
ARG MS_PORT
COPY ${JAR_FILE} app.jar
ENV TLS_PROTOCOL="TLSv1.2"
ENTRYPOINT ["java","-Djdk.tls.client.protocols=${TLS_PROTOCOL}","-jar","/app.jar"]
EXPOSE ${MS_PORT}