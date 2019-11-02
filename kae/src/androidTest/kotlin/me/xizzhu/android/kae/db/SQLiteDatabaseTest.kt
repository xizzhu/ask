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
            db.execSQL("CREATE TABLE $TABLE_NAME ($COLUMN_KEY TEXT UNIQUE, $COLUMN_VALUE TEXT)")
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
        database.createTable("tableName") {
            it["column1"] = INTEGER + PRIMARY_KEY
            it["column2"] = TEXT + NOT_NULL
            it["column3"] = INTEGER + UNIQUE(ConflictClause.REPLACE)
        }

        database.insertOrThrow("tableName") {
            it["column1"] = 1
            it["column2"] = "text"
            it["column3"] = 3
        }
        database.rawQuery("SELECT * from tableName;", null).use {
            assertEquals(1, it.count)

            with(it.asSequence().first()) {
                assertEquals(1L, get("column1"))
                assertEquals("text", get("column2"))
                assertEquals(3L, get("column3"))
            }
        }

        assertFailsWith(SQLiteException::class) {
            // duplicated primary key
            database.insertOrThrow("tableName") {
                it["column1"] = 1
                it["column2"] = ""
                it["column3"] = 100
            }
        }
        assertEquals(1, database.rawQuery("SELECT * from tableName;", null).use { it.count })

        // break uniqueness
        database.insertOrThrow("tableName") {
            it["column1"] = 2
            it["column2"] = "updated"
            it["column3"] = 3
        }
        database.rawQuery("SELECT * from tableName;", null).use {
            assertEquals(1, it.count)

            with(it.asSequence().first()) {
                assertEquals(2L, get("column1"))
                assertEquals("updated", get("column2"))
                assertEquals(3L, get("column3"))
            }
        }

        // normal insertion
        database.insertOrThrow("tableName") {
            it["column1"] = 4
            it["column2"] = "4"
            it["column3"] = 4
        }
        database.rawQuery("SELECT * from tableName;", null).use {
            assertEquals(2, it.count)

            with(it.asSequence().toList()) {
                assertEquals(2L, get(0).get("column1"))
                assertEquals("updated", get(0).get("column2"))
                assertEquals(3L, get(0).get("column3"))

                assertEquals(4L, get(1).get("column1"))
                assertEquals("4", get(1).get("column2"))
                assertEquals(4L, get(1).get("column3"))
            }
        }
    }

    @Test
    fun testCreateExistingTable() {
        database.createTable(TABLE_NAME) {
            it[COLUMN_KEY] = TEXT
            it[COLUMN_VALUE] = TEXT
        }

        assertFailsWith(SQLiteException::class) {
            database.createTable(TABLE_NAME, false) {
                it[COLUMN_KEY] = TEXT
                it[COLUMN_VALUE] = TEXT
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
            insert(TABLE_NAME, null, ContentValues()
                    .apply {
                        put(COLUMN_KEY, "key")
                        put(COLUMN_VALUE, "value")
                    })
        }

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToNext())
            assertEquals("key", it.getString(it.getColumnIndex(COLUMN_KEY)))
            assertEquals("value", it.getString(it.getColumnIndex(COLUMN_VALUE)))
        }
    }

    @Test
    fun testAbortTransaction() {
        database.transaction {
            insert(TABLE_NAME, null, ContentValues().apply {
                put(COLUMN_KEY, "key")
                put(COLUMN_VALUE, "value")
            })
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
                    put(COLUMN_VALUE, "value")
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
            it[COLUMN_VALUE] = "value1"
        })
        assertNotEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = "value2"
        })
        assertEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = "value"
        })
        assertEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = "value"
        })

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(2, it.count)

            assertTrue(it.moveToNext())
            assertEquals("key1", it.getString(it.getColumnIndex(COLUMN_KEY)))
            assertEquals("value1", it.getString(it.getColumnIndex(COLUMN_VALUE)))

            assertTrue(it.moveToNext())
            assertEquals("key2", it.getString(it.getColumnIndex(COLUMN_KEY)))
            assertEquals("value2", it.getString(it.getColumnIndex(COLUMN_VALUE)))
        }
    }

    @Test
    fun testInsertOrThrow() {
        assertNotEquals(-1L, database.insertOrThrow(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = "value1"
        })
        assertNotEquals(-1L, database.insertOrThrow(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = "value2"
        })

        assertFailsWith(SQLException::class) {
            database.insertOrThrow(TABLE_NAME) {
                it[COLUMN_KEY] = "key1"
                it[COLUMN_VALUE] = "value"
            }
        }
        assertFailsWith(SQLException::class) {
            database.insertOrThrow(TABLE_NAME) {
                it[COLUMN_KEY] = "key2"
                it[COLUMN_VALUE] = "value"
            }
        }

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(2, it.count)

            assertTrue(it.moveToNext())
            assertEquals("key1", it.getString(it.getColumnIndex(COLUMN_KEY)))
            assertEquals("value1", it.getString(it.getColumnIndex(COLUMN_VALUE)))

            assertTrue(it.moveToNext())
            assertEquals("key2", it.getString(it.getColumnIndex(COLUMN_KEY)))
            assertEquals("value2", it.getString(it.getColumnIndex(COLUMN_VALUE)))
        }
    }

    @Test
    fun testInsertWithOnConflict() {
        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = "value1"
        })
        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = "value2"
        })

        assertFailsWith(SQLException::class) {
            database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
                it[COLUMN_KEY] = "key1"
                it[COLUMN_VALUE] = "value"
            }
        }
        assertFailsWith(SQLException::class) {
            database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
                it[COLUMN_KEY] = "key2"
                it[COLUMN_VALUE] = "value"
            }
        }

        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = "value1_updated"
        })
        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = "value2_updated"
        })

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(2, it.count)

            assertTrue(it.moveToNext())
            assertEquals("key1", it.getString(it.getColumnIndex(COLUMN_KEY)))
            assertEquals("value1_updated", it.getString(it.getColumnIndex(COLUMN_VALUE)))

            assertTrue(it.moveToNext())
            assertEquals("key2", it.getString(it.getColumnIndex(COLUMN_KEY)))
            assertEquals("value2_updated", it.getString(it.getColumnIndex(COLUMN_VALUE)))
        }
    }
}
