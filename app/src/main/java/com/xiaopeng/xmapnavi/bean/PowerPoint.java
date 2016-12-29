package com.xiaopeng.xmapnavi.bean;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by linzx on 2016/12/28.
 */
@Table(name = "PowerPoint")
public class PowerPoint extends Model{
    @Column(name = "poiId",unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String poiId;

    @Column(name = "name")
    public String name;

    @Column(name = "city")
    public String city;

    @Column(name =  "imgUrl")
    public String imgUrl;

    @Column(name = "acCnt")
    public int acCnt;

    @Column(name = "acUseCnt")
    public int acUseCnt;

    @Column(name = "acIdleCnt")
    public int acIdleCnt;

    @Column(name = "acErrorCnt")
    public int acErrorCnt;

    @Column(name = "dcCnt")
    public int dcCnt;

    @Column(name = "dcUseCnt")
    public int dcUseCnt;

    @Column(name = "dcIdleCnt")
    public int dcIdleCnt;

    @Column(name = "dcErrorCnt")
    public int dcErrorCnt;

    @Column(name = "timeDesc")
    public String timeDesc;

    @Column(name = "address")
    public String address;

    @Column(name = "lat")
    public double lat;

    @Column(name = "lon")
    public double lon;

    @Column(name = "distance")
    public int distance;

    @Column(name = "electricFee")
    public float electricFee;

    @Column(name = "serviceFee")
    public float serviceFee;

    @Column(name = "type")
    public int type;

    @Column(name = "notice")
    public String notice;

    public float getElectricFee() {
        return electricFee;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public float getServiceFee() {
        return serviceFee;
    }

    public int getAcCnt() {
        return acCnt;
    }

    public int getAcErrorCnt() {
        return acErrorCnt;
    }

    public int getAcIdleCnt() {
        return acIdleCnt;
    }

    public int getDcCnt() {
        return dcCnt;
    }

    public int getDcErrorCnt() {
        return dcErrorCnt;
    }

    public int getDcIdleCnt() {
        return dcIdleCnt;
    }

    public int getDcUseCnt() {
        return dcUseCnt;
    }

    public int getDistance() {
        return distance;
    }

    public String getTimeDesc() {
        return timeDesc;
    }

    public int getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getName() {
        return name;
    }

    public String getNotice() {
        return notice;
    }

    public String getPoiId() {
        return poiId;
    }

}
