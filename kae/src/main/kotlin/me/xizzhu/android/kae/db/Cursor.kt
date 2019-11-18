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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Create a [Iterable] that returns all the data from the [Cursor].
 *
 * Each element in the iterator represents one row from the cursor as a [Map]. The key is the column
 * name, and the value is the value of the column.
 */
fun Cursor.asIterable(): Iterable<Map<String, Any?>> = Iterable {
    object : Iterator<Map<String, Any?>> {
        override fun hasNext(): Boolean = position < count - 1

        override fun next(): Map<String, Any?> {
            moveToNext()
            return getRow()
        }
    }
}

/**
 * Create a [Sequence] that returns all the data from the [Cursor].
 *
 * Each element in the sequence represents one row from the cursor as a [Map]. The key is the column
 * name, and the value is the value of the column.
 */
fun Cursor.asSequence(): Sequence<Map<String, Any?>> = generateSequence {
    if (moveToNext()) getRow() else null
}

/**
 * Create a [Flow] that returns all the data from the [Cursor].
 *
 * Each element in the flow represents one row from the cursor as a [Map]. The key is the column
 * name, and the value is the value of the column.
 */
fun Cursor.asFlow(): Flow<Map<String, Any?>> = flow { while (moveToNext()) emit(getRow()) }

private fun Cursor.getRow(): Map<String, Any?> = hashMapOf<String, Any?>().apply {
    (0 until columnCount).forEach { index ->
        put(
                getColumnName(index),
                when (getType(index)) {
                    Cursor.FIELD_TYPE_BLOB -> getBlob(index)
                    Cursor.FIELD_TYPE_FLOAT -> getDouble(index)
                    Cursor.FIELD_TYPE_INTEGER -> getLong(index)
                    Cursor.FIELD_TYPE_NULL -> null
                    Cursor.FIELD_TYPE_STRING -> getString(index)
                    else -> null
                }
        )
    }
}

/**
 * Create a [List] that contains all the data from the [Cursor], and close the cursor.
 *
 * Each element in the list represents one row from the cursor as a [Map]. The key is the column
 * name, and the value is the value of the column.
 */
fun Cursor.toList(): List<Map<String, Any?>> = ArrayList<Map<String, Any?>>().apply {
    this@toList.use { cursor ->
        ensureCapacity(cursor.count)
        while (cursor.moveToNext()) add(cursor.getRow())
    }
}
