package com.example.sereno.features.chat.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sereno.features.chat.data.model.Chat

@Database(entities = [Chat::class], version = 1)
abstract class ChatsDatabase : RoomDatabase() {

    companion object{
        const val CHATS_DB = "sereno.db"
        const val CHATS_TABLE = "chats_table"

    }
    abstract val dao: ChatsDao
}