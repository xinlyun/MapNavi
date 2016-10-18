package com.xiaopeng.xmapnavi.bean;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by linzx on 2016/10/14.
 */
@Table(name = "HisItem")
public class HisItem extends Model {
    @Column(name = "type")
    public int type;

    @Column( name = "time")
    public long time;

    @Column( name = "msg",unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String msg ;

    @Column( name = "posiLat")
    public float posiLat ;

    @Column( name = "posiLon")
    public float posiLon;

    @Column( name = "posiName",unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String posiName;

    @Column( name = "posiArt")
    public String posiArt;

}
