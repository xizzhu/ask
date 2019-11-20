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

import android.database.sqlite.SQLiteException
import me.xizzhu.android.kae.tests.assertListEquals
import me.xizzhu.android.kae.tests.assertMapEquals
import kotlin.test.*

class SQLiteDatabaseSelectTest : BaseSQLiteDatabaseTest() {
    @Test
    fun testSelectNonExistTable() {
        assertFailsWith(SQLiteException::class) {
            database.select("non_exist").asCursor()
        }
    }

    @Test
    fun testSelectEmptyTable() {
        assertTrue(database.select(TABLE_NAME).toList().isEmpty())
    }

    @Test
    fun testSelectAll() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME).toList()
        )
    }

    @Test
    fun testSelectWithColumns() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_VALUE to 1L),
                        mapOf(COLUMN_VALUE to 2L),
                        mapOf(COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME, COLUMN_VALUE).toList()
        )
    }

    @Test
    fun testDistinct() {
        database.execSQL("CREATE TABLE another_table (column1 TEXT, column2 INTEGER)")
        database.insert("another_table") {
            it["column1"] = "key1"
            it["column2"] = 1L
        }
        database.insert("another_table") {
            it["column1"] = "key1"
            it["column2"] = 1L
        }
        database.insert("another_table") {
            it["column1"] = "key2"
            it["column2"] = 2L
        }
        database.insert("another_table") {
            it["column1"] = "key2"
            it["column2"] = 3L
        }

        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "column2" to 1L),
                        mapOf("column1" to "key1", "column2" to 1L),
                        mapOf("column1" to "key2", "column2" to 2L),
                        mapOf("column1" to "key2", "column2" to 3L)
                ),
                database.select("another_table").toList()
        )

        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "column2" to 1L),
                        mapOf("column1" to "key2", "column2" to 2L),
                        mapOf("column1" to "key2", "column2" to 3L)
                ),
                database.select("another_table").distinct().toList()
        )
    }

    @Test
    fun testGroupBy() {
        database.execSQL("CREATE TABLE another_table (column1 TEXT, column2 REAL)")
        database.insert("another_table") {
            it["column1"] = "key1"
            it["column2"] = 1.0
        }
        database.insert("another_table") {
            it["column1"] = "key1"
            it["column2"] = 2.0
        }
        database.insert("another_table") {
            it["column1"] = "key2"
            it["column2"] = 3.0
        }
        database.insert("another_table") {
            it["column1"] = "key2"
            it["column2"] = 3.0
        }
        database.insert("another_table") {
            it["column1"] = "key2"
            it["column2"] = 6.0
        }

        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "AVG(column2)" to 1.5),
                        mapOf("column1" to "key2", "AVG(column2)" to 4.0)
                ),
                database.select("another_table", "column1", avg("column2")).groupBy("column1").toList()
        )
        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "AVG(DISTINCT column2)" to 1.5),
                        mapOf("column1" to "key2", "AVG(DISTINCT column2)" to 4.5)
                ),
                database.select("another_table", "column1", avg("column2", true)).groupBy("column1").toList()
        )
        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "COUNT(column2)" to 2L),
                        mapOf("column1" to "key2", "COUNT(column2)" to 3L)
                ),
                database.select("another_table", "column1", count("column2")).groupBy("column1").toList()
        )
        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "COUNT(DISTINCT column2)" to 2L),
                        mapOf("column1" to "key2", "COUNT(DISTINCT column2)" to 2L)
                ),
                database.select("another_table", "column1", count("column2", true)).groupBy("column1").toList()
        )
        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "MIN(column2)" to 1.0),
                        mapOf("column1" to "key2", "MIN(column2)" to 3.0)
                ),
                database.select("another_table", "column1", min("column2")).groupBy("column1").toList()
        )
        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "MAX(column2)" to 2.0),
                        mapOf("column1" to "key2", "MAX(column2)" to 6.0)
                ),
                database.select("another_table", "column1", max("column2")).groupBy("column1").toList()
        )
        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "SUM(column2)" to 3.0),
                        mapOf("column1" to "key2", "SUM(column2)" to 12.0)
                ),
                database.select("another_table", "column1", sum("column2")).groupBy("column1").toList()
        )
        assertListEquals(
                listOf(
                        mapOf("column1" to "key1", "SUM(DISTINCT column2)" to 3.0),
                        mapOf("column1" to "key2", "SUM(DISTINCT column2)" to 9.0)
                ),
                database.select("another_table", "column1", sum("column2", true)).groupBy("column1").toList()
        )
    }

    @Test
    fun testOrderBy() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME).orderBy(COLUMN_KEY, sortOrder = SortOrder.DESCENDING).toList()
        )
    }

    @Test
    fun testSelectLimit() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME).limit(1L).toList()
        )

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME).limit(-1L).offset(1L).toList()
        )

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L)
                ),
                database.select(TABLE_NAME).limit(1L).offset(1L).toList()
        )

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME).limit(2L).offset(1L).toList()
        )
    }

    @Test
    fun testForEach() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME).toList()
        )

        var index = 0
        database.select(TABLE_NAME).forEach { row ->
            when (index++) {
                0 -> assertMapEquals(mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L), row)
                1 -> assertMapEquals(mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L), row)
                2 -> assertMapEquals(mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L), row)
                else -> fail()
            }
        }
    }

    @Test
    fun testForEachIndexed() {
        populateDatabase()

        database.select(TABLE_NAME).forEachIndexed { index, row ->
            when (index) {
                0 -> assertMapEquals(mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L), row)
                1 -> assertMapEquals(mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L), row)
                2 -> assertMapEquals(mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L), row)
                else -> fail()
            }
        }
    }
}
