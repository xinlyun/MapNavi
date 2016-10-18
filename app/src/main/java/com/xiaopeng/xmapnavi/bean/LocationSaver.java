package com.xiaopeng.xmapnavi.bean;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.amap.api.location.AMapLocation;

import java.util.List;

/**
 * Created by linzx on 2016/10/18.
 */
@Table(name = "LocationSaver")
public class LocationSaver extends Model {

    @Column(name = "mid",unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int id;

    @Column(name = "lat")
    public double lat;

    @Column(name = "lon")
    public double lon;

    @Column(name = "city")
    public String city;

    @Column(name = "cityCode")
    public String cityCode;

    @Column(name = "bearing")
    public float bearing;

    @Column(name = "address")
    public String address;

//    @Column(name = "str")
//    public String str;

    public static AMapLocation getSaveLocation(){
        List<LocationSaver> locationSavers = new Select()
                .from(LocationSaver.class)
                .limit(1)
                .execute();
        if (locationSavers.size() < 1){
            return null;
        }
        else {
            LocationSaver saver = locationSavers.get(0);
            AMapLocation aMapLocation = new AMapLocation("");
            aMapLocation.setLatitude(saver.lat);
            aMapLocation.setLongitude(saver.lon);
            aMapLocation.setCity(saver.city);
            aMapLocation.setCityCode(saver.cityCode);
            aMapLocation.setBearing(saver.bearing);
            aMapLocation.setAddress(saver.address);
            return aMapLocation;
        }
    }

    public static LocationSaver saveNewLocation(AMapLocation location){
        LocationSaver locationSaver = new LocationSaver();
        locationSaver.id = 0;
        locationSaver.lat = location.getLatitude();
        locationSaver.lon = location.getLongitude();
        locationSaver.city = location.getCity();
        locationSaver.cityCode = location.getCityCode();
        locationSaver.bearing = location.getBearing();
        locationSaver.address = location.getAddress();
        return locationSaver;
    }



}
