package com.prajwal.firebasesos;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by PRAJWAL on 25-09-2017.
 */

public class Request {
    public String uid;
    public String name;
    public LatLng latLng;
    public Integer count;

    public Request(){

    }

    public Request(String uid, String name, LatLng latLng, Integer count) {
        this.uid = uid;
        this.name = name;
        this.latLng = latLng;
        this.count = count;
    }
}
