FROM openjdk:8
RUN mkdir /pbf
COPY . /usr/src/app
WORKDIR /usr/src/app
CMD ["java", "-Xmx2g", "-jar", "/usr/src/app/target/nearbyserver-0.0.1-SNAPSHOT.jar"]
