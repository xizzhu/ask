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
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.kae.tests.BaseUnitTest
import kotlin.test.*

class SQLiteDatabaseTest : BaseUnitTest() {
    private class DatabaseOpenHelper : SQLiteOpenHelper(ApplicationProvider.getApplicationContext(), "db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE testTable(testColumn TEXT)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // do nothing
        }
    }

    private lateinit var database: SQLiteDatabase

    @BeforeTest
    override fun setup() {
        super.setup()
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase("db")
        database = DatabaseOpenHelper().writableDatabase
    }

    @AfterTest
    override fun tearDown() {
        database.close()
        super.tearDown()
    }

    @Test
    fun testTransaction() {
        database.transaction {
            insert("testTable", null, ContentValues().apply { put("testColumn", "value") })
        }

        database.rawQuery("SELECT testColumn from testTable;", null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToNext())
            assertEquals("value", it.getString(it.getColumnIndex("testColumn")))
        }
    }

    @Test
    fun testAbortTransaction() {
        database.transaction {
            insert("testTable", null, ContentValues().apply { put("testColumn", "value") })
            throw TransactionAbortedException()
        }

        database.rawQuery("SELECT testColumn from testTable;", null).use {
            assertEquals(0, it.count)
        }
    }
}
