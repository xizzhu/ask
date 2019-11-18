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

class SQLiteDatabaseSelectTest : BaseSQLiteDatabaseTest() {
    @Test
    fun testSelectNonExistTable() {
        assertFailsWith(SQLiteException::class) {
            database.select("non_exist") { "key" eq "value" }
        }
    }

    @Test
    fun testSelectEmptyTable() {
        database.select(TABLE_NAME) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.use { assertEquals(0, it.count) }
    }

    @Test
    fun testSelectAll() {
        populateDatabase()

        database.select(TABLE_NAME) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }
    }

    @Test
    fun testSelectLimit() {
        populateDatabase()

        database.select(TABLE_NAME, limit = 1) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                    ),
                    it.asSequence().toList()
            )
        }

        database.select(TABLE_NAME, limit = -1, offset = 1) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                            mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                    ),
                    it.asSequence().toList()
            )
        }

        database.select(TABLE_NAME, limit = 1, offset = 1) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.use {
            assertListEquals(
                    listOf(
                            mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L)
                    ),
                    it.asSequence().toList()
            )
        }

        database.select(TABLE_NAME, limit = 2, offset = 1) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.use {
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
