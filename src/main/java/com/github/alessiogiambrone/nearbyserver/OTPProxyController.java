package com.github.alessiogiambrone.nearbyserver;

import com.github.alessiogiambrone.nearbyserver.dto.Boundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class OTPProxyController {

    @Autowired
    OTPProxyService otpProxyService;

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String health() throws Exception {
        return "OK";
    }

    @RequestMapping(value = "/boundaries", method = RequestMethod.GET)
    public Boundary getBoundaries() throws Exception {
        return otpProxyService.getBoundaries();
    }

    @RequestMapping(value = "/isochrone", method = RequestMethod.GET)
    public String isochrone(@RequestParam("cutoffs") Integer[] cutoffs,
                       @RequestParam Double lat,
                       @RequestParam Double lng,
                       @RequestParam String[] modes) throws Exception {
        return otpProxyService.computeIsochrone(cutoffs,lat, lng, modes);
    }

}
