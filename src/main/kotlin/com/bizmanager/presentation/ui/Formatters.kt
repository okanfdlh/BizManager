package com.bizmanager.presentation.ui

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val currencyFormatter: NumberFormat = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
    maximumFractionDigits = 2
    minimumFractionDigits = 0
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")

fun BigDecimal.toCurrencyLabel(currency: String = "Rp"): String = "$currency ${currencyFormatter.format(this)}"

fun LocalDateTime.toDateLabel(): String = format(dateFormatter)

fun LocalDateTime.toDateTimeLabel(): String = format(dateTimeFormatter)
