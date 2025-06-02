FROM openjdk:8

RUN mkdir /opt/babag-api
COPY target/babag-api-*-standalone.jar /opt/babag-api/babag-api.jar

WORKDIR /opt/babag-api

EXPOSE 8080 7888
CMD ["java", "-jar", "/opt/babag-api/babag-api.jar"]
