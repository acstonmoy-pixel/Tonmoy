package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ParsedFile
import kotlinx.coroutines.flow.Flow

@Dao
interface ParsedFileDao {

    @Query("SELECT * FROM parsed_files ORDER BY timestamp DESC")
    fun getAllParsedFiles(): Flow<List<ParsedFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParsedFile(parsedFile: ParsedFile)

    @Query("DELETE FROM parsed_files")
    suspend fun clearAllLogs()
}
