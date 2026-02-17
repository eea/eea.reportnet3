FROM adoptopenjdk/openjdk11:jdk-11.0.23_9-alpine
VOLUME /tmp
ARG JAR_FILE
ARG MS_PORT
COPY ${JAR_FILE} app.jar
RUN apk update && apk add --no-cache fontconfig ttf-dejavu
ENTRYPOINT ["java","-Xmx4G","-XX:+UseG1GC","-XX:G1HeapRegionSize=32M","-XX:MaxRAMPercentage=70.0","-XX:MinHeapFreeRatio=20","-XX:MaxHeapFreeRatio=40","-XX:+UseStringDeduplication","-jar","/app.jar"]
EXPOSE ${MS_PORT}
