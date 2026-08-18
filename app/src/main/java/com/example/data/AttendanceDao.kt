package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<AttendanceEntry>>

    @Query("SELECT * FROM attendance_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): AttendanceEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: AttendanceEntry): Long

    @Update
    suspend fun updateEntry(entry: AttendanceEntry)

    @Delete
    suspend fun deleteEntry(entry: AttendanceEntry)

    @Query("DELETE FROM attendance_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM attendance_entries")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<AttendanceEntry>)
}
