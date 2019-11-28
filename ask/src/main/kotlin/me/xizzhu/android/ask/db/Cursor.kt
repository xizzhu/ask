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

import android.database.Cursor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun Cursor.getType(column: String): Int = getType(getColumnIndexOrThrow(column))

fun Cursor.getBlob(column: String): ByteArray = getBlob(getColumnIndexOrThrow(column))

fun Cursor.getDouble(column: String): Double = getDouble(getColumnIndexOrThrow(column))

fun Cursor.getFloat(column: String): Float = getFloat(getColumnIndexOrThrow(column))

fun Cursor.getInt(column: String): Int = getInt(getColumnIndexOrThrow(column))

fun Cursor.getLong(column: String): Long = getLong(getColumnIndexOrThrow(column))

fun Cursor.getShort(column: String): Short = getShort(getColumnIndexOrThrow(column))

fun Cursor.getString(column: String): String = getString(getColumnIndexOrThrow(column))

fun Map<String, Any?>.getBlob(key: String): ByteArray = get(key).let { value ->
    if (value is ByteArray) value else throw NoSuchElementException("No element for '$key'")
}

fun Map<String, Any?>.getDouble(key: String): Double = get(key).let { value ->
    if (value is Double) value else throw NoSuchElementException("No element for '$key'")
}

fun Map<String, Any?>.getFloat(key: String): Float = get(key).let { value ->
    if (value is Double) value.toFloat() else throw NoSuchElementException("No element for '$key'")
}

fun Map<String, Any?>.getInt(key: String): Int = get(key).let { value ->
    if (value is Long) value.toInt() else throw NoSuchElementException("No element for '$key'")
}

fun Map<String, Any?>.getLong(key: String): Long = get(key).let { value ->
    if (value is Long) value else throw NoSuchElementException("No element for '$key'")
}

fun Map<String, Any?>.getShort(key: String): Short = get(key).let { value ->
    if (value is Long) value.toShort() else throw NoSuchElementException("No element for '$key'")
}

fun Map<String, Any?>.getString(key: String): String = get(key).let { value ->
    if (value is String) value else throw NoSuchElementException("No element for '$key'")
}

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
 * @internal
 */
fun Cursor.getRow(): Map<String, Any?> = hashMapOf<String, Any?>().apply {
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

/**
 * Perform the given [action] on all data from the [Cursor].
 *
 * Each element represents one row from the cursor as a [Map]. The key is the column name, and the
 * value is the value of the column.
 */
inline fun Cursor.forEach(action: (Map<String, Any?>) -> Unit) = use {
    while (moveToNext()) action(getRow())
}

/**
 * Perform the given [action] on all data from the [Cursor].
 *
 * Each element represents one row from the cursor as a [Map]. The key is the column name, and the
 * value is the value of the column.
 */
inline fun Cursor.forEachIndexed(action: (Int, Map<String, Any?>) -> Unit) = use {
    var index = 0
    while (moveToNext()) action(index++, getRow())
}

/**
 * Create a [List] that contains all the data from the [Cursor], and close the cursor.
 *
 * Each element in the list represents one row from the cursor as a [Map]. The key is the column
 * name, and the value is the value of the column.
 */
fun Cursor.toList(): List<Map<String, Any?>> = use {
    ArrayList<Map<String, Any?>>().apply {
        ensureCapacity(count)
        while (moveToNext()) add(getRow())
    }
}

/**
 * Create a [List] of items created from each row of the [Cursor] using [converter], and close the cursor.
 */
inline fun <T> Cursor.toList(converter: (Map<String, Any?>) -> T): List<T> = use {
    ArrayList<T>(count).apply { while (moveToNext()) add(converter(getRow())) }
}

/**
 * Create a [List] of items created from each row of the [Cursor] using [converter], and close the cursor.
 */
inline fun <T> Cursor.toListIndexed(converter: (Int, Map<String, Any?>) -> T): List<T> = use {
    ArrayList<T>(count).apply {
        var index = 0
        while (moveToNext()) add(converter(index++, getRow()))
    }
}

/**
 * Create a [Map] that contains data of first item from the [Cursor], and close the cursor.
 *
 * The key is the column name, and the value is the value of the column.
 */
fun Cursor.first(): Map<String, Any?> = use {
    moveToFirst()
    getRow()
}

/**
 * Return an object of type [T] created from first item from the [Cursor] using [converter], and
 * close the cursor.
 */
inline fun <T> Cursor.first(converter: (Map<String, Any?>) -> T): T = converter(first())

/**
 * Create a [Map] that contains data of first item from the [Cursor], and close the cursor.
 *
 * The key is the column name, and the value is the value of the column.
 */
fun Cursor.firstOrDefault(defaultValue: Map<String, Any?>): Map<String, Any?> = use {
    if (moveToFirst()) getRow() else defaultValue
}

/**
 * Return an object of type [T] created from first item from the [Cursor] using [converter], and
 * close the cursor.
 */
inline fun <T> Cursor.firstOrDefault(defaultValue: T, converter: (Map<String, Any?>) -> T): T = use {
    if (moveToFirst()) converter(getRow()) else defaultValue
}

/**
 * Create a [Map] that contains data of first item from the [Cursor], and close the cursor.
 *
 * The key is the column name, and the value is the value of the column.
 */
inline fun Cursor.firstOrDefault(defaultValue: () -> Map<String, Any?>): Map<String, Any?> = use {
    if (moveToFirst()) getRow() else defaultValue()
}

/**
 * Return an object of type [T] created from first item from the [Cursor] using [converter], and
 * close the cursor.
 */
inline fun <T> Cursor.firstOrDefault(defaultValue: () -> T, converter: (Map<String, Any?>) -> T): T = use {
    if (moveToFirst()) converter(getRow()) else defaultValue()
}
