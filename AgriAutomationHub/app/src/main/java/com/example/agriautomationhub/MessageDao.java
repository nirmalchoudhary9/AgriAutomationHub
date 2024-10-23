package com.example.agriautomationhub;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM MessageEntity ORDER BY timestamp ASC")
    List<MessageEntity> getAllMessages();

    @Insert
    void insert(MessageEntity message);

    @Query("DELETE FROM MessageEntity")
    void deleteAllMessages();
}
