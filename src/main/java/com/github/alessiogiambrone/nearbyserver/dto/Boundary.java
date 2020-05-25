package com.github.alessiogiambrone.nearbyserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Boundary {

    Double minLat;
    Double maxLat;
    Double minLng;
    Double maxLng;

    public String toString(){
        return "Lat: ["+minLat+" - "+maxLat+"] ___ Lng: ["+minLng+" - "+maxLng+"]";
    }

}
