package com.prajwal.firebasesos;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by PRAJWAL on 25-09-2017.
 */

public class HelperMarker {
    public String uidMarker;
    public String nameMarker;
    public LatLng latLngMarker;

    public HelperMarker(){

    }

    public HelperMarker(String uidMarker, String nameMarker, LatLng latLngMarker) {
        this.uidMarker = uidMarker;
        this.nameMarker = nameMarker;
        this.latLngMarker = latLngMarker;
    }
}
