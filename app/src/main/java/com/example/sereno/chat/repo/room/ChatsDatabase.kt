package com.example.sereno.chat.repo.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sereno.chat.model.Chat

@Database(entities = [Chat::class], version = 1)
abstract class ChatsDatabase : RoomDatabase() {

    companion object{
        const val CHATS_DB = "sereno.db"
        const val CHATS_TABLE = "chats_table"

    }
    abstract val dao: ChatsDao
}