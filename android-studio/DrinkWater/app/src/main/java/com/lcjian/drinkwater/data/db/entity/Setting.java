package com.lcjian.drinkwater.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 设置
 */
@Entity(tableName = "setting",
        indices = {@Index(value = "unit_id", unique = true),
                @Index(value = "cup_id", unique = true)},
        foreignKeys = {@ForeignKey(entity = Unit.class, parentColumns = "id", childColumns = "unit_id")
                , @ForeignKey(entity = Cup.class, parentColumns = "id", childColumns = "cup_id")})
public class Setting {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    // general
    @ColumnInfo(name = "unit_id")
    public Long unitId;          // 当前单位

    @ColumnInfo(name = "intake_goal")
    public Double intakeGoal;    // 每日饮水目标

    @ColumnInfo(name = "language")
    public String language;      // 语言

    // user
    @ColumnInfo(name = "gender")
    public Integer gender;       // 性别

    @ColumnInfo(name = "weight")
    public Double weight;        // 体重

    @ColumnInfo(name = "wake_up_time")
    public String wakeUpTime;    // 起床时间 HH:mm

    @ColumnInfo(name = "sleep_time")
    public String sleepTime;     // 睡觉时间 HH:mm

    // alert
    @ColumnInfo(name = "reminder_interval")
    public Integer reminderInterval; // 提醒间隔 单位分 minutes 30，45，60，90中的一个

    @ColumnInfo(name = "reminder_mode")
    public Integer reminderMode; // 0:off, 1:mute, 2:auto

    @ColumnInfo(name = "reminder_alert")
    public Boolean reminderAlert; // 解锁提醒

    @ColumnInfo(name = "further_reminder")
    public Boolean furtherReminder; // 持续提醒

    // cup
    @ColumnInfo(name = "cup_id")
    public Long cupId;             // 当前默认喝水杯子
}