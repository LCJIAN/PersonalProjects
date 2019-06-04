package com.lcjian.drinkwater.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 单位
 */
@Entity(tableName = "unit", indices = {@Index(value = "name", unique = true)})
public class Unit {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    // kg,ml和lbs,fl oz；逗号分隔，前面是体重单位，后面是杯子容量单位
    @ColumnInfo(name = "name")
    public String name;

    // 1,1和0.45359237,0.033814；逗号分隔，前面是体重比率，后面是杯子容量单位比率
    @ColumnInfo(name = "rate")
    public String rate;

}
