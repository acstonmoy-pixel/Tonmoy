package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parsed_files")
data class ParsedFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val fileType: String, // PDF, XLSX, CSV, DOCX
    val recordCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
