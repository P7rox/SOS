package com.prajwal.firebasesos;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by PRAJWAL on 14-09-2017.
 */

@IgnoreExtraProperties
public class UserNoLoc {

    public String name;
    public String email;
    public String address1;
    public String address2;
    public String pincode;
    public String phone;


    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public UserNoLoc() {
    }

    public UserNoLoc(String name, String email, String address1, String address2, String pincode, String phone) {
        this.name = name;
        this.email = email;
        this.address1 = address1;
        this.address2 = address2;
        this.pincode = pincode;
        this.phone = phone;
    }
}
