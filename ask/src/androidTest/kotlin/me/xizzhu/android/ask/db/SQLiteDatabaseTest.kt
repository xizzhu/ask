/*
 * Copyright (C) 2020 Xizhi Zhu
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

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import me.xizzhu.android.ask.tests.assertListEquals
import kotlin.test.*

class SQLiteDatabaseTest : BaseSQLiteDatabaseTest() {
    @Test
    fun testHasTable() {
        assertTrue(database.hasTable(TABLE_NAME))
        assertFalse(database.hasTable("nonExist"))
    }

    @Test
    fun testCreateTable() {
        val columns = mapOf(
                "column1" to INTEGER + PRIMARY_KEY,
                "column2" to BLOB + NOT_NULL,
                "column3" to INTEGER + UNIQUE(ConflictClause.REPLACE),
                "column4" to REAL,
                "column5" to TEXT + DEFAULT("default_value"),
                "column6" to TEXT + FOREIGN_KEY(TABLE_NAME, COLUMN_KEY, ON_DELETE(ForeignKey.Constraint.Action.SET_NULL), ON_UPDATE(ForeignKey.Constraint.Action.CASCADE))
        )

        assertEquals(
                "CREATE TABLE IF NOT EXISTS tableName (" +
                        "column1 INTEGER, " +
                        "column2 BLOB NOT NULL, " +
                        "column3 INTEGER UNIQUE ON CONFLICT REPLACE, " +
                        "column4 REAL, " +
                        "column5 TEXT DEFAULT default_value, " +
                        "column6 TEXT, " +
                        "PRIMARY KEY(column1), " +
                        "FOREIGN KEY(column6) REFERENCES testTable(testColumnKey) ON DELETE SET NULL ON UPDATE CASCADE" +
                        ");",
                buildSqlForCreatingTable("tableName", true, columns)
        )

        database.createTable("tableName") { it.putAll(columns) }
    }

    @Test
    fun testCreateTableWithConflictClause() {
        val columns = mapOf(
                "column1" to INTEGER + PRIMARY_KEY(ConflictClause.ABORT),
                "column2" to TEXT + NOT_NULL,
                "column3" to INTEGER + UNIQUE(ConflictClause.REPLACE),
                "column4" to REAL,
                "column5" to TEXT + DEFAULT("default_value"),
                "column6" to TEXT + FOREIGN_KEY(TABLE_NAME, COLUMN_KEY)
        )

        assertEquals(
                "CREATE TABLE IF NOT EXISTS tableName (" +
                        "column1 INTEGER, " +
                        "column2 TEXT NOT NULL, " +
                        "column3 INTEGER UNIQUE ON CONFLICT REPLACE, " +
                        "column4 REAL, " +
                        "column5 TEXT DEFAULT default_value, " +
                        "column6 TEXT, " +
                        "PRIMARY KEY(column1) ON CONFLICT ABORT, " +
                        "FOREIGN KEY(column6) REFERENCES testTable(testColumnKey)" +
                        ");",
                buildSqlForCreatingTable("tableName", true, columns)
        )

        database.createTable("tableName") { it.putAll(columns) }
    }

    @Test
    fun testCreateTableWithMultiColumnPrimaryKey() {
        val columns = mapOf(
                "column1" to INTEGER + PRIMARY_KEY(ConflictClause.ROLLBACK),
                "column2" to INTEGER + PRIMARY_KEY,
                "column3" to INTEGER,
                "column4" to TEXT,
                "column5" to TEXT + DEFAULT("default_value"),
                "column6" to TEXT + FOREIGN_KEY(TABLE_NAME, COLUMN_KEY)
        )

        assertEquals(
                "CREATE TABLE IF NOT EXISTS tableName (" +
                        "column1 INTEGER, " +
                        "column2 INTEGER, " +
                        "column3 INTEGER, " +
                        "column4 TEXT, " +
                        "column5 TEXT DEFAULT default_value, " +
                        "column6 TEXT, " +
                        "PRIMARY KEY(column1, column2) ON CONFLICT ROLLBACK, " +
                        "FOREIGN KEY(column6) REFERENCES testTable(testColumnKey)" +
                        ");",
                buildSqlForCreatingTable("tableName", true, columns)
        )

        database.createTable("tableName") { it.putAll(columns) }
    }

    @Test
    fun testCreateExistingTable() {
        database.createTable(TABLE_NAME) {
            it[COLUMN_KEY] = TEXT
            it[COLUMN_VALUE] = INTEGER
        }

        assertFailsWith(SQLiteException::class) {
            database.createTable(TABLE_NAME, false) {
                it[COLUMN_KEY] = TEXT
                it[COLUMN_VALUE] = INTEGER
            }
        }
    }

    @Test
    fun testDropTable() {
        assertTrue(database.hasTable(TABLE_NAME))
        database.dropTable(TABLE_NAME)
        assertFalse(database.hasTable(TABLE_NAME))
    }

    @Test
    fun testDropNonExistTable() {
        database.dropTable("nonExist")
        assertFailsWith(SQLiteException::class) { database.dropTable("nonExist", false) }
    }

    @Test
    fun testCreateIndex() {
        database.createIndex("testIndex1", TABLE_NAME, COLUMN_KEY)
        database.createIndex("testIndex2", TABLE_NAME, COLUMN_KEY, COLUMN_VALUE)
        database.createIndex("testIndex3", TABLE_NAME, COLUMN_KEY, ifNotExists = false)
        database.createIndex("testIndex4", TABLE_NAME, COLUMN_KEY, COLUMN_VALUE, ifNotExists = false)
    }

    @Test
    fun testDropNonExistIndex() {
        database.dropIndex("nonExist")
        assertFailsWith(SQLiteException::class) { database.dropIndex("nonExist", false) }
    }

    @Test
    fun testTransaction() {
        database.transaction {
            insert(TABLE_NAME) {
                it[COLUMN_KEY] = "key"
                it[COLUMN_VALUE] = 0L
            }
        }

        selectAll().use {
            assertListEquals(
                    listOf(mapOf(COLUMN_KEY to "key", COLUMN_VALUE to 0L)),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testAbortTransaction() {
        database.transaction {
            insert(TABLE_NAME) {
                it[COLUMN_KEY] = "key"
                it[COLUMN_VALUE] = 0L
            }
            throw TransactionAbortedException()
        }

        selectAll().use { assertEquals(0, it.count) }
    }

    @Test
    fun testTransactionWithException() {
        assertFailsWith(RuntimeException::class) {
            database.transaction {
                insert(TABLE_NAME, null, ContentValues().apply {
                    put(COLUMN_KEY, "key")
                    put(COLUMN_VALUE, 0L)
                })
                throw RuntimeException()
            }
        }

        selectAll().use { assertEquals(0, it.count) }
    }

    @Test
    fun testWithTransaction() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.withTransaction { selectAll().toList() }
        )
    }

    @Test
    fun testWithTransactionWithException() {
        populateDatabase()

        assertFailsWith(RuntimeException::class) {
            database.withTransaction {
                deleteAll(TABLE_NAME)
                throw RuntimeException()
            }
        }

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                selectAll().toList()
        )
    }
}
