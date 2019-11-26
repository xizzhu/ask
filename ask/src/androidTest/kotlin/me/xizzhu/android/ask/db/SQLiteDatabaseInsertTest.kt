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

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import me.xizzhu.android.ask.tests.assertListEquals
import kotlin.test.*

class SQLiteDatabaseInsertTest : BaseSQLiteDatabaseTest() {
    @Test
    fun testInsertThenIgnore() {
        assertNotEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        })
        assertNotEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 2L
        })
        assertEquals(-1L, database.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 0L
        })
        assertEquals(-1L, database.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 0L
        })

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
    fun testInsertThenThrow() {
        assertNotEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        })
        assertNotEquals(-1L, database.insert(TABLE_NAME) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 2L
        })

        assertFailsWith(SQLException::class) {
            database.insert(TABLE_NAME) {
                it[COLUMN_KEY] = "key1"
                it[COLUMN_VALUE] = 0L
            }
        }
        assertFailsWith(SQLException::class) {
            database.insert(TABLE_NAME) {
                it[COLUMN_KEY] = "key2"
                it[COLUMN_VALUE] = 0L
            }
        }

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
    fun testInsertThenReplace() {
        assertNotEquals(-1L, database.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 1L
        })
        assertNotEquals(-1L, database.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_NONE) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 2L
        })

        assertNotEquals(-1L, database.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_KEY] = "key1"
            it[COLUMN_VALUE] = 3L
        })
        assertNotEquals(-1L, database.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_KEY] = "key2"
            it[COLUMN_VALUE] = 4L
        })

        selectAll().use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 3L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 4L)
                    ),
                    it.asSequence().toList()
            )
        }
    }
}
