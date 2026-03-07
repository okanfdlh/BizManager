package com.bizmanager.data.repository

import com.bizmanager.data.database.Customers
import com.bizmanager.domain.model.Customer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class CustomerRepository {

    fun insert(customer: Customer): Customer = transaction {
        val insertStatement = Customers.insert {
            it[code] = customer.code
            it[name] = customer.name
            it[company] = customer.company
            it[phone] = customer.phone
            it[email] = customer.email
            it[address] = customer.address
            it[notes] = customer.notes
            it[isActive] = customer.isActive
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        customer.copy(id = insertStatement[Customers.id].value)
    }

    fun update(customer: Customer) = transaction {
        Customers.update({ Customers.id eq customer.id }) {
            it[name] = customer.name
            it[company] = customer.company
            it[phone] = customer.phone
            it[email] = customer.email
            it[address] = customer.address
            it[notes] = customer.notes
            it[isActive] = customer.isActive
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun findById(id: Int): Customer? = transaction {
        Customers.select { Customers.id eq id }.map { mapToCustomer(it) }.singleOrNull()
    }

    fun findAll(includeInactive: Boolean = false): List<Customer> = transaction {
        val query = if (includeInactive) {
            Customers.selectAll()
        } else {
            Customers.select { Customers.isActive eq true }
        }
        query.orderBy(Customers.name to SortOrder.ASC).map { mapToCustomer(it) }
    }

    private fun mapToCustomer(row: ResultRow): Customer = Customer(
        id = row[Customers.id].value,
        code = row[Customers.code],
        name = row[Customers.name],
        company = row[Customers.company],
        phone = row[Customers.phone],
        email = row[Customers.email],
        address = row[Customers.address],
        notes = row[Customers.notes],
        isActive = row[Customers.isActive],
        createdAt = row[Customers.createdAt],
        updatedAt = row[Customers.updatedAt]
    )
}
