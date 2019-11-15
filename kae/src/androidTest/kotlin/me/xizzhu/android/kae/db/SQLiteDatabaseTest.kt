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

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.kae.tests.BaseUnitTest
import me.xizzhu.android.kae.tests.assertListEquals
import kotlin.test.*

class SQLiteDatabaseTest : BaseUnitTest() {
    companion object {
        private const val DB_NAME = "db"
        private const val TABLE_NAME = "testTable"
        private const val COLUMN_KEY = "testColumnKey"
        private const val COLUMN_VALUE = "testColumnValue"
    }

    private class DatabaseOpenHelper : SQLiteOpenHelper(ApplicationProvider.getApplicationContext(), DB_NAME, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_NAME ($COLUMN_KEY TEXT UNIQUE, $COLUMN_VALUE INTEGER)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // do nothing
        }
    }

    private lateinit var database: SQLiteDatabase

    @BeforeTest
    override fun setup() {
        super.setup()
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(DB_NAME)
        database = DatabaseOpenHelper().writableDatabase
    }

    @AfterTest
    override fun tearDown() {
        database.close()
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(DB_NAME)
        super.tearDown()
    }

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
    fun testTransaction() {
        database.transaction {
            insert(TABLE_NAME) {
                it[COLUMN_KEY] = "key"
                it[COLUMN_VALUE] = 0L
            }
        }

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
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

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(0, it.count)
        }
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

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(0, it.count)
        }
    }

    @Test
    fun testInsert() {
        assertNotEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        })
        assertNotEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 2L
        })
        assertEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 0L
        })
        assertEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 0L
        })

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testInsertOrThrow() {
        assertNotEquals(-1L, database.insertOrThrow(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        })
        assertNotEquals(-1L, database.insertOrThrow(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 2L
        })

        assertFailsWith(SQLException::class) {
            database.insertOrThrow(TABLE_NAME) {
                it[COLUMN_KEY] = "key1"
                it[COLUMN_VALUE] = 0L
            }
        }
        assertFailsWith(SQLException::class) {
            database.insertOrThrow(TABLE_NAME) {
                it[COLUMN_KEY] = "key2"
                it[COLUMN_VALUE] = 0L
            }
        }

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testInsertWithOnConflict() {
        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        })
        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 2L
        })

        assertFailsWith(SQLException::class) {
            database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
                it[COLUMN_KEY] = "key1"
                it[COLUMN_VALUE] = 0L
            }
        }
        assertFailsWith(SQLException::class) {
            database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
                it[COLUMN_KEY] = "key2"
                it[COLUMN_VALUE] = 0L
            }
        }

        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 3L
        })
        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 4L
        })

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 3L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 4L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testDeleteNonExistTable() {
        assertFailsWith(SQLiteException::class) {
            database.deleteAll("non_exist")
        }

        assertFailsWith(SQLiteException::class) {
            database.delete("non_exist") { "key" eq "value" }
        }
    }

    @Test
    fun testDeleteEmptyTable() {
        database.deleteAll(TABLE_NAME)
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(0, it.count)
        }

        assertEquals(0, database.delete(TABLE_NAME) { COLUMN_KEY eq "key1" })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(0, it.count)
        }
    }

    @Test
    fun testDeleteAll() {
        populateDatabase()

        database.deleteAll(TABLE_NAME)
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(0, it.count)
        }
    }

    private fun populateDatabase() {
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 2L
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key3"
            it[COLUMN_VALUE] = 3L
        }
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(3, it.count)
        }
    }

    @Test
    fun testDeleteWhereIsNull() {
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key3"
            it[COLUMN_VALUE] = 3L
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key4"
        }
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(4, it.count)
        }

        assertEquals(0, database.delete(TABLE_NAME) { COLUMN_KEY.isNull() })

        assertEquals(2, database.delete(TABLE_NAME) { COLUMN_VALUE.isNull() })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }

        assertEquals(2, database.delete(TABLE_NAME) { COLUMN_VALUE.isNotNull() })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(0, it.count)
        }
    }

    @Test
    fun testDeleteWhereEq() {
        populateDatabase()

        assertEquals(1, database.delete(TABLE_NAME) { COLUMN_KEY eq "key1" })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testDeleteWhereNeq() {
        populateDatabase()

        assertEquals(2, database.delete(TABLE_NAME) { COLUMN_KEY neq "key1" })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testDeleteWhereLess() {
        populateDatabase()

        assertEquals(0, database.delete(TABLE_NAME) { COLUMN_VALUE less 1L })
        assertEquals(0, database.delete(TABLE_NAME) { COLUMN_VALUE lessEq 0L })

        assertEquals(1, database.delete(TABLE_NAME) { COLUMN_VALUE less 2L })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }

        assertEquals(1, database.delete(TABLE_NAME) { COLUMN_VALUE lessEq 2L })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testDeleteWhereGreater() {
        populateDatabase()

        assertEquals(0, database.delete(TABLE_NAME) { COLUMN_VALUE greater 3L })
        assertEquals(0, database.delete(TABLE_NAME) { COLUMN_VALUE greaterEq 4L })

        assertEquals(1, database.delete(TABLE_NAME) { COLUMN_VALUE greater 2L })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L)
                    ),
                    it.asSequence().toList()
            )
        }

        assertEquals(1, database.delete(TABLE_NAME) { COLUMN_VALUE greaterEq 2L })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testDeleteWhereAnd() {
        populateDatabase()

        assertEquals(0, database.delete(TABLE_NAME) { COLUMN_KEY eq "key1" and (COLUMN_VALUE eq 0L) })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }

        assertEquals(1, database.delete(TABLE_NAME) { COLUMN_KEY eq "key1" and (COLUMN_VALUE eq 1L) })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testDeleteWhereOr() {
        populateDatabase()

        assertEquals(0, database.delete(TABLE_NAME) { COLUMN_KEY eq "key" or (COLUMN_VALUE eq 0L) })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }

        assertEquals(1, database.delete(TABLE_NAME) { COLUMN_KEY eq "key1" or (COLUMN_VALUE eq 0L) })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }
}
