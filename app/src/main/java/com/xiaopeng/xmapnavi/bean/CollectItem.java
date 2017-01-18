package com.xiaopeng.xmapnavi.bean;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by linzx on 2016/11/7.
 */
@Table(name = "Collect")
public class CollectItem  extends Model{

    public static final int STYLE_COLLECT = 0,STYLE_STUB = 1;

    @Column(name = "posLat")
    public double posLat;

    @Column(name = "posLon")
    public double posLon;

    @Column(name = "pName",unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String pName;

    @Column(name = "pDesc")
    public String pDesc;

    @Column( name = "time")
    public long time;

    @Column( name = "style")
    public int style = STYLE_COLLECT;

    @Column( name = "styleMsg")
    public String styleMsg;



    public static void saveCollectByPowerPoi(PowerPoint powerPoint){
        CollectItem collectItem = new CollectItem();
        collectItem.pName = powerPoint.getName();
        collectItem.posLat = powerPoint.getLat();
        collectItem.posLon = powerPoint.getLon();
        collectItem.pDesc = powerPoint.getAddress();
        collectItem.style = CollectItem.STYLE_STUB;
        collectItem.styleMsg = powerPoint.getPoiId();
        collectItem.save();
    }

    public static void delectCollectByPowerPoi(PowerPoint powerPoint){
        List<CollectItem> list = new Select()
                .from(CollectItem.class)
                .where("styleMsg = ?",powerPoint.getPoiId())
                .execute();
        for (CollectItem poi: list){
            poi.delete();
        }
    }

}
