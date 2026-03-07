package com.bizmanager.domain.service

import com.bizmanager.domain.model.AgingStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object AgingCalculator {

    /**
     * Calculates the AgingBucket dynamically based on the balance due and the due date
     */
    fun calculateAging(dueDate: LocalDateTime, balanceDue: BigDecimal, currentDate: LocalDate = LocalDate.now()): AgingStatus? {
        if (balanceDue <= BigDecimal.ZERO) return null

        val due = dueDate.toLocalDate()
        
        if (due >= currentDate) {
            return AgingStatus.Current
        }

        val daysOverdue = ChronoUnit.DAYS.between(due, currentDate)

        return when {
            daysOverdue in 1..30 -> AgingStatus.Overdue0To30
            daysOverdue in 31..90 -> AgingStatus.Overdue31To90
            daysOverdue > 90 -> AgingStatus.OverdueMoreThan90
            else -> AgingStatus.Current // Fallback, shouldn't occur
        }
    }
}
