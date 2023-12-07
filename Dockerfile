FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine
VOLUME /tmp
ARG JAR_FILE
ARG MS_PORT
COPY ${JAR_FILE} app.jar
RUN apk update && apk add --no-cache fontconfig ttf-dejavu
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE ${MS_PORT}
