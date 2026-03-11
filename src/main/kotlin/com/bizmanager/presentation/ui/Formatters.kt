package com.bizmanager.presentation.ui

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val currencyFormatter: NumberFormat = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
    maximumFractionDigits = 2
    minimumFractionDigits = 2
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")

fun BigDecimal.toCurrencyLabel(currency: String = "Rp."): String {
    val normalizedCurrency = when (currency.trim()) {
        "Rp", "Rp." -> "Rp."
        else -> currency.trim()
    }
    val prefix = if (this < BigDecimal.ZERO) "-" else ""
    return "$prefix$normalizedCurrency ${currencyFormatter.format(this.abs())}"
}

fun BigDecimal.toRupiahWordsLabel(): String {
    val roundedValue = setScale(0, RoundingMode.HALF_UP).toBigIntegerExact()
    val prefix = if (roundedValue.signum() < 0) "minus " else ""
    val absoluteValue = roundedValue.abs()
    val words = numberToIndonesianWords(absoluteValue.toLong())
    return "$prefix$words rupiah"
}

fun LocalDateTime.toDateLabel(): String = format(dateFormatter)

fun LocalDateTime.toDateTimeLabel(): String = format(dateTimeFormatter)

private fun numberToIndonesianWords(value: Long): String = when {
    value == 0L -> "nol"
    value < 12L -> basicNumbers[value.toInt()]
    value < 20L -> "${numberToIndonesianWords(value - 10L)} belas"
    value < 100L -> {
        val tens = value / 10L
        val remainder = value % 10L
        buildPhrase("${numberToIndonesianWords(tens)} puluh", remainder)
    }
    value < 200L -> buildPhrase("seratus", value - 100L)
    value < 1_000L -> {
        val hundreds = value / 100L
        val remainder = value % 100L
        buildPhrase("${numberToIndonesianWords(hundreds)} ratus", remainder)
    }
    value < 2_000L -> buildPhrase("seribu", value - 1_000L)
    value < 1_000_000L -> {
        val thousands = value / 1_000L
        val remainder = value % 1_000L
        buildPhrase("${numberToIndonesianWords(thousands)} ribu", remainder)
    }
    value < 1_000_000_000L -> {
        val millions = value / 1_000_000L
        val remainder = value % 1_000_000L
        buildPhrase("${numberToIndonesianWords(millions)} juta", remainder)
    }
    value < 1_000_000_000_000L -> {
        val billions = value / 1_000_000_000L
        val remainder = value % 1_000_000_000L
        buildPhrase("${numberToIndonesianWords(billions)} miliar", remainder)
    }
    else -> {
        val trillions = value / 1_000_000_000_000L
        val remainder = value % 1_000_000_000_000L
        buildPhrase("${numberToIndonesianWords(trillions)} triliun", remainder)
    }
}

private fun buildPhrase(prefix: String, remainder: Long): String =
    if (remainder == 0L) prefix else "$prefix ${numberToIndonesianWords(remainder)}"

private val basicNumbers = listOf(
    "",
    "satu",
    "dua",
    "tiga",
    "empat",
    "lima",
    "enam",
    "tujuh",
    "delapan",
    "sembilan",
    "sepuluh",
    "sebelas"
)
