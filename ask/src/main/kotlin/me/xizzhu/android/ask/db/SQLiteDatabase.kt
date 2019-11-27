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

package me.xizzhu.android.ask.db

import android.database.sqlite.SQLiteDatabase
import me.xizzhu.android.ask.content.toContentValues

class TransactionAbortedException : RuntimeException()

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
 * Run [block] in a transaction and return the result.
 *
 * Due to limitation that transactions are thread confined, SQLite accessing code inside the [block]
 * can only run in the current thread.
 *
 * @param exclusive Run the transaction in EXCLUSIVE mode if true, or IMMEDIATE mode otherwise.
 */
inline fun <T> SQLiteDatabase.withTransaction(exclusive: Boolean = true, block: SQLiteDatabase.() -> T): T {
    if (exclusive) beginTransaction() else beginTransactionNonExclusive()

    try {
        val result = block()
        setTransactionSuccessful()
        return result
    } finally {
        endTransaction()
    }
}

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
 * @throws SQLException
 */
inline fun SQLiteDatabase.createTable(table: String, ifNotExists: Boolean = true, block: (MutableMap<String, ColumnModifiers>) -> Unit) {
    execSQL(buildSqlForCreatingTable(table, ifNotExists, hashMapOf<String, ColumnModifiers>().apply(block)))
}

/**
 * Remove the [table].
 *
 * @param ifExists If true, suppress the error in case the [table] does not exist.
 * @throws SQLException
 */
fun SQLiteDatabase.dropTable(table: String, ifExists: Boolean = true) {
    execSQL("DROP TABLE ${if (ifExists) "IF EXISTS" else ""} $table;")
}

/**
 * Create an [index] with [columns] of the [table].
 *
 * @param ifNotExists If true, suppress the error in case the [index] already exists.
 * @throws SQLException
 */
fun SQLiteDatabase.createIndex(index: String, table: String, vararg columns: String, ifNotExists: Boolean = true) {
    execSQL(buildSqlForCreatingIndex(index, table, columns, ifNotExists))
}

/**
 * Remove the [index].
 *
 * @param ifExists If true, suppress the error in case the [index] does not exist.
 * @throws SQLException
 */
fun SQLiteDatabase.dropIndex(index: String, ifExists: Boolean = true) {
    execSQL("DROP INDEX ${if (ifExists) "IF EXISTS" else ""} $index;")
}

/**
 * Insert values provided through [block] as a row into the [table], using [conflictAlgorithm] to resolve conflicts.
 *
 * @throws SQLException
 * @return Row ID of the newly inserted row, or -1 upon failure.
 */
inline fun SQLiteDatabase.insert(table: String, conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE,
                                 nullColumnHack: String? = null, block: (MutableMap<String, Any?>) -> Unit): Long =
        insertWithOnConflict(table, nullColumnHack, hashMapOf<String, Any?>().apply(block).toContentValues(), conflictAlgorithm)

/**
 * Create a [Query] to get values from [columns] of the [table] matching the given [condition]. Note
 * that the query is not executed until [Query.asCursor] or other methods are called.
 *
 * @return A [Query] object.
 */
inline fun SQLiteDatabase.select(table: String, vararg columns: String,
                                 condition: ConditionBuilder.() -> Condition = { noOp() }): Query =
        Query(this, table, columns, buildSqlForWhere(condition(ConditionBuilder)))

/**
 * Update [values] matching the given [condition] into the [table].
 *
 * @throws SQLException
 * @return The number of rows affected.
 */
inline fun SQLiteDatabase.update(table: String, values: (MutableMap<String, Any?>) -> Unit,
                                 conflictAlgorithm: Int = SQLiteDatabase.CONFLICT_NONE,
                                 condition: ConditionBuilder.() -> Condition): Int =
        updateWithOnConflict(table, hashMapOf<String, Any?>().apply(values).toContentValues(),
                buildSqlForWhere(condition(ConditionBuilder)), null, conflictAlgorithm)

/**
 * Delete all values from [table].
 *
 * @throws SQLException
 */
fun SQLiteDatabase.deleteAll(table: String) {
    delete(table, null, null)
}

/**
 * Delete values from [table] matching the given [condition].
 *
 * @throws SQLException
 * @return The number of rows deleted.
 */
inline fun SQLiteDatabase.delete(table: String, condition: ConditionBuilder.() -> Condition): Int =
        delete(table, buildSqlForWhere(condition(ConditionBuilder)), null)
