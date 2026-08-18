package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceEntry
import com.example.data.DutyCalculations
import com.example.ui.AttendanceViewModel
import com.example.ui.components.AppDatePickerField
import com.example.ui.components.AppTimePickerField
import com.example.ui.theme.AmberContainerLight
import com.example.ui.theme.AmberOnContainerLight
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DutyBlue
import com.example.ui.theme.DutyBlueBg
import com.example.ui.theme.EmergencyAmber
import com.example.ui.theme.EmergencyAmberBg
import com.example.ui.theme.LeaveRed
import com.example.ui.theme.LeaveRedBg
import com.example.ui.theme.SuccessGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    onDeleteRequested: (AttendanceEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedMonth by viewModel.selectedMonthFilter.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()

    // Generate month filter options from available entries + current month
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
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Brand Header
        item {
            HeaderSection()
        }

        // Form Card (Add / Edit)
        item {
            EntryFormCard(
                formState = formState,
                settingsCurrency = settings.currencySymbol,
                onDateChange = viewModel::onDateChange,
                onTypeChange = viewModel::onTypeChange,
                onInTimeChange = viewModel::onInTimeChange,
                onOutTimeChange = viewModel::onOutTimeChange,
                onOtHoursChange = viewModel::onOtHoursChange,
                onAdjustOt = viewModel::adjustOt,
                onNoteChange = viewModel::onNoteChange,
                onSave = viewModel::saveEntry,
                onCancel = viewModel::resetForm
            )
        }

        // Filter Bar
        item {
            FilterSection(
                monthOptions = monthOptions,
                selectedMonth = selectedMonth,
                selectedType = selectedType,
                onMonthSelected = viewModel::setMonthFilter,
                onTypeSelected = viewModel::setTypeFilter
            )
        }

        // Section Title with Count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance Records",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("entry_count_badge")
                ) {
                    Text(
                        text = "${entries.size} entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Entry Items or Empty State
        if (entries.isEmpty()) {
            item {
                EmptyStateCard(isFiltered = selectedMonth != "ALL" || selectedType != "ALL")
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                AttendanceEntryCard(
                    entry = entry,
                    currency = settings.currencySymbol,
                    hourlyRate = settings.hourlyRate,
                    otRate = settings.otHourlyRate,
                    onEdit = { viewModel.startEditing(entry) },
                    onDelete = { onDeleteRequested(entry) }
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.getDefault()))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Duty",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Log",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AmberPrimary
                )
            }
            Text(
                text = "Attendance & Shift Wage Tracker",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = todayFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EntryFormCard(
    formState: com.example.ui.FormState,
    settingsCurrency: String,
    onDateChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onInTimeChange: (String) -> Unit,
    onOutTimeChange: (String) -> Unit,
    onOtHoursChange: (String) -> Unit,
    onAdjustOt: (Double) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .testTag("entry_form_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Add Entry vs Edit Entry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (formState.editingId != null) "EDIT ENTRY" else "ADD ENTRY",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (formState.editingId != null) AmberPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (formState.editingId != null) {
                    Text(
                        text = "ID: #${formState.editingId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Date Picker Field
            AppDatePickerField(
                label = "Date",
                selectedDate = formState.date,
                onDateSelected = onDateChange,
                modifier = Modifier.fillMaxWidth()
            )

            // Type Segmented Selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Entry Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf("duty" to "Duty", "leave" to "Leave", "eleave" to "Emergency")
                    for ((typeKey, typeLabel) in types) {
                        val isSelected = formState.type == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    if (isSelected) AmberPrimary else Color.Transparent
                                )
                                .clickable { onTypeChange(typeKey) }
                                .padding(vertical = 10.dp)
                                .testTag("type_seg_$typeKey"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF231705) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Duty Specific Fields
            AnimatedVisibility(visible = formState.type == "duty") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // In Time and Out Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppTimePickerField(
                            label = "In Time",
                            timeValue = formState.inTime,
                            onTimeSelected = onInTimeChange,
                            modifier = Modifier.weight(1f),
                            testTag = "in_time_input"
                        )
                        AppTimePickerField(
                            label = "Out Time",
                            timeValue = formState.outTime,
                            onTimeSelected = onOutTimeChange,
                            modifier = Modifier.weight(1f),
                            testTag = "out_time_input"
                        )
                    }

                    // Duty Hours preview bar
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DutyBlueBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DutyBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = DutyBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Calculated Duty Hours:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DutyBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "${formState.dutyHours} hrs",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = DutyBlue
                            )
                        }
                    }

                    // OT Hours Section
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Overtime (OT) Hours (beyond regular duty)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formState.otHours,
                                onValueChange = onOtHoursChange,
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ot_hours_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            // Quick adjust step buttons
                            OutlinedButton(
                                onClick = { onAdjustOt(-0.5) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.testTag("ot_minus_btn")
                            ) {
                                Text("-0.5h", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onAdjustOt(+0.5) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.testTag("ot_plus_btn")
                            ) {
                                Text("+0.5h", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onAdjustOt(+1.0) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.testTag("ot_plus_1h_btn")
                            ) {
                                Text("+1h", fontWeight = FontWeight.Bold, color = Color(0xFF231705))
                            }
                        }
                    }
                }
            }

            // Note Input (Optional)
            OutlinedTextField(
                value = formState.note,
                onValueChange = onNoteChange,
                label = { Text("Note (optional)") },
                placeholder = { Text("e.g. half day, market visit, night shift...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_note_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_entry_button")
                ) {
                    Text(
                        text = if (formState.editingId != null) "Update Entry" else "Save Entry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF231705)
                    )
                }

                if (formState.editingId != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("cancel_edit_button")
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    monthOptions: List<String>,
    selectedMonth: String,
    selectedType: String,
    onMonthSelected: (String) -> Unit,
    onTypeSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Month Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(monthOptions) { monthKey ->
                val isSelected = selectedMonth == monthKey
                val label = if (monthKey == "ALL") "All Months" else formatMonthLabel(monthKey)
                FilterChip(
                    selected = isSelected,
                    onClick = { onMonthSelected(monthKey) },
                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberPrimary,
                        selectedLabelColor = Color(0xFF231705)
                    ),
                    modifier = Modifier.testTag("month_chip_$monthKey")
                )
            }
        }

        // Type Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val typeList = listOf("ALL" to "All Types", "duty" to "Duty", "leave" to "Leave", "eleave" to "Emergency")
            for ((key, label) in typeList) {
                val isSelected = selectedType == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeSelected(key) },
                    label = { Text(label, fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.testTag("type_chip_$key")
                )
            }
        }
    }
}

@Composable
private fun AttendanceEntryCard(
    entry: AttendanceEntry,
    currency: String,
    hourlyRate: Double,
    otRate: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tagBg = when (entry.type) {
        "duty" -> DutyBlueBg
        "leave" -> LeaveRedBg
        else -> EmergencyAmberBg
    }
    val tagColor = when (entry.type) {
        "duty" -> DutyBlue
        "leave" -> LeaveRed
        else -> EmergencyAmber
    }
    val tagLabel = when (entry.type) {
        "duty" -> "DUTY"
        "leave" -> "LEAVE"
        else -> "EMERGENCY"
    }

    val earnedSalary = if (entry.type == "duty") {
        (entry.dutyHours * hourlyRate) + (entry.otHours * otRate)
    } else 0.0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .testTag("entry_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Date & Type Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = DutyCalculations.formatDate(entry.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tagBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, tagColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = tagLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = tagColor,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Shift time details or leave notice
                if (entry.type == "duty") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!entry.inTime.isNullOrBlank() && !entry.outTime.isNullOrBlank()) {
                            Text(
                                text = "${entry.inTime} – ${entry.outTime}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${entry.dutyHours}h duty",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = DutyBlue
                        )
                        if (entry.otHours > 0) {
                            Text(
                                text = "+ ${entry.otHours}h OT",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberPrimary
                            )
                        }
                    }

                    // Compensation preview badge
                    if (hourlyRate > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Earned: $currency${kotlin.math.round(earnedSalary * 100) / 100.0}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                } else {
                    Text(
                        text = if (entry.type == "leave") "Scheduled Leave Day" else "Emergency Leave Recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Note if present
                if (entry.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "“${entry.note}”",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Action Buttons (Edit & Delete)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_entry_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Entry",
                            tint = DutyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LeaveRedBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LeaveRed.copy(alpha = 0.3f))
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_entry_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Entry",
                            tint = LeaveRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(isFiltered: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp)
            .testTag("empty_state_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isFiltered) Icons.Default.FilterList else Icons.Default.Work,
                        contentDescription = null,
                        tint = AmberPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = if (isFiltered) "No entries match this filter" else "No Attendance Records Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isFiltered) "Try selecting 'All Months' or 'All Types' above." else "Use the form above to log your first duty shift or leave.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
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
