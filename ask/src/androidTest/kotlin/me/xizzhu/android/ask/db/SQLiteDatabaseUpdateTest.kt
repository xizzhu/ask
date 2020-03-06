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

import android.database.sqlite.SQLiteException
import me.xizzhu.android.ask.tests.assertListEquals
import kotlin.test.*

class SQLiteDatabaseUpdateTest : BaseSQLiteDatabaseTest() {
    @Test
    fun testUpdateNonExistTable() {
        assertFailsWith(SQLiteException::class) {
            database.update("non_exist", { it["key"] = "value" }) { "key" eq "value" }
        }
    }

    @Test
    fun testUpdateEmptyTable() {
        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_KEY] = "value" }) { COLUMN_KEY.isNull() })
        selectAll().use { assertEquals(0, it.count) }
    }

    @Test
    fun testUpdateWhereIsNull() {
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
        }
        selectAll().use { assertEquals(2, it.count) }

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 2L }) { COLUMN_VALUE.isNull() })
        selectAll().use {
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
    fun testUpdateWhereIsNotNull() {
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        }
        database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
        }
        selectAll().use { assertEquals(2, it.count) }

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE.isNotNull() })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to null)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereEq() {
        populateDatabase()

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY eq "key1" })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereNeq() {
        populateDatabase()

        assertEquals(2, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 4321L }) { COLUMN_KEY neq "key1" })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 4321L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 4321L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereLess() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE less 1L })

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE less 2L })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereLessEq() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE lessEq 0L })

        assertEquals(2, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE lessEq 2L })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereGreater() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE greater 3L })

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE greater 2L })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 1234L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereGreaterEq() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE greaterEq 4L })

        assertEquals(2, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE greaterEq 2L })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 1234L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereBetween() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE.between(Long.MIN_VALUE, 0L) })
        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE.between(4L, Long.MAX_VALUE) })

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_VALUE.between(3L, Long.MAX_VALUE) })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 1234L)
                    ),
                    it.asSequence().toList()
            )
        }

        assertEquals(3, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 4321L }) { COLUMN_VALUE.between(Long.MIN_VALUE, Long.MAX_VALUE) })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 4321L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 4321L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 4321L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereLike() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY like "%non_exist%" })

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY like "%Y1" })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereNotLike() {
        populateDatabase()

        assertEquals(3, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 4321L }) { COLUMN_KEY notLike "%non_exist%" })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 4321L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 4321L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 4321L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereGlob() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY glob "*non_exist*" })
        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY glob "*KEY*" })

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY glob "*y1" })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }

        assertEquals(3, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 4321L }) { COLUMN_KEY glob "*key*" })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 4321L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 4321L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 4321L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereIn() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY inList listOf("non_exist") })

        assertEquals(2, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY inList listOf("key1", "key3", "non_exist") })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 1234L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereNotIn() {
        populateDatabase()

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 4321L }) { COLUMN_KEY notInList listOf("key1", "key2") })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 4321L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereAnd() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY eq "key1" and (COLUMN_VALUE eq 0L) })

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY eq "key1" and (COLUMN_VALUE eq 1L) })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereOr() {
        populateDatabase()

        assertEquals(0, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY eq "key" or (COLUMN_VALUE eq 0L) })

        assertEquals(1, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { COLUMN_KEY eq "key1" or (COLUMN_VALUE eq 0L) })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testUpdateWhereNot() {
        populateDatabase()

        assertEquals(2, database.update(TABLE_NAME, { it[COLUMN_VALUE] = 1234L }) { not(COLUMN_KEY eq "key1") })
        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 1234L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 1234L)
                    ),
                    it.asSequence().toList()
            )
        }
    }
}
