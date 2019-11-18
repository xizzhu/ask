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

import android.database.sqlite.SQLiteDatabase
import me.xizzhu.android.kae.content.toContentValues
import me.xizzhu.android.kae.utils.forEachIndexed

class TransactionAbortedException : RuntimeException()

/**
 * @return True if the [table] exists, or false otherwise.
 */
fun SQLiteDatabase.hasTable(table: String): Boolean =
        rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '$table';", null).use {
            it.count > 0
        }

/**
 * Create a [table] with columns described in [block].
 *
 * @param ifNotExists If true, suppress the error in case the [table] already exists.
 */
inline fun SQLiteDatabase.createTable(table: String, ifNotExists: Boolean = true, block: (MutableMap<String, ColumnModifiers>) -> Unit) {
    execSQL(buildSqlForCreatingTable(table, ifNotExists, hashMapOf<String, ColumnModifiers>().apply(block)))
}

/**
 * @internal
 */
fun buildSqlForCreatingTable(table: String, ifNotExists: Boolean, columnDefinitions: Map<String, ColumnModifiers>): String {
    val sqlBuilder = StringBuilder("CREATE TABLE")
    if (ifNotExists) sqlBuilder.append(" IF NOT EXISTS")
    sqlBuilder.append(' ').append(table)

    sqlBuilder.append(" (")

    val primaryKeys = arrayListOf<String>()
    var primaryKeyConflictClause: ConflictClause? = null
    val foreignKeys = hashMapOf<String, ForeignKey>()
    columnDefinitions.forEachIndexed { index, (columnName, modifiers) ->
        if (index > 0) sqlBuilder.append(", ")
        sqlBuilder.append(columnName)

        modifiers.modifiers.forEach { modifier ->
            when (modifier) {
                is PrimaryKey -> {
                    primaryKeys.add(columnName)
                    if (primaryKeyConflictClause == null) primaryKeyConflictClause = modifier.conflictClause
                }
                is ForeignKey -> {
                    foreignKeys[columnName] = modifier
                }
                else -> {
                    sqlBuilder.append(' ').append(modifier.text)
                }
            }
        }
    }
    if (primaryKeys.isNotEmpty()) {
        sqlBuilder.append(", PRIMARY KEY(")
        primaryKeys.forEachIndexed { index, primaryKey ->
            if (index > 0) sqlBuilder.append(", ")
            sqlBuilder.append(primaryKey)
        }
        sqlBuilder.append(')')
        primaryKeyConflictClause?.let { sqlBuilder.append(' ').append(it.text) }
    }
    foreignKeys.forEach { (columnName, foreignKey) ->
        sqlBuilder.append(", FOREIGN KEY(")
                .append(columnName)
                .append(") REFERENCES ")
                .append(foreignKey.referenceTable)
                .append('(')
                .append(foreignKey.referenceColumn)
                .append(')')

        foreignKey.constraints.forEach { constraint ->
            sqlBuilder.append(' ').append(constraint.text)
        }
    }

    sqlBuilder.append(");")

    return sqlBuilder.toString()
}

/**
 * Remove the [table].
 *
 * @param ifExists If true, suppress the error in case the [table] does not exist.
 */
fun SQLiteDatabase.dropTable(table: String, ifExists: Boolean = true) {
    execSQL("DROP TABLE ${if (ifExists) "IF EXISTS" else ""} $table;")
}

/**
 * Run [block] in a transaction. To abort the transaction, simply throw a [TransactionAbortedException].
 *
 * Due to limitation that transactions are thread confined, SQLite accessing code inside the [block]
 * can only run in the current thread.
 *
 * @param exclusive Run the transaction in EXCLUSIVE mode if true, or IMMEDIATE mode otherwise.
 */
inline fun SQLiteDatabase.transaction(exclusive: Boolean = true, block: SQLiteDatabase.() -> Unit) {
    if (exclusive) beginTransaction() else beginTransactionNonExclusive()

    try {
        block()
        setTransactionSuccessful()
    } catch (e: TransactionAbortedException) {
        // do nothing, just abort the transaction
    } finally {
        endTransaction()
    }
}

/**
 * Insert values provided through [block] as a row into the [table].
 *
 * @return Row ID of the newly inserted row, or -1 upon failure.
 */
inline fun SQLiteDatabase.insert(table: String, nullColumnHack: String? = null, block: (MutableMap<String, Any?>) -> Unit): Long =
        insert(table, nullColumnHack, hashMapOf<String, Any?>().apply(block).toContentValues())

/**
 * Insert values provided through [block] as a row into the [table].
 *
 * @throws SQLException
 * @return Row ID of the newly inserted row, or -1 upon failure.
 */
inline fun SQLiteDatabase.insertOrThrow(table: String, nullColumnHack: String? = null, block: (MutableMap<String, Any?>) -> Unit): Long =
        insertOrThrow(table, nullColumnHack, hashMapOf<String, Any?>().apply(block).toContentValues())

/**
 * Insert values provided through [block] as a row into the [table], using [conflictAlgorithm] to resolve conflicts.
 *
 * @throws SQLException
 * @return Row ID of the newly inserted row, or -1 upon failure.
 */
inline fun SQLiteDatabase.insertWithOnConflict(table: String, conflictAlgorithm: Int, nullColumnHack: String? = null, block: (MutableMap<String, Any?>) -> Unit): Long =
        insertWithOnConflict(table, nullColumnHack, hashMapOf<String, Any?>().apply(block).toContentValues(), conflictAlgorithm)

/**
 * Update [values] matching conditions by [where] into the [table].
 *
 * @throws SQLException
 * @return The number of rows affected.
 */
inline fun SQLiteDatabase.update(table: String, values: (MutableMap<String, Any?>) -> Unit,
                                 conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE,
                                 where: WhereBuilder.() -> Where): Int =
        updateWithOnConflict(table, hashMapOf<String, Any?>().apply(values).toContentValues(),
                buildSqlForWhere(where(WhereBuilder)), null, conflictAlgorithm)

/**
 * @internal
 */
fun buildSqlForWhere(where: Where): String = where.text

/**
 * Delete all values from [table].
 *
 * @throws SQLException
 */
fun SQLiteDatabase.deleteAll(table: String) {
    delete(table, null, null)
}

/**
 * Delete values from [table] matching conditions by [where].
 *
 * @throws SQLException
 * @return The number of rows deleted.
 */
inline fun SQLiteDatabase.delete(table: String, where: WhereBuilder.() -> Where): Int = delete(table, buildSqlForWhere(where(WhereBuilder)), null)
