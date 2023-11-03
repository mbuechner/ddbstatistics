FROM maven:3-openjdk-17-slim AS MAVEN_CHAIN
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package

FROM debian:bookworm-slim
ENV TZ=Europe/Berlin
ENV DDBSTATISTICS.PORT=8080
RUN apt-get -y update && apt-get -y install openjdk-17-jre && mkdir /home/ddbstatistics
COPY --from=MAVEN_CHAIN /tmp/target/ddbstatistics.jar /home/ddbstatistics/ddbstatistics.jar
WORKDIR /home/ddbid/
CMD ["java", "-jar", "ddbstatistics.jar"]

EXPOSE 8080
