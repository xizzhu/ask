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
            database.select("non_exist") { "key" eq "value" }.asCursor()
        }
    }

    @Test
    fun testSelectEmptyTable() {
        assertTrue(database.select(TABLE_NAME) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.toList().isEmpty())
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
                database.selectAll(TABLE_NAME)
        )

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L),
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.toList()
        )
    }

    @Test
    fun testSelectWithColumns() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1"),
                        mapOf(COLUMN_KEY to "key2"),
                        mapOf(COLUMN_KEY to "key3")
                ),
                database.selectAll(TABLE_NAME, COLUMN_KEY)
        )

        assertListEquals(
                listOf(
                        mapOf(COLUMN_VALUE to 1L),
                        mapOf(COLUMN_VALUE to 2L),
                        mapOf(COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME, COLUMN_VALUE) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.toList()
        )
    }

    @Test
    fun testSelectLimit() {
        populateDatabase()

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key1", COLUMN_VALUE to 1L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.limit(1L).toList()
        )

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.limit(-1L).offset(1L).toList()
        )

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.limit(1L).offset(1L).toList()
        )

        assertListEquals(
                listOf(
                        mapOf(COLUMN_KEY to "key2", COLUMN_VALUE to 2L),
                        mapOf(COLUMN_KEY to "key3", COLUMN_VALUE to 3L)
                ),
                database.select(TABLE_NAME) { COLUMN_KEY.isNull() or COLUMN_KEY.isNotNull() }.limit(2L).offset(1L).toList()
        )
    }
}
