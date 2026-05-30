package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "voters",
    indices = [
        Index(value = ["nid"], unique = true),
        Index(value = ["serialNumber"]),
        Index(value = ["name"]),
        Index(value = ["division", "district", "upazila", "union", "village", "constituency"])
    ]
)
data class Voter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serialNumber: String,
    val name: String,
    val nid: String,
    val division: String,
    val district: String,
    val upazila: String,
    val union: String,
    val village: String,
    val constituency: String,
    val gender: String = "Male",
    val age: Int = 30,
    val voterArea: String = "",
    val isSynced: Boolean = false
) {
    // Helper to format full address
    fun getFormattedAddress(): String {
        return "$village, $union, $upazila, $district, $division"
    }
}
