package com.xiaopeng.xmapnavi.bean;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by linzx on 2016/11/18.
 */
@Table(name = "WherePoi")
public class WherePoi extends Model{
    @Column(name = "saveType",unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int type;

    @Column(name = "posLat")
    public double posLat;

    @Column(name = "posLon")
    public double posLon;

    @Column(name = "pName")
    public String pName;

    @Column(name = "pDesc")
    public String pDesc;

    @Column( name = "time")
    public long time;

}
