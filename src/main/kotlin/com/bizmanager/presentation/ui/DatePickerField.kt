package com.bizmanager.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DISPLAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val DAY_LABELS = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

/**
 * Sebuah OutlinedButton yang menampilkan tanggal terpilih dan membuka
 * kalender popup saat diklik.
 *
 * @param label      Label di atas field (misal "from" / "to")
 * @param date       Tanggal yang sedang terpilih, null = kosong
 * @param onSelect   Callback saat user memilih tanggal
 * @param width      Lebar tombol
 */
@Composable
fun DatePickerField(
    label: String,
    date: LocalDate?,
    onSelect: (LocalDate?) -> Unit,
    width: Dp = 150.dp
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.width(width).height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = date?.format(DISPLAY_FMT) ?: "Pilih tanggal",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showPicker) {
        CalendarPickerDialog(
            initialDate = date ?: LocalDate.now(),
            selectedDate = date,
            onDismiss = { showPicker = false },
            onSelect = { chosen ->
                onSelect(chosen)
                showPicker = false
            },
            onClear = {
                onSelect(null)
                showPicker = false
            }
        )
    }
}

@Composable
private fun CalendarPickerDialog(
    initialDate: LocalDate,
    selectedDate: LocalDate?,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
    onClear: () -> Unit
) {
    var viewMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    val today = LocalDate.now()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Month/Year navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewMonth = viewMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Bulan sebelumnya")
                }
                Text(
                    text = "${viewMonth.month.getDisplayName(TextStyle.FULL, Locale("id", "ID")).replaceFirstChar { it.uppercase() }} ${viewMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { viewMonth = viewMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Bulan berikutnya")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Day-of-week header
                Row(modifier = Modifier.fillMaxWidth()) {
                    DAY_LABELS.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Build calendar grid
                val firstDayOfMonth = viewMonth.atDay(1)
                // ISO day of week: Mon=1 … Sun=7, we want Mon-first offset
                val startOffset = (firstDayOfMonth.dayOfWeek.value - 1)
                val daysInMonth = viewMonth.lengthOfMonth()
                val totalCells = startOffset + daysInMonth
                val rows = (totalCells + 6) / 7

                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - startOffset + 1
                            if (dayNumber < 1 || dayNumber > daysInMonth) {
                                Box(modifier = Modifier.weight(1f).size(36.dp))
                            } else {
                                val date = viewMonth.atDay(dayNumber)
                                val isSelected = date == selectedDate
                                val isToday = date == today

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                                else -> androidx.compose.ui.graphics.Color.Transparent
                                            }
                                        )
                                        .then(
                                            if (isToday && !isSelected)
                                                Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                            else Modifier
                                        )
                                        .clickable { onSelect(date) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        dismissButton = {
            if (selectedDate != null) {
                TextButton(onClick = onClear) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}
