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
            db.execSQL("CREATE TABLE $TABLE_NAME($COLUMN_KEY TEXT UNIQUE, $COLUMN_VALUE TEXT)")
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
        assertNotEquals(-1L, database.insert(TABLE_NAME, COLUMN_KEY to "key1", COLUMN_VALUE to "value1"))
        assertNotEquals(-1L, database.insert(TABLE_NAME, COLUMN_KEY to "key2", COLUMN_VALUE to "value2"))
        assertEquals(-1L, database.insert(TABLE_NAME, COLUMN_KEY to "key1", COLUMN_VALUE to "value"))
        assertEquals(-1L, database.insert(TABLE_NAME, COLUMN_KEY to "key2", COLUMN_VALUE to "value"))

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
        assertNotEquals(-1L, database.insertOrThrow(TABLE_NAME, COLUMN_KEY to "key1", COLUMN_VALUE to "value1"))
        assertNotEquals(-1L, database.insertOrThrow(TABLE_NAME, COLUMN_KEY to "key2", COLUMN_VALUE to "value2"))

        assertFailsWith(SQLException::class) { database.insertOrThrow(TABLE_NAME, COLUMN_KEY to "key1", COLUMN_VALUE to "value") }
        assertFailsWith(SQLException::class) { database.insertOrThrow(TABLE_NAME, COLUMN_KEY to "key2", COLUMN_VALUE to "value") }

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
        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE, COLUMN_KEY to "key1", COLUMN_VALUE to "value1"))
        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE, COLUMN_KEY to "key2", COLUMN_VALUE to "value2"))

        assertFailsWith(SQLException::class) { database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE, COLUMN_KEY to "key1", COLUMN_VALUE to "value") }
        assertFailsWith(SQLException::class) { database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE, COLUMN_KEY to "key2", COLUMN_VALUE to "value") }

        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE, COLUMN_KEY to "key1", COLUMN_VALUE to "value1_updated"))
        assertNotEquals(-1L, database.insertWithOnConflict(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE, COLUMN_KEY to "key2", COLUMN_VALUE to "value2_updated"))

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
