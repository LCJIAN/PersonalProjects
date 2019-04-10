package com.lcjian.drinkwater.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "setting",
        indices = {@Index(value = "unit_id", unique = true)},
        foreignKeys = @ForeignKey(entity = Unit.class, parentColumns = "id", childColumns = "unit_id"))
public class Setting {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    // general
    @ColumnInfo(name = "unit_id")
    public Long unitId;

    @ColumnInfo(name = "intake_goal")
    public Double intakeGoal;

    @ColumnInfo(name = "language")
    public String language;

    // user
    @ColumnInfo(name = "gender")
    public Integer gender;

    @ColumnInfo(name = "weight")
    public Double weight;

    @ColumnInfo(name = "wake_up_time")
    public String wakeUpTime;

    @ColumnInfo(name = "sleep_time")
    public String sleepTime;

    // alert
    @ColumnInfo(name = "reminder_interval")
    public Integer reminderInterval; // minutes

    @ColumnInfo(name = "reminder_mode")
    public Integer reminderMode; // 0:off, 1:mute, 2:auto

    @ColumnInfo(name = "reminder_alert")
    public Boolean reminderAlert;

    @ColumnInfo(name = "further_reminder")
    public Boolean furtherReminder;

    // cup
    @ColumnInfo(name = "cup_capacity")
    public Double cupCapacity;
}