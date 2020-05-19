package com.github.alessiogiambrone.nearbyserver;

import java.io.File;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.opentripplanner.analyst.core.IsochroneData;
import org.opentripplanner.analyst.request.IsoChroneRequest;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.graph_builder.module.osm.DefaultWayPropertySetSource;
import org.opentripplanner.graph_builder.module.osm.OpenStreetMapModule;
import org.opentripplanner.openstreetmap.impl.BinaryFileBasedOpenStreetMapProviderImpl;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.DefaultStreetVertexIndexFactory;
import org.opentripplanner.standalone.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.*;

import static org.opentripplanner.api.resource.LIsochrone.makeContourFeatures;

@Data
@Component
public class OTPProxyService {

    private static Logger logger = LoggerFactory.getLogger(OTPProxyService.class);

    private HashMap<Class<?>, Object> extra = new HashMap<Class<?>, Object>();
    private Router router;
    private Graph gg;
    private RoutingRequest routingRequest;

    private String getPbfPath(String pbf) {
        File f = new File(pbf);
        if (f.isDirectory()){
          File[] listOfFiles = f.listFiles();

          for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].toString().toLowerCase().endsWith("pbf")){
              return listOfFiles[i].toString();
            }
          }

        }
        return pbf;
    }


    public OTPProxyService(@Value("${pbf.path}") String pbf) {
        pbf = getPbfPath(pbf);
        logger.info("loading file "+pbf);
        gg = new Graph();
        OpenStreetMapModule loader = getLoader(pbf);
        loader.buildGraph(gg, extra);
        gg.index(new DefaultStreetVertexIndexFactory());
        router = new Router("TEST", gg);
        Map ggConfig = new HashMap();
        ObjectMapper mapper = new ObjectMapper();
        router.startup(mapper.convertValue(ggConfig, JsonNode.class));
    }

    private OpenStreetMapModule getLoader(@Value("${pbf.path}") String pbf) {
        OpenStreetMapModule loader = new OpenStreetMapModule();
        loader.setDefaultWayPropertySetSource(new DefaultWayPropertySetSource());
        BinaryFileBasedOpenStreetMapProviderImpl provider = new BinaryFileBasedOpenStreetMapProviderImpl();

        pbf = getPbfPath(pbf);
        File f = new File(pbf);
        provider.setPath(f);
        loader.setProvider(provider);
        return loader;
    }

    private void initializeRoutingRequest(GenericLocation coords, TraverseModeSet modes) {
        routingRequest = new RoutingRequest();
        //routingRequest.setNumItineraries(1);
        long dateTime = System.currentTimeMillis() / 1000l - 24*60*60;
        routingRequest.setArriveBy(dateTime < 0);
        routingRequest.dateTime = Math.abs(dateTime);
        routingRequest.batch = true;
        routingRequest.from = coords;
        //routingRequest.setWheelchairAccessible(false);
        //routingRequest.transferPenalty = ( 300);
        routingRequest.setModes(modes);
        /*routingRequest.setOtherThanPreferredRoutesPenalty(0);
        // The walk board cost is set low because it interferes with test 2c1.
        // As long as boarding has a very low cost, waiting should not be "better" than riding
        // since this makes interlining _worse_ than alighting and re-boarding the same line.
        // TODO rethink whether it makes sense to weight waiting to board _less_ than 1.
        routingRequest.setWaitReluctance(1);
        routingRequest.setWalkBoardCost(30);*/
        routingRequest.setRoutingContext(gg);
    }

    private TraverseModeSet getMode(String mode){
        List<TraverseMode> tms = new ArrayList<>();
        tms.add(TraverseMode.valueOf(mode));
        return new TraverseModeSet(tms);
    }

    public Object getIsochrones(IsoChroneRequest isoChroneRequest, String mode) throws IOException {
        initializeRoutingRequest(
                new GenericLocation(isoChroneRequest.coordinateOrigin),
                getMode(mode)
        );
        List<IsochroneData> isochrones =
                getRouter().isoChroneSPTRenderer.getIsochrones(isoChroneRequest, routingRequest);
        SimpleFeatureCollection contourFeatures = makeContourFeatures(isochrones);
        StringWriter writer = new StringWriter();
        FeatureJSON fj = new FeatureJSON();
        fj.writeFeatureCollection(contourFeatures, writer);
        return writer.toString();
    }

    @Cacheable(value = "isochrone")
    public String computeIsochrone(Integer[] cutoffs,
        Double lat,
        Double lng,
        String[] modes) throws Exception {
        IsoChroneRequest isoChroneRequest = new IsoChroneRequest(Arrays.asList(cutoffs));
        isoChroneRequest.coordinateOrigin = new GenericLocation(lat, lng).getCoordinate();
        isoChroneRequest.includeDebugGeometry = true;
        isoChroneRequest.precisionMeters = 100;
        isoChroneRequest.offRoadDistanceMeters = 150;
        isoChroneRequest.maxTimeSec = 5000000;

        // I'd really like to generate the response in a better way
        String result = "{";
        List resultList = new ArrayList<String>();
        for (int i = 0; i<modes.length; i++){
          // geojson object will be serialized as string....
          // and if we try to serialize it will recurse to exception.
          //result.put(modes[i], loader.getIsochrones(isoChroneRequest, modes[i]));
          String temp = "\""+modes[i]+"\""+":"+(String) getIsochrones(isoChroneRequest, modes[i]);
          resultList.add(temp);
        }
        result = result + String.join(",", resultList);
        return result+"}";
    }

}
