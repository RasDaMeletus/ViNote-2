package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.GoalItem
import com.example.data.model.TransactionItem

@Database(
    entities = [TransactionItem::class, GoalItem::class],
    version = 1,
    exportSchema = false
)
abstract class ViNoteDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: ViNoteDatabase? = null

        fun getDatabase(context: Context): ViNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ViNoteDatabase::class.java,
                    "vinote_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
