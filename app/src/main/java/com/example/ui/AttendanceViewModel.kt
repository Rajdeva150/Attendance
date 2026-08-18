package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceEntry
import com.example.data.AttendanceRepository
import com.example.data.DutyCalculations
import com.example.data.SalarySettings
import com.example.data.SettingsRepository
import com.example.data.SummaryData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class FormState(
    val editingId: Long? = null,
    val date: String = LocalDate.now().toString(),
    val type: String = "duty", // "duty", "leave", "eleave"
    val inTime: String = "09:00",
    val outTime: String = "18:00",
    val otHours: String = "0",
    val note: String = ""
) {
    val dutyHours: Double
        get() = if (type == "duty") DutyCalculations.calculateDutyHours(inTime, outTime) else 0.0

    val otHoursDouble: Double
        get() = otHours.toDoubleOrNull() ?: 0.0
}

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AttendanceRepository(database.attendanceDao())
    private val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<SalarySettings> = settingsRepository.settings

    val allEntries: StateFlow<List<AttendanceEntry>> = repository.allEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedMonthFilter = MutableStateFlow("ALL") // "ALL" or "YYYY-MM"
    val selectedMonthFilter: StateFlow<String> = _selectedMonthFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("ALL") // "ALL", "duty", "leave", "eleave"
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    // Filtered entries for attendance list
    val filteredEntries: StateFlow<List<AttendanceEntry>> = combine(
        allEntries,
        _selectedMonthFilter,
        _selectedTypeFilter
    ) { entries, month, type ->
        entries.filter { entry ->
            val matchesMonth = if (month == "ALL") true else entry.date.startsWith(month)
            val matchesType = if (type == "ALL") true else entry.type == type
            matchesMonth && matchesType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Summary calculation for selected month (or all time)
    val summaryData: StateFlow<SummaryData> = combine(
        allEntries,
        _selectedMonthFilter,
        settings
    ) { entries, month, currentSettings ->
        val filtered = if (month == "ALL") entries else entries.filter { it.date.startsWith(month) }
        var dutyDays = 0
        var totalDutyHours = 0.0
        var totalOtHours = 0.0
        var leaveDays = 0
        var emergencyLeaveDays = 0

        for (entry in filtered) {
            when (entry.type) {
                "duty" -> {
                    dutyDays++
                    totalDutyHours += entry.dutyHours
                    totalOtHours += entry.otHours
                }
                "leave" -> leaveDays++
                "eleave" -> emergencyLeaveDays++
            }
        }

        val dutySalary = totalDutyHours * currentSettings.hourlyRate
        val otSalary = totalOtHours * currentSettings.otHourlyRate
        val totalSalary = dutySalary + otSalary

        SummaryData(
            dutyDays = dutyDays,
            totalDutyHours = kotlin.math.round(totalDutyHours * 100) / 100.0,
            totalOtHours = kotlin.math.round(totalOtHours * 100) / 100.0,
            leaveDays = leaveDays,
            emergencyLeaveDays = emergencyLeaveDays,
            dutySalary = kotlin.math.round(dutySalary * 100) / 100.0,
            otSalary = kotlin.math.round(otSalary * 100) / 100.0,
            totalSalary = kotlin.math.round(totalSalary * 100) / 100.0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SummaryData()
    )

    fun setMonthFilter(month: String) {
        _selectedMonthFilter.value = month
    }

    fun setTypeFilter(type: String) {
        _selectedTypeFilter.value = type
    }

    fun onDateChange(newDate: String) {
        _formState.value = _formState.value.copy(date = newDate)
    }

    fun onTypeChange(newType: String) {
        _formState.value = _formState.value.copy(type = newType)
    }

    fun onInTimeChange(newInTime: String) {
        _formState.value = _formState.value.copy(inTime = newInTime)
    }

    fun onOutTimeChange(newOutTime: String) {
        _formState.value = _formState.value.copy(outTime = newOutTime)
    }

    fun onOtHoursChange(newOt: String) {
        _formState.value = _formState.value.copy(otHours = newOt)
    }

    fun adjustOt(delta: Double) {
        val current = _formState.value.otHoursDouble
        val next = kotlin.math.max(0.0, current + delta)
        _formState.value = _formState.value.copy(otHours = if (next % 1.0 == 0.0) next.toInt().toString() else next.toString())
    }

    fun onNoteChange(newNote: String) {
        _formState.value = _formState.value.copy(note = newNote)
    }

    fun startEditing(entry: AttendanceEntry) {
        _formState.value = FormState(
            editingId = entry.id,
            date = entry.date,
            type = entry.type,
            inTime = entry.inTime ?: "09:00",
            outTime = entry.outTime ?: "18:00",
            otHours = if (entry.otHours % 1.0 == 0.0) entry.otHours.toInt().toString() else entry.otHours.toString(),
            note = entry.note
        )
    }

    fun resetForm() {
        _formState.value = FormState(
            editingId = null,
            date = LocalDate.now().toString(),
            type = "duty",
            inTime = "09:00",
            outTime = "18:00",
            otHours = "0",
            note = ""
        )
    }

    fun saveEntry() {
        val form = _formState.value
        if (form.date.isBlank()) {
            emitMessage("Please select a valid date")
            return
        }

        viewModelScope.launch {
            val dutyHours = if (form.type == "duty") {
                DutyCalculations.calculateDutyHours(form.inTime, form.outTime)
            } else 0.0

            val entry = AttendanceEntry(
                id = form.editingId ?: 0,
                date = form.date,
                type = form.type,
                inTime = if (form.type == "duty") form.inTime else null,
                outTime = if (form.type == "duty") form.outTime else null,
                otHours = if (form.type == "duty") form.otHoursDouble else 0.0,
                dutyHours = dutyHours,
                note = form.note.trim()
            )

            if (form.editingId != null && form.editingId != 0L) {
                repository.updateEntry(entry)
                emitMessage("Entry updated successfully")
            } else {
                repository.insertEntry(entry)
                emitMessage("Entry saved successfully")
            }
            resetForm()
        }
    }

    fun deleteEntry(entry: AttendanceEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            if (_formState.value.editingId == entry.id) {
                resetForm()
            }
            emitMessage("Entry deleted")
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAll()
            resetForm()
            emitMessage("All attendance records deleted")
        }
    }

    fun updateSettings(hourlyRate: Double, otRate: Double, currency: String) {
        settingsRepository.updateSettings(hourlyRate, otRate, currency)
        emitMessage("Salary settings updated")
    }

    fun exportBackupJson(): String {
        val list = allEntries.value
        val currentSettings = settings.value
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val settingsObj = JSONObject()
        settingsObj.put("hourlyRate", currentSettings.hourlyRate)
        settingsObj.put("otHourlyRate", currentSettings.otHourlyRate)
        settingsObj.put("currencySymbol", currentSettings.currencySymbol)
        root.put("settings", settingsObj)

        val entriesArray = JSONArray()
        for (e in list) {
            val item = JSONObject()
            item.put("date", e.date)
            item.put("type", e.type)
            item.put("inTime", e.inTime ?: "")
            item.put("outTime", e.outTime ?: "")
            item.put("otHours", e.otHours)
            item.put("dutyHours", e.dutyHours)
            item.put("note", e.note)
            entriesArray.put(item)
        }
        root.put("entries", entriesArray)
        return root.toString(2)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                val hr = s.optDouble("hourlyRate", 60.0)
                val ot = s.optDouble("otHourlyRate", 90.0)
                val cur = s.optString("currencySymbol", "₹")
                settingsRepository.updateSettings(hr, ot, cur)
            }

            if (root.has("entries")) {
                val array = root.getJSONArray("entries")
                val importedList = mutableListOf<AttendanceEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    importedList.add(
                        AttendanceEntry(
                            date = obj.getString("date"),
                            type = obj.optString("type", "duty"),
                            inTime = obj.optString("inTime").takeIf { it.isNotBlank() },
                            outTime = obj.optString("outTime").takeIf { it.isNotBlank() },
                            otHours = obj.optDouble("otHours", 0.0),
                            dutyHours = obj.optDouble("dutyHours", 0.0),
                            note = obj.optString("note", "")
                        )
                    )
                }
                viewModelScope.launch {
                    repository.insertAll(importedList)
                    emitMessage("Imported ${importedList.size} entries successfully")
                }
            }
            true
        } catch (e: Exception) {
            emitMessage("Import failed: Invalid JSON file")
            false
        }
    }

    fun generateShareSummaryText(): String {
        val s = summaryData.value
        val cur = settings.value.currencySymbol
        val monthLabel = if (_selectedMonthFilter.value == "ALL") "All Time" else _selectedMonthFilter.value

        return """
            📋 DutyLog Attendance & Salary Summary
            Period: $monthLabel
            ───────────────────────
            • Duty Days: ${s.dutyDays}
            • Duty Hours: ${s.totalDutyHours}h
            • Overtime (OT): ${s.totalOtHours}h
            • Leave Days: ${s.leaveDays}
            • Emergency Leaves: ${s.emergencyLeaveDays}
            ───────────────────────
            • Hourly Rate: $cur${settings.value.hourlyRate}/h
            • OT Rate: $cur${settings.value.otHourlyRate}/h
            • Duty Salary: $cur${s.dutySalary}
            • OT Salary: $cur${s.otSalary}
            ★ Total Salary: $cur${s.totalSalary}
        """.trimIndent()
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _messageEvents.emit(msg)
        }
    }
}
