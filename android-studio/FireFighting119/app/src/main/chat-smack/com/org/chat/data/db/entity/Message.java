package com.org.chat.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "message", indices = {@Index(value = "stanza_id", unique = true)/*, @Index(value = "address", unique = true)*/})
public class Message {

    public static final int DS_NEW = 0; // this message has not been sent
    public static final int DS_SENT = 1; // this message was sent but not yet acked
    public static final int DS_ACKED = 2; // this message was XEP-0184 acknowledged
    public static final int DS_FAILED = 3; // this message was returned as failed
    public static final int DS_RECEIVED = 4; // this message was received but not displayed yet
    public static final int DS_READ = 5; // this message was received and read

    public static final int LS_NEW = 0;
    public static final int LS_CLICKED = 1;

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "stanza_id")
    public String stanzaId;

    @ColumnInfo(name = "from_entity_bare_jid")
    public String fromEntityBareJid;

    @ColumnInfo(name = "to_entity_bare_jid")
    public String toEntityBareJid;

    @ColumnInfo(name = "from_resource")
    public String fromResource;

    @ColumnInfo(name = "to_resource")
    public String toResource;

    @ColumnInfo(name = "message_type")
    public Integer messageType;

    @ColumnInfo(name = "body")
    public String body;

    @ColumnInfo(name = "other_bodies")
    public String otherBodies;

    @ColumnInfo(name = "delivery_status")
    public Integer deliveryStatus;

    @ColumnInfo(name = "local_status")
    public Integer localStatus;

    @ColumnInfo(name = "create_time")
    public Date createTime;
}
