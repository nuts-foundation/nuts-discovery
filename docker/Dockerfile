FROM adoptopenjdk/openjdk11:alpine-slim as builder
LABEL maintainer="wout.slakhorst@nuts.nl"
RUN mkdir /opt/nuts
COPY / /opt/nuts/
WORKDIR /opt/nuts
RUN ./gradlew bootJar

FROM adoptopenjdk/openjdk11:alpine-slim
COPY --from=builder /opt/nuts/build/libs/nuts-discovery.jar /opt/nuts/discovery/bin/nuts-discovery.jar
COPY docker/application.properties /opt/nuts/discovery/
WORKDIR /opt/nuts/discovery
CMD ["java", "-jar", "/opt/nuts/discovery/bin/nuts-discovery.jar"]
EXPOSE 8080