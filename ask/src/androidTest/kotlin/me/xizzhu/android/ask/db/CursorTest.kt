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
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.ask.tests.BaseUnitTest
import me.xizzhu.android.ask.tests.assertListEquals
import me.xizzhu.android.ask.tests.assertMapEquals
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
            db.execSQL("CREATE TABLE $TABLE_NAME ($COLUMN_BLOB BLOB, $COLUMN_FLOAT REAL, $COLUMN_INTEGER INTEGER, $COLUMN_STRING TEXT);")
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
    fun testCursorGetters() {
        prepareDatabase()

        database.select(TABLE_NAME).limit(1L).asCursor().use { cursor ->
            cursor.moveToFirst()

            assertEquals(Cursor.FIELD_TYPE_BLOB, cursor.getType(COLUMN_BLOB))
            assertEquals(Cursor.FIELD_TYPE_FLOAT, cursor.getType(COLUMN_FLOAT))
            assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(COLUMN_INTEGER))
            assertEquals(Cursor.FIELD_TYPE_STRING, cursor.getType(COLUMN_STRING))

            assertArrayEquals(byteArrayOf(1, 2, 3), cursor.getBlob(COLUMN_BLOB))
            assertEquals(4.56, cursor.getDouble(COLUMN_FLOAT))
            assertEquals(4.56F, cursor.getFloat(COLUMN_FLOAT))
            assertEquals(789, cursor.getInt(COLUMN_INTEGER))
            assertEquals(789L, cursor.getLong(COLUMN_INTEGER))
            assertEquals(789, cursor.getShort(COLUMN_INTEGER))
            assertEquals("string", cursor.getString(COLUMN_STRING))
        }
    }

    @Test
    fun testMapGetters() {
        prepareDatabase()

        database.select(TABLE_NAME).limit(1L).forEach { row ->
            assertArrayEquals(byteArrayOf(1, 2, 3), row.getBlob(COLUMN_BLOB))
            assertFailsWith(NoSuchElementException::class) { row.getBlob("non_exist") }
            assertFailsWith(NoSuchElementException::class) { row.getBlob(COLUMN_FLOAT) }

            assertEquals(4.56, row.getDouble(COLUMN_FLOAT))
            assertFailsWith(NoSuchElementException::class) { row.getDouble("non_exist") }
            assertFailsWith(NoSuchElementException::class) { row.getDouble(COLUMN_BLOB) }

            assertEquals(4.56F, row.getFloat(COLUMN_FLOAT))
            assertFailsWith(NoSuchElementException::class) { row.getFloat("non_exist") }
            assertFailsWith(NoSuchElementException::class) { row.getFloat(COLUMN_BLOB) }

            assertEquals(789, row.getInt(COLUMN_INTEGER))
            assertFailsWith(NoSuchElementException::class) { row.getInt("non_exist") }
            assertFailsWith(NoSuchElementException::class) { row.getInt(COLUMN_BLOB) }

            assertEquals(789L, row.getLong(COLUMN_INTEGER))
            assertFailsWith(NoSuchElementException::class) { row.getLong("non_exist") }
            assertFailsWith(NoSuchElementException::class) { row.getLong(COLUMN_BLOB) }

            assertEquals(789, row.getShort(COLUMN_INTEGER))
            assertFailsWith(NoSuchElementException::class) { row.getShort("non_exist") }
            assertFailsWith(NoSuchElementException::class) { row.getShort(COLUMN_BLOB) }

            assertEquals("string", row.getString(COLUMN_STRING))
            assertFailsWith(NoSuchElementException::class) { row.getString("non_exist") }
            assertFailsWith(NoSuchElementException::class) { row.getString(COLUMN_BLOB) }
        }
    }

    private fun prepareDatabase() {
        database.insert(TABLE_NAME) {
            it[COLUMN_BLOB] = byteArrayOf(1, 2, 3)
            it[COLUMN_FLOAT] = 4.56
            it[COLUMN_INTEGER] = 789L
            it[COLUMN_STRING] = "string"
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_BLOB] = null
            it[COLUMN_FLOAT] = null
            it[COLUMN_INTEGER] = null
            it[COLUMN_STRING] = null
        }
    }

    @Test
    fun testAsIterable() {
        prepareDatabase()
        queryAll().use {
            assertEquals(2, it.count)
            it.asIterable().forEachIndexed { index, row -> assertEquals(index, row) }
        }
    }

    private fun queryAll() = database.rawQuery("SELECT * from $TABLE_NAME;", null)

    private fun assertEquals(index: Int, row: Map<String, Any?>) {
        when (index) {
            0 -> {
                assertMapEquals(
                        mapOf(
                                Pair(COLUMN_BLOB, byteArrayOf(1, 2, 3)),
                                Pair(COLUMN_FLOAT, 4.56),
                                Pair(COLUMN_INTEGER, 789L),
                                Pair(COLUMN_STRING, "string")
                        ),
                        row
                )
            }
            1 -> {
                assertMapEquals(
                        mapOf(
                                Pair(COLUMN_BLOB, null),
                                Pair(COLUMN_FLOAT, null),
                                Pair(COLUMN_INTEGER, null),
                                Pair(COLUMN_STRING, null)
                        ),
                        row
                )
            }
            else -> fail()
        }
    }

    @Test
    fun testAsSequence() {
        prepareDatabase()
        queryAll().use {
            assertEquals(2, it.count)
            it.asSequence().forEachIndexed { index, row -> assertEquals(index, row) }
        }
    }

    @Test
    fun testAsFlow() = testDispatcher.runBlockingTest {
        prepareDatabase()
        queryAll().use {
            assertEquals(2, it.count)
            it.asFlow().collectIndexed { index, row -> assertEquals(index, row) }
        }
    }

    @Test
    fun testForEach() {
        prepareDatabase()

        var index = 0
        queryAll().forEach { row -> assertEquals(index++, row) }
    }

    @Test
    fun testForEachIndexed() {
        prepareDatabase()
        queryAll().forEachIndexed { index, row -> assertEquals(index, row) }
    }

    @Test
    fun testToList() {
        prepareDatabase()

        assertListEquals(
                listOf(
                        mapOf(
                                Pair(COLUMN_BLOB, byteArrayOf(1, 2, 3)),
                                Pair(COLUMN_FLOAT, 4.56),
                                Pair(COLUMN_INTEGER, 789L),
                                Pair(COLUMN_STRING, "string")
                        ),
                        mapOf(
                                Pair(COLUMN_BLOB, null),
                                Pair(COLUMN_FLOAT, null),
                                Pair(COLUMN_INTEGER, null),
                                Pair(COLUMN_STRING, null)
                        )
                ),
                queryAll().toList()
        )

        assertListEquals(
                listOf(
                        "string" to 789L,
                        null to null
                ),
                queryAll().toList { (it[COLUMN_STRING] as String?) to (it[COLUMN_INTEGER] as Long?) }
        )
    }

    @Test
    fun testToListIndexed() {
        prepareDatabase()

        assertListEquals(
                listOf(
                        0 to 789L,
                        1 to null
                ),
                queryAll().toListIndexed { index, row -> index to (row[COLUMN_INTEGER] as Long?) }
        )
    }

    @Test
    fun testFirst() {
        assertFailsWith(CursorIndexOutOfBoundsException::class) { queryAll().first() }

        prepareDatabase()

        assertMapEquals(
                mapOf(
                        Pair(COLUMN_BLOB, byteArrayOf(1, 2, 3)),
                        Pair(COLUMN_FLOAT, 4.56),
                        Pair(COLUMN_INTEGER, 789L),
                        Pair(COLUMN_STRING, "string")
                ),
                queryAll().first()
        )
        assertEquals(
                "string" to 789L,
                queryAll().first { (it[COLUMN_STRING] as String) to (it[COLUMN_INTEGER] as Long) }
        )
    }

    @Test
    fun testFirstOrDefault() {
        assertTrue(queryAll().firstOrDefault(emptyMap()).isEmpty())
        assertTrue(queryAll().firstOrDefault { emptyMap() }.isEmpty())

        prepareDatabase()

        assertMapEquals(
                mapOf(
                        Pair(COLUMN_BLOB, byteArrayOf(1, 2, 3)),
                        Pair(COLUMN_FLOAT, 4.56),
                        Pair(COLUMN_INTEGER, 789L),
                        Pair(COLUMN_STRING, "string")
                ),
                queryAll().firstOrDefault(emptyMap())
        )
        assertEquals(
                "string" to 789L,
                queryAll().firstOrDefault("" to 0L) { (it[COLUMN_STRING] as String) to (it[COLUMN_INTEGER] as Long) }
        )

        assertMapEquals(
                mapOf(
                        Pair(COLUMN_BLOB, byteArrayOf(1, 2, 3)),
                        Pair(COLUMN_FLOAT, 4.56),
                        Pair(COLUMN_INTEGER, 789L),
                        Pair(COLUMN_STRING, "string")
                ),
                queryAll().firstOrDefault { emptyMap() }
        )
        assertEquals(
                "string" to 789L,
                queryAll().firstOrDefault({ "" to 0L }) { (it[COLUMN_STRING] as String) to (it[COLUMN_INTEGER] as Long) }
        )
    }
}
