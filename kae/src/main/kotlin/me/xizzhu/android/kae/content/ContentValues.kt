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

package me.xizzhu.android.kae.content

import android.content.ContentValues

/**
 * Create a new [ContentValues] using values from the [Map].
 *
 * @throws IllegalArgumentException When the type of a value is not allowed in [ContentValues].
 */
fun Map<String, Any?>.toContentValues(): ContentValues = ContentValues(size).apply {
    forEach { (key, value) ->
        when (value) {
            null -> putNull(key)
            is Boolean -> put(key, value)
            is Byte -> put(key, value)
            is ByteArray -> put(key, value)
            is Double -> put(key, value)
            is Float -> put(key, value)
            is Int -> put(key, value)
            is Long -> put(key, value)
            is Short -> put(key, value)
            is String -> put(key, value)
            else -> throw IllegalArgumentException("Unsupported value type: ${value.javaClass.name}, key: $key")
        }
    }
}
