package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceEntry
import com.example.data.DutyCalculations
import com.example.ui.AttendanceViewModel
import com.example.ui.theme.AmberContainerLight
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DutyBlue
import com.example.ui.theme.EmergencyAmber
import com.example.ui.theme.LeaveRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenBg
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SummaryScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val allEntries by viewModel.allEntries.collectAsState()
    val filteredEntries by viewModel.filteredEntries.collectAsState()
    val summary by viewModel.summaryData.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedMonth by viewModel.selectedMonthFilter.collectAsState()
    val context = LocalContext.current

    val currentMonthKey = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) }
    val monthOptions = remember(allEntries) {
        val set = mutableSetOf<String>()
        set.add("ALL")
        set.add(currentMonthKey)
        for (e in allEntries) {
            if (e.date.length >= 7) {
                set.add(e.date.substring(0, 7))
            }
        }
        set.toList().sortedDescending()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = AmberPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Salary & Totals Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Period Selector Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(monthOptions) { monthKey ->
                    val isSelected = selectedMonth == monthKey
                    val label = if (monthKey == "ALL") "All Time" else formatMonthLabel(monthKey)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setMonthFilter(monthKey) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberPrimary,
                            selectedLabelColor = Color(0xFF231705)
                        ),
                        modifier = Modifier.testTag("summary_month_chip_$monthKey")
                    )
                }
            }
        }

        // Highlight Grand Total Earnings Card
        item {
            TotalEarningsBanner(
                totalSalary = summary.totalSalary,
                dutySalary = summary.dutySalary,
                otSalary = summary.otSalary,
                currency = settings.currencySymbol,
                hourlyRate = settings.hourlyRate,
                otRate = settings.otHourlyRate
            )
        }

        // KPI Stat Grid
        item {
            StatGridSection(summary = summary)
        }

        // Salary Breakdown List / Table
        item {
            SalaryBreakdownTable(
                entries = filteredEntries,
                settings = settings
            )
        }

        // Share Summary Report Button
        item {
            Button(
                onClick = {
                    val shareText = viewModel.generateShareSummaryText()
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Attendance Summary")
                    context.startActivity(shareIntent)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("share_summary_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "↗ Share Summary Report",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TotalEarningsBanner(
    totalSalary: Double,
    dutySalary: Double,
    otSalary: Double,
    currency: String,
    hourlyRate: Double,
    otRate: Double
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(AmberPrimary, SuccessGreen)),
                RoundedCornerShape(18.dp)
            )
            .testTag("total_salary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL CALCULATED SALARY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SuccessGreenBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Rate: $currency$hourlyRate/h",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "$currency$totalSalary",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = AmberPrimary
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Duty Pay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currency$dutySalary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Overtime (OT) Pay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currency$otSalary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberDark
                    )
                }
            }
        }
    }
}

@Composable
private fun StatGridSection(summary: com.example.data.SummaryData) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Duty Days",
                value = summary.dutyDays.toString(),
                icon = Icons.Default.Work,
                iconColor = DutyBlue,
                modifier = Modifier.weight(1f),
                testTag = "stat_duty_days"
            )
            StatCard(
                title = "Total Duty Hours",
                value = "${summary.totalDutyHours}h",
                icon = Icons.Default.Schedule,
                iconColor = DutyBlue,
                modifier = Modifier.weight(1f),
                testTag = "stat_duty_hours"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Total OT Hours",
                value = "${summary.totalOtHours}h",
                icon = Icons.Default.MoreTime,
                iconColor = AmberPrimary,
                modifier = Modifier.weight(1f),
                testTag = "stat_ot_hours"
            )
            StatCard(
                title = "Leave Days",
                value = summary.leaveDays.toString(),
                icon = Icons.Default.EventBusy,
                iconColor = LeaveRed,
                modifier = Modifier.weight(1f),
                testTag = "stat_leave_days"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Emergency Leaves",
                value = summary.emergencyLeaveDays.toString(),
                icon = Icons.Default.Emergency,
                iconColor = EmergencyAmber,
                modifier = Modifier.weight(1f),
                testTag = "stat_eleave_days"
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SalaryBreakdownTable(
    entries: List<AttendanceEntry>,
    settings: com.example.data.SalarySettings
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .testTag("summary_table_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "SALARY DETAIL BREAKDOWN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (entries.isEmpty()) {
                Text(
                    text = "No entries in this selected period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Date / Type",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.8f)
                    )
                    Text(
                        text = "Duty",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "OT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = "Earned",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.4f)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Table Items
                entries.forEach { entry ->
                    val earned = DutyCalculations.calculateSalary(entry, settings)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.8f)) {
                            Text(
                                text = DutyCalculations.formatDate(entry.date),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (entry.type) {
                                    "duty" -> "Duty Shift"
                                    "leave" -> "Leave"
                                    else -> "Emergency"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when (entry.type) {
                                    "duty" -> DutyBlue
                                    "leave" -> LeaveRed
                                    else -> EmergencyAmber
                                }
                            )
                        }

                        Text(
                            text = if (entry.type == "duty") "${entry.dutyHours}h" else "–",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = if (entry.type == "duty" && entry.otHours > 0) "${entry.otHours}h" else "–",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = AmberPrimary,
                            modifier = Modifier.weight(0.8f)
                        )

                        Text(
                            text = if (entry.type == "duty") "${settings.currencySymbol}$earned" else "–",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.End,
                            color = if (entry.type == "duty") SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.4f)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        }
    }
}

private fun formatMonthLabel(yyyyMm: String): String {
    return try {
        val parts = yyyyMm.split("-")
        val year = parts[0]
        val month = parts[1].toInt()
        val date = LocalDate.of(year.toInt(), month, 1)
        date.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()))
    } catch (e: Exception) {
        yyyyMm
    }
}
