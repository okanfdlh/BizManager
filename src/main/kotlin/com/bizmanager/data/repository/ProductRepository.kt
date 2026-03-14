package com.bizmanager.data.repository

import com.bizmanager.data.database.Products
import com.bizmanager.domain.model.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ProductRepository {

    fun insert(product: Product): Product = transaction {
        val insertStatement = Products.insert {
            it[code] = product.code
            it[name] = product.name
            it[category] = product.category
            it[unit] = product.unit
            it[costPrice] = product.costPrice
            it[sellPrice] = product.sellPrice
            it[isActive] = product.isActive
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        product.copy(id = insertStatement[Products.id].value)
    }

    fun update(product: Product) = transaction {
        Products.update({ Products.id eq product.id }) {
            it[name] = product.name
            it[category] = product.category
            it[unit] = product.unit
            it[costPrice] = product.costPrice
            it[sellPrice] = product.sellPrice
            it[isActive] = product.isActive
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun findById(id: Int): Product? = transaction {
        Products.select { Products.id eq id }.map { mapToProduct(it) }.singleOrNull()
    }

    fun findAll(includeInactive: Boolean = false): List<Product> = transaction {
        val query = if (includeInactive) {
            Products.selectAll()
        } else {
            Products.select { Products.isActive eq true }
        }
        query.orderBy(Products.name to SortOrder.ASC).map { mapToProduct(it) }
    }

    private fun mapToProduct(row: ResultRow): Product = Product(
        id = row[Products.id].value,
        code = row[Products.code],
        name = row[Products.name],
        category = row[Products.category],
        unit = row[Products.unit],
        costPrice = row[Products.costPrice],
        sellPrice = row[Products.sellPrice],
        isActive = row[Products.isActive],
        createdAt = row[Products.createdAt],
        updatedAt = row[Products.updatedAt]
    )

    fun delete(id: Int) = transaction {
        Products.deleteWhere { Products.id eq id }
    }
}
