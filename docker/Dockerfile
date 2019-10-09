FROM openjdk:8-jdk-slim as builder
LABEL maintainer="wout.slakhorst@nuts.nl"
RUN mkdir /opt/nuts
COPY / /opt/nuts/
RUN cd /opt/nuts && ./gradlew bootJar

FROM openjdk:8-jre-slim
COPY --from=builder /opt/nuts/build/libs/nuts-discovery.jar /opt/nuts/discovery/bin/nuts-discovery.jar
COPY docker/application.properties /opt/nuts/discovery/
CMD ["java", "-jar", "/opt/nuts/discovery/nuts-discovery.jar", "--spring.config.location=file:/opt/nuts/discovery/application.properties"]
EXPOSE 8080