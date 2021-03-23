FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
ARG MS_PORT
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djdk.tls.client.protocols=TLSv1.2","-Djdk.tls.client.cipherSuites=TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384","-jar","/app.jar"]
EXPOSE ${MS_PORT}