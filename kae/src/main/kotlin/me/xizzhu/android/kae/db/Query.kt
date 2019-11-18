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

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class Query(private val db: SQLiteDatabase, private val table: String,
            private val columns: Array<out String>, private val where: String = "") {
    private var distinct: Boolean = false
    private var limit: Long = -1L
    private var offset: Long = -1L

    fun distinct(distinct: Boolean): Query = apply { this.distinct = distinct }

    fun limit(limit: Long): Query = apply { this.limit = limit }

    fun offset(offset: Long): Query = apply { this.offset = offset }

    /**
     * Execute the query and return a [Cursor] positioned before the first entry.
     */
    fun asCursor(): Cursor =
            db.query(distinct, table, columns, where, null, null, null, null,
                    buildLimitClause())

    private fun buildLimitClause(): String? {
        if (limit < 0L && offset <= 0L) return null

        return StringBuilder().apply {
            if (offset > 0) append(offset)
            if (isNotEmpty()) append(',')

            // in case limit < 0, it means we have offset, we need to put the limit there, but Android
            // doesn't like a negative limit, so we put Long.MAX_VALUE in this case
            append(if (limit < 0) Long.MAX_VALUE else limit)
        }.toString()
    }

    /**
     * Execute the query and return a [List] containing all the rows from the query.
     */
    fun toList(): List<Map<String, Any?>> = asCursor().toList()
}
