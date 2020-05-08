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
EXPOSE 8080