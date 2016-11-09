package com.xiaopeng.xmapnavi.bean;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by linzx on 2016/11/7.
 */
@Table(name = "Collect")
public class CollectItem  extends Model{
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
}
