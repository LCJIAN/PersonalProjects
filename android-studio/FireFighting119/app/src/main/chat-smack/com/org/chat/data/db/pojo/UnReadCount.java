package com.org.chat.data.db.pojo;

import androidx.room.ColumnInfo;

public class UnReadCount {

    @ColumnInfo(name = "from_entity_bare_jid")
    public String fromEntityBareJid;

    @ColumnInfo(name = "un_read_count")
    public Integer unReadCount;
}
