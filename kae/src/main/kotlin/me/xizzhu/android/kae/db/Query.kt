/*
 * Copyright (C) 2019 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xizzhu.android.kae.db

import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException

enum class SortOrder(internal val text: String) {
    ASCENDING("ASC"),
    DESCENDING("DESC")
}

class Query(private val db: SQLiteDatabase, private val table: String,
            private val columns: Array<out String>, private val where: String = "") {
    private var distinct: Boolean = false
    private var groupBy: Array<out String>? = null
    private var having: Condition? = null
    private var orderBy: Array<out String>? = null
    private var sortOrder: SortOrder = SortOrder.ASCENDING
    private var limit: Long = -1L
    private var offset: Long = -1L

    fun distinct(distinct: Boolean = true): Query = apply { this.distinct = distinct }

    fun groupBy(vararg columns: String) = apply { groupBy = columns }

    fun having(condition: ConditionBuilder.() -> Condition) = apply { having = condition(ConditionBuilder) }

    fun orderBy(vararg columns: String, sortOrder: SortOrder = SortOrder.ASCENDING) = apply {
        orderBy = columns
        this.sortOrder = sortOrder
    }

    fun limit(limit: Long): Query = apply { this.limit = limit }

    fun offset(offset: Long): Query = apply { this.offset = offset }

    /**
     * Execute the query and return a [Cursor] positioned before the first entry.
     */
    fun asCursor(): Cursor =
            db.query(distinct, table, columns, where, null,
                    buildGroupByClause(), having?.text,
                    buildOrderByClause(), buildLimitClause())

    private fun buildGroupByClause(): String? = groupBy?.let { columns ->
        StringBuilder().apply {
            columns.forEach { column ->
                if (isNotEmpty()) append(',')
                append(column)
            }
        }.toString()
    }

    private fun buildOrderByClause(): String? = orderBy?.let { columns ->
        StringBuilder().apply {
            columns.forEach { column ->
                if (isNotEmpty()) append(',')
                append(column)
            }
            append(' ').append(sortOrder.text)
        }.toString()
    }

    private fun buildLimitClause(): String? {
        if (limit < 0L && offset <= 0L) return null

        return StringBuilder().apply {
            if (offset > 0) append(offset)
            if (isNotEmpty()) append(',')

            // in case limit < 0, it means we have offset, we need to put the limit there, but Android
            // doesn't like a negative limit, so we put Long.MAX_VALUE in this case
            append(if (limit < 0) Long.MAX_VALUE else limit)
        }.toString()
    }
}

/**
 * Execute the query and perform the given [action] on each row from the query.
 */
inline fun Query.forEach(action: (Map<String, Any?>) -> Unit) = asCursor().forEach(action)

/**
 * Execute the query and perform the given [action] on all the rows from the query.
 */
inline fun Query.forEachIndexed(action: (Int, Map<String, Any?>) -> Unit) = asCursor().forEachIndexed(action)

/**
 * Execute the query and return a [List] containing each row from the query.
 */
fun Query.toList(): List<Map<String, Any?>> = asCursor().toList()

/**
 * Execute the query and return a [List] of items created from each row of the query using [converter].
 */
inline fun <T> Query.toList(converter: (Map<String, Any?>) -> T): List<T> = asCursor().toList(converter)

/**
 * Execute the query and return first row from the query.
 *
 * @throws [SQLiteException]
 */
fun Query.first(): Map<String, Any?> = asCursor().use { cursor ->
    cursor.moveToFirst()
    try {
        cursor.getRow()
    } catch (e: CursorIndexOutOfBoundsException) {
        throw SQLiteException("Failed to get first row", e)
    }
}

/**
 * Execute the query and return an item created from first row of the query using [converter].
 *
 * @throws [SQLiteException]
 */
inline fun <T> Query.first(converter: (Map<String, Any?>) -> T): T = converter(first())

/**
 * Execute the query and return first row from the query.
 */
fun Query.firstOrDefault(defaultValue: Map<String, Any?>): Map<String, Any?> = asCursor().use {
    if (it.moveToFirst()) it.getRow() else defaultValue
}

/**
 * Execute the query and return an item created from first row of the query using [converter].
 */
inline fun <T> Query.firstOrDefault(defaultValue: T, converter: (Map<String, Any?>) -> T): T = asCursor().use {
    if (it.moveToFirst()) converter(it.getRow()) else defaultValue
}

/**
 * Execute the query and return first row from the query.
 */
inline fun Query.firstOrDefault(defaultValue: () -> Map<String, Any?>): Map<String, Any?> = asCursor().use {
    if (it.moveToFirst()) it.getRow() else defaultValue()
}

/**
 * Execute the query and return an item created from first row of the query using [converter].
 */
inline fun <T> Query.firstOrDefault(defaultValue: () -> T, converter: (Map<String, Any?>) -> T): T = asCursor().use {
    if (it.moveToFirst()) converter(it.getRow()) else defaultValue()
}
