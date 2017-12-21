package com.prajwal.firebasesos;

import java.sql.Time;

/**
 * Created by PRAJWAL on 26-09-2017.
 */

public class Item {
    String personName;
    String distance;
    String time;
    String address;
    int personImage;

    public Item(){}

    public Item(String personName,String distance ,String time, String address,int personImage)
    {
        this.personImage=personImage;
        this.personName=personName;
        this.distance=distance;
        this.address=address;
        this.time=time;
    }
    public String getPersonName()
    {
        return personName;
    }
    public String getDistance()
    {
        return distance;
    }
    public String getTime()
    {
        return time;
    }
    public String getAddress() {
        return address;
    }
    public int getPersonImage()
    {
        return personImage;
    }
}
