package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SalarySettings(
    val hourlyRate: Double = 60.0,
    val otHourlyRate: Double = 90.0,
    val currencySymbol: String = "₹"
)

data class SummaryData(
    val dutyDays: Int = 0,
    val totalDutyHours: Double = 0.0,
    val totalOtHours: Double = 0.0,
    val leaveDays: Int = 0,
    val emergencyLeaveDays: Int = 0,
    val dutySalary: Double = 0.0,
    val otSalary: Double = 0.0,
    val totalSalary: Double = 0.0
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dutylog_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SalarySettings> = _settings.asStateFlow()

    private fun loadSettings(): SalarySettings {
        val rate = prefs.getString("hourly_rate", "60.0")?.toDoubleOrNull() ?: 60.0
        val otRate = prefs.getString("ot_rate", "90.0")?.toDoubleOrNull() ?: 90.0
        val currency = prefs.getString("currency_symbol", "₹") ?: "₹"
        return SalarySettings(hourlyRate = rate, otHourlyRate = otRate, currencySymbol = currency)
    }

    fun updateSettings(hourlyRate: Double, otHourlyRate: Double, currencySymbol: String = "₹") {
        prefs.edit()
            .putString("hourly_rate", hourlyRate.toString())
            .putString("ot_rate", otHourlyRate.toString())
            .putString("currency_symbol", currencySymbol)
            .apply()
        _settings.value = SalarySettings(hourlyRate, otHourlyRate, currencySymbol)
    }
}

object DutyCalculations {
    fun parseTimeToMinutes(timeStr: String?): Int? {
        if (timeStr.isNullOrBlank()) return null
        val parts = timeStr.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return h * 60 + m
    }

    fun calculateDutyHours(inTime: String?, outTime: String?): Double {
        val start = parseTimeToMinutes(inTime) ?: return 0.0
        val end = parseTimeToMinutes(outTime) ?: return 0.0
        var diff = end - start
        if (diff < 0) {
            // Overnight shift (e.g. 22:00 to 06:00 = 8h)
            diff += 24 * 60
        }
        val hours = diff.toDouble() / 60.0
        return kotlin.math.round(hours * 100) / 100.0
    }

    fun calculateSalary(entry: AttendanceEntry, settings: SalarySettings): Double {
        if (entry.type != "duty") return 0.0
        val dutyPay = entry.dutyHours * settings.hourlyRate
        val otPay = entry.otHours * settings.otHourlyRate
        return kotlin.math.round((dutyPay + otPay) * 100) / 100.0
    }

    fun formatDate(isoDate: String): String {
        return try {
            val localDate = LocalDate.parse(isoDate)
            localDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatTimeDisplay(timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return "--:--"
        return timeStr
    }
}
