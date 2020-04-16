FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/*.jar ApiGatewayApplication.jar


# Monitoring
RUN wget https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-6.6.1-linux-x86_64.tar.gz
RUN tar -xzf filebeat-6.6.1-linux-x86_64.tar.gz
RUN rm filebeat-6.6.1-linux-x86_64.tar.gz

COPY filebeat.yml /filebeat-6.6.1-linux-x86_64/filebeat.yml
#COPY entrypoint.sh /bin/entrypoint.sh
#RUN chmod +x /bin/entrypoint.sh
ENTRYPOINT ["java","-Dspring.profiles.active=production","-jar","/ApiGatewayApplication.jar"]
CMD ["which","/filebeat-6.6.1-linux-x86_64/filebeat"]
#CMD "java -jar ./ApiGatewayApplication.jar"
