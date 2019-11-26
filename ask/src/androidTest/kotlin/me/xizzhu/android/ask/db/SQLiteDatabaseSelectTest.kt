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

import android.database.sqlite.SQLiteException
import me.xizzhu.android.ask.tests.assertListEquals
import me.xizzhu.android.ask.tests.assertMapEquals
import kotlin.test.*

class SQLiteDatabaseSelectTest : BaseSQLiteDatabaseTest() {
    @Test
    fun testSelectNonExistTable() {
        assertFailsWith(SQLiteException::class) {
            database.select("non_exist").asCursor()
        }
    }

    @Test
    fun testSelectWhereIsNull() {
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
        }

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to null)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE.isNull() }.toList()
        )
    }

    @Test
    fun testSelectWhereIsNotNull() {
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
        }

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE.isNotNull() }.toList()
        )
    }

    @Test
    fun testSelectWhereEq() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE eq 1L }.toList()
        )
    }

    @Test
    fun testSelectWhereNeq() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE neq 1L }.toList()
        )
    }

    @Test
    fun testSelectWhereLess() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_VALUE less 0L }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE less 2L }.toList()
        )
    }

    @Test
    fun testSelectWhereLessEq() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_VALUE lessEq -1L }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE lessEq 2L }.toList()
        )
    }

    @Test
    fun testSelectWhereGreater() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_VALUE greater 3L }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE greater 2L }.toList()
        )
    }

    @Test
    fun testSelectWhereGreaterEq() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_VALUE greaterEq 4L }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE greaterEq 2L }.toList()
        )
    }

    @Test
    fun testSelectWhereBetween() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_VALUE.between(Long.MIN_VALUE, 0L) }.toList().isEmpty())
        assertTrue(database.select(TABLE_NAME) { COLUMN_VALUE.between(4L, Long.MAX_VALUE) }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE.between(1L, 2L) }.toList()
        )
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_VALUE.between(Long.MIN_VALUE, Long.MAX_VALUE) }.toList()
        )
    }

    @Test
    fun testSelectWhereLike() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_KEY like "%non_exist%" }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY like "%Y1" }.toList()
        )
    }

    @Test
    fun testSelectWhereNotLike() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY notLike "%non_exist%" }.toList()
        )
    }

    @Test
    fun testSelectWhereGlob() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_KEY glob "*non_exist*" }.toList().isEmpty())
        assertTrue(database.select(TABLE_NAME) { COLUMN_KEY glob "*KEY*" }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY glob "*y1" }.toList()
        )
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY glob "*key*" }.toList()
        )
    }

    @Test
    fun testSelectWhereIn() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_KEY inList listOf("non_exist") }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY inList listOf("key1", "key3", "non_exist") }.toList()
        )
    }

    @Test
    fun testSelectWhereNotIn() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY notInList listOf("key2", "key3") }.toList()
        )
    }

    @Test
    fun testSelectWhereAnd() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_KEY eq "key1" and (COLUMN_VALUE eq 0L) }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY eq "key1" and (COLUMN_VALUE eq 1L) }.toList()
        )
    }

    @Test
    fun testSelectWhereOr() {
        populateDatabase()

        assertTrue(database.select(TABLE_NAME) { COLUMN_KEY eq "key" or (COLUMN_VALUE eq 0L) }.toList().isEmpty())
        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY eq "key1" or (COLUMN_VALUE eq 0L) }.toList()
        )
    }

    @Test
    fun testSelectWhereNot() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { not(COLUMN_KEY eq "key1") }.toList()
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
    fun testHaving() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key3", "COUNT($COLUMN_VALUE)" to 1L)
                ),
                database.select(TABLE_NAME, COLUMN_KEY, count(COLUMN_VALUE))
                        .groupBy(COLUMN_KEY)
                        .having { max(COLUMN_VALUE) greater 2L }
                        .toList()
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
        database.select(TABLE_NAME).forEach { fail() }

        populateDatabase()

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
        database.select(TABLE_NAME).forEachIndexed { _, _ -> fail() }

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

    @Test
    fun testToList() {
        assertTrue(database.select(TABLE_NAME).toList().isEmpty())
        assertTrue(database.select(TABLE_NAME).toList { fail() }.isEmpty())

        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME).toList()
        )
        assertListEquals(
                listOf(
                        "key1" to 1L,
                        "key2" to 2L,
                        "key3" to 3L
                ),
                database.select(TABLE_NAME).toList { (it[COLUMN_KEY] as String) to (it[COLUMN_VALUE] as Long) }
        )
    }

    @Test
    fun testFirst() {
        assertFailsWith(SQLiteException::class) { database.select(TABLE_NAME).first() }

        populateDatabase()

        assertMapEquals(mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L), database.select(TABLE_NAME).first())
        assertEquals(
                "key1" to 1L,
                database.select(TABLE_NAME).first { (it[COLUMN_KEY] as String) to (it[COLUMN_VALUE] as Long) }
        )
    }

    @Test
    fun testFirstOrDefault() {
        assertTrue(database.select(TABLE_NAME).firstOrDefault(emptyMap()).isEmpty())
        assertTrue(database.select(TABLE_NAME).firstOrDefault { emptyMap() }.isEmpty())

        populateDatabase()

        assertMapEquals(
                mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                database.select(TABLE_NAME).firstOrDefault(emptyMap())
        )
        assertEquals(
                "key1" to 1L,
                database.select(TABLE_NAME).firstOrDefault("" to 0L) { (it[COLUMN_KEY] as String) to (it[COLUMN_VALUE] as Long) }
        )

        assertMapEquals(
                mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                database.select(TABLE_NAME).firstOrDefault { emptyMap() }
        )
        assertEquals(
                "key1" to 1L,
                database.select(TABLE_NAME).firstOrDefault({ "" to 0L }) { (it[COLUMN_KEY] as String) to (it[COLUMN_VALUE] as Long) }
        )
    }
}
