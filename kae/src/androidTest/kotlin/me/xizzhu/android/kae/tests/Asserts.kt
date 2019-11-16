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

package me.xizzhu.android.kae.tests

import kotlin.test.*

fun <T> assertListEquals(expected: List<T>, actual: List<T>) {
    assertEquals(expected.count(), actual.count(), "List size mismatch")

    val expectedIterator = expected.iterator()
    val actualIterator = actual.iterator()
    while (expectedIterator.hasNext() && actualIterator.hasNext()) {
        val expectedElement = expectedIterator.next()
        val actualElement = actualIterator.next()
        when (expectedElement) {
            is List<*> -> if (actualElement is List<*>) assertListEquals(expectedElement, actualElement) else fail("Element type mismatch")
            is Map<*, *> -> if (actualElement is Map<*, *>) assertMapEquals(expectedElement as Map<Any?, Any?>, actualElement as Map<Any?, Any?>) else fail("Element type mismatch")
            else -> assertEquals(expectedElement, actualElement)
        }
    }
}

fun <K, V> assertMapEquals(expected: Map<K, V>, actual: Map<K, V>) {
    assertEquals(expected.count(), actual.count(), "Map size mismatch")
    expected.forEach { (key, expectedValue) ->
        if (!actual.containsKey(key)) fail("Missing value for '$key' from the map")

        val actualValue = actual[key]
        when (expectedValue) {
            is List<*> -> if (actualValue is List<*>) assertListEquals(expectedValue, actualValue) else fail("Element type mismatch")
            is Map<*, *> -> if (actualValue is Map<*, *>) assertMapEquals(expectedValue as Map<Any?, Any?>, actualValue as Map<Any?, Any?>) else fail("Element type mismatch")
            else -> assertEquals(expectedValue, actualValue)
        }
    }
}
