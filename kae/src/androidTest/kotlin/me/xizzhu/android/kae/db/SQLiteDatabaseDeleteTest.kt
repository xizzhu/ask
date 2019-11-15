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
import kotlin.test.*

class SQLiteDatabaseDeleteTest : BaseSQLiteDatabaseTest() {
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
    fun testDeleteWhereLike() {
        populateDatabase()

        assertEquals(0, database.delete(TABLE_NAME) { COLUMN_KEY like "non_exist" })

        assertEquals(1, database.delete(TABLE_NAME) { COLUMN_KEY like "%1" })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }

        assertEquals(2, database.delete(TABLE_NAME) { COLUMN_KEY notLike "non_exist" })
        database.rawQuery("SELECT * from $TABLE_NAME;", null).use {
            assertEquals(0, it.count)
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
