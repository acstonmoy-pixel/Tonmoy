package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ParsedFile
import com.example.data.model.Voter

@Database(entities = [Voter::class, ParsedFile::class], version = 1, exportSchema = false)
abstract class VoterDatabase : RoomDatabase() {

    abstract fun voterDao(): VoterDao
    abstract fun parsedFileDao(): ParsedFileDao

    companion object {
        @Volatile
        private var INSTANCE: VoterDatabase? = null

        fun getDatabase(context: Context): VoterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoterDatabase::class.java,
                    "voter_search_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
