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
        try {
            List<LocationSaver> locationSavers = null;
            try {
                locationSavers = new Select()
                        .from(LocationSaver.class)
                        .limit(1)
                        .execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (locationSavers == null || locationSavers.size() < 1) {
                return null;
            } else {
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
        }catch (Exception e){
            e.printStackTrace();
            return null;
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

    /**
     * 第一次开启可能无法快速定位，以次数据为定位第一次数据
     */
    public static void saverFirst(){
        try {
            LocationSaver locationSaver = new LocationSaver();
            locationSaver.id = 0;
            locationSaver.lat = 23.1535488735f;
            locationSaver.lon = 113.4962516729f;
            locationSaver.city = "广州";
            locationSaver.cityCode = "0755";
            locationSaver.bearing = 0;
            locationSaver.address = "小鹏汽车公司";
            locationSaver.save();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
