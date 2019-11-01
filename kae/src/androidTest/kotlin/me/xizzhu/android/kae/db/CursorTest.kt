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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.kae.tests.BaseUnitTest
import org.junit.Assert.assertArrayEquals
import kotlin.test.*

class CursorTest : BaseUnitTest() {
    companion object {
        private const val DB_NAME = "db"
        private const val TABLE_NAME = "testTable"
        private const val COLUMN_BLOB = "blobColumn"
        private const val COLUMN_FLOAT = "floatColumn"
        private const val COLUMN_INTEGER = "integerColumn"
        private const val COLUMN_STRING = "stringColumn"
    }

    private class DatabaseOpenHelper : SQLiteOpenHelper(ApplicationProvider.getApplicationContext(), DB_NAME, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            try {
                db.execSQL("CREATE TABLE $TABLE_NAME ($COLUMN_BLOB BLOB, $COLUMN_FLOAT REAL, $COLUMN_INTEGER INTEGER, $COLUMN_STRING TEXT);")
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
    fun testAsSequence() {
        database.insert(TABLE_NAME,
                COLUMN_BLOB to byteArrayOf(1, 2, 3),
                COLUMN_FLOAT to 4.56,
                COLUMN_INTEGER to 789L,
                COLUMN_STRING to "string")
        database.insert(TABLE_NAME,
                COLUMN_BLOB to null,
                COLUMN_FLOAT to null,
                COLUMN_INTEGER to null,
                COLUMN_STRING to null)

        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(2, it.count)

            it.asSequence().forEachIndexed { index, row ->
                when (index) {
                    0 -> {
                        assertEquals(4, row.count())
                        assertArrayEquals(byteArrayOf(1, 2, 3), row[COLUMN_BLOB] as ByteArray)
                        assertEquals(4.56, row[COLUMN_FLOAT])
                        assertEquals(789L, row[COLUMN_INTEGER])
                        assertEquals("string", row[COLUMN_STRING])
                    }
                    1 -> {
                        assertEquals(4, row.count())
                        assertNull(row[COLUMN_BLOB])
                        assertNull(row[COLUMN_FLOAT])
                        assertNull(row[COLUMN_INTEGER])
                        assertNull(row[COLUMN_STRING])
                    }
                    else -> fail()
                }
            }
        }
    }
}
