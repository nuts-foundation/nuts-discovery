FROM adoptopenjdk/openjdk11:alpine-slim as builder
LABEL maintainer="wout.slakhorst@nuts.nl"
RUN mkdir /opt/nuts
COPY / /opt/nuts/
RUN cd /opt/nuts && ./gradlew clean bootJar

FROM adoptopenjdk/openjdk11:alpine-slim
COPY --from=builder /opt/nuts/build/libs/nuts-discovery.jar /opt/nuts/discovery/bin/nuts-discovery.jar
COPY docker/application.properties /opt/nuts/discovery/conf/
COPY docker/keys/* /opt/nuts/discovery/keys/
CMD ["java", "-jar", "/opt/nuts/discovery/bin/nuts-discovery.jar", "--spring.config.location=file:/opt/nuts/discovery/conf/application.properties"]
HEALTHCHECK --start-period=30s --timeout=5s --interval=10s \
    CMD wget -q -O - http://localhost:8080/actuator/health | grep UP || exit 1
EXPOSE 8080