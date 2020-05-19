FROM maven:3.6.3-jdk-8-slim
RUN mkdir /pbf
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN ["mvn", "package"]
CMD ["java", "-Xmx2g", "-jar", "/usr/src/app/target/nearbyserver-0.0.1-SNAPSHOT.jar"]
