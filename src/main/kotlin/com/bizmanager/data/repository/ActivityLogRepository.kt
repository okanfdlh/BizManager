package com.bizmanager.data.repository

import com.bizmanager.data.database.ActivityLogs
import com.bizmanager.domain.model.ActivityLog
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ActivityLogRepository {

    fun insert(log: ActivityLog): ActivityLog = transaction {
        val insertStatement = ActivityLogs.insert {
            it[action] = log.action
            it[entity] = log.entity
            it[referenceId] = log.referenceId
            it[description] = log.description
            it[createdAt] = LocalDateTime.now()
        }
        log.copy(id = insertStatement[ActivityLogs.id].value)
    }

    fun findAll(): List<ActivityLog> = transaction {
        ActivityLogs.selectAll()
            .orderBy(ActivityLogs.createdAt to SortOrder.DESC)
            .map { mapToActivityLog(it) }
    }

    private fun mapToActivityLog(row: ResultRow): ActivityLog = ActivityLog(
        id = row[ActivityLogs.id].value,
        createdAt = row[ActivityLogs.createdAt],
        action = row[ActivityLogs.action],
        entity = row[ActivityLogs.entity],
        referenceId = row[ActivityLogs.referenceId],
        description = row[ActivityLogs.description]
    )
}
