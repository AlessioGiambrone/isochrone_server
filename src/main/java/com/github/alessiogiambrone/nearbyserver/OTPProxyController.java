package com.github.alessiogiambrone.nearbyserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class OTPProxyController {

    @Autowired
    OTPProxyService loader;

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String health() throws Exception {
        return "OK";
    }

    @RequestMapping(value = "/isochrone", method = RequestMethod.GET)
    public String isochrone(@RequestParam("cutoffs") Integer[] cutoffs,
                       @RequestParam Double lat,
                       @RequestParam Double lng,
                       @RequestParam String[] modes) throws Exception {
        return loader.computeIsochrone(cutoffs,lat, lng, modes);
    }

}
