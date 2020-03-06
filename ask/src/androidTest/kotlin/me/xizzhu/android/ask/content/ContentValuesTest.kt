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

package me.xizzhu.android.ask.content

import me.xizzhu.android.ask.tests.BaseUnitTest
import org.junit.Assert.assertArrayEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContentValuesTest : BaseUnitTest() {
    @Test
    fun testToContentValues() {
        val actual = mapOf(
                "null" to null,
                "boolean" to false,
                "byte" to 123.toByte(),
                "byteArray" to byteArrayOf(4, 5, 6),
                "double" to 789.0123,
                "float" to 456.78F,
                "int" to 901,
                "long" to 2345678L,
                "short" to 9012.toShort(),
                "string" to "string"
        ).toContentValues()
        assertEquals(10, actual.size())
        assertNull(actual.get("null"))
        assertEquals(false, actual.get("boolean"))
        assertEquals(123.toByte(), actual.get("byte"))
        assertArrayEquals(byteArrayOf(4, 5, 6), actual.get("byteArray") as ByteArray)
        assertEquals(789.0123, actual.get("double"))
        assertEquals(456.78F, actual.get("float"))
        assertEquals(901, actual.get("int"))
        assertEquals(2345678L, actual.get("long"))
        assertEquals(9012.toShort(), actual.get("short"))
        assertEquals("string", actual.get("string"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testUnsupportedValueType() {
        mapOf("key" to Unit).toContentValues()
    }
}
