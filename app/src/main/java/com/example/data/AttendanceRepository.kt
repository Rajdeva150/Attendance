package com.example.data

import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val dao: AttendanceDao) {
    val allEntries: Flow<List<AttendanceEntry>> = dao.getAllEntries()

    suspend fun getEntryById(id: Long): AttendanceEntry? = dao.getEntryById(id)

    suspend fun insertEntry(entry: AttendanceEntry): Long = dao.insertEntry(entry)

    suspend fun updateEntry(entry: AttendanceEntry) = dao.updateEntry(entry)

    suspend fun deleteEntry(entry: AttendanceEntry) = dao.deleteEntry(entry)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun insertAll(entries: List<AttendanceEntry>) = dao.insertAll(entries)
}
