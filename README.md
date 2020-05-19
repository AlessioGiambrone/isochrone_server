# Isochrone-server

Backend service for the Nearby project.

It's a Java Spring application that wraps OpenTripPlanner for
generating isochrone GeoJsons.

Responses will be cached.

Because relies on OpenTripPlanner, it's pretty heavy and it is convenient to
move away from here as much as logic as possible.

## Run

Give the server the path to your pbf file in an environment variable and start
it:

```bash
PBF_PATH=./my_city.pbf java -jar nearbyserver-0.0.1-SNAPSHOT.jar
```

## Build

```bash
mvn package
```

## Docker

After building with maven, you can continue with the container with just

```bash
docker build -t nearby_geoserver .
```

then you can run it specifying:
- which file to use using the `PBF_PATH` environment variable
- where to get `pbf` files.

In the example below, we're mounting the local directory `pbf_folder` into
container's `/pbf`, and using the `my_city.pbf` inside it.

```bash
docker run --rm -e "PBF_PATH=/pbf/my_city.pbf" -v $(pwd)/pbf_folder:/pbf nearby_geoservice
```
