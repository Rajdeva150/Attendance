package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_entries")
data class AttendanceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD format
    val type: String, // "duty", "leave", "eleave"
    val inTime: String? = null, // "HH:mm"
    val outTime: String? = null, // "HH:mm"
    val otHours: Double = 0.0,
    val dutyHours: Double = 0.0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
