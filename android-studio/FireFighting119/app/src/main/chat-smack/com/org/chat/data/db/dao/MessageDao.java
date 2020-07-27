package com.org.chat.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.org.chat.data.db.entity.Message;
import com.org.chat.data.db.pojo.UnReadCount;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long[] insert(Message... messages);

    @Delete
    void delete(Message... messages);

    @Update
    void update(Message... messages);

    @Query("SELECT * FROM message")
    List<Message> getAll();

    @Query("SELECT * FROM message")
    Flowable<List<Message>> getAllRx();

    @Query("SELECT *"
            + "         FROM message M"
            + "         WHERE M.id = ("
            + "             SELECT DISTINCT id"
            + "             FROM message"
            + "             WHERE (from_entity_bare_jid = M.from_entity_bare_jid AND to_entity_bare_jid = :entityBareJid)"
            + "                 OR (to_entity_bare_jid = M.from_entity_bare_jid AND from_entity_bare_jid = :entityBareJid)"
            + "                 OR (to_entity_bare_jid = M.to_entity_bare_jid AND from_entity_bare_jid = :entityBareJid)"
            + "                 OR (from_entity_bare_jid = M.to_entity_bare_jid AND to_entity_bare_jid = :entityBareJid)"
            + "             ORDER BY create_time DESC"
            + "             LIMIT 0, 1)"
            + "         ORDER BY create_time DESC")
    Flowable<List<Message>> getConversationsRx(String entityBareJid);

    @Query("SELECT *" +
            " FROM message" +
            " WHERE ((from_entity_bare_jid = :bareOwnerJid AND to_entity_bare_jid = :bareOppositeJid)" +
            "     OR (from_entity_bare_jid = :bareOppositeJid AND to_entity_bare_jid = :bareOwnerJid))" +
            " ORDER BY create_time DESC")
    Flowable<List<Message>> getMessagesRx(String bareOwnerJid, String bareOppositeJid);

    @Query("SELECT from_entity_bare_jid, COUNT(*) AS un_read_count" +
            " FROM message" +
            " WHERE (from_entity_bare_jid IN (:bareOppositeJidList) AND to_entity_bare_jid = :bareOwnerJid) AND delivery_status = 4" +
            " GROUP BY from_entity_bare_jid")
    List<UnReadCount> getUnReadMessageCounts(String bareOwnerJid, List<String> bareOppositeJidList);

    @Query("UPDATE message SET delivery_status = 5 " +
            " WHERE from_entity_bare_jid = :bareOppositeJid AND to_entity_bare_jid = :bareOwnerJid")
    void readMessages(String bareOwnerJid, String bareOppositeJid);
}
