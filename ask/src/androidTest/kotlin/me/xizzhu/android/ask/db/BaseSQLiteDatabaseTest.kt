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

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.ask.tests.BaseUnitTest
import kotlin.test.*

open class BaseSQLiteDatabaseTest : BaseUnitTest() {
    companion object {
        const val DB_NAME = "db"
        const val TABLE_NAME = "testTable"
        const val COLUMN_KEY = "testColumnKey"
        const val COLUMN_VALUE = "testColumnValue"
    }

    private class DatabaseOpenHelper : SQLiteOpenHelper(ApplicationProvider.getApplicationContext(), DB_NAME, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_NAME ($COLUMN_KEY TEXT UNIQUE, $COLUMN_VALUE INTEGER)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // do nothing
        }
    }

    lateinit var database: SQLiteDatabase

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

    fun populateDatabase() {
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

    fun selectAll(): Cursor = database.rawQuery("SELECT * from $TABLE_NAME;", null)
}
