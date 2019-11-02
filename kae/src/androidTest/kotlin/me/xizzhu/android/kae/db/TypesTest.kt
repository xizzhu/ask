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

import me.xizzhu.android.kae.tests.BaseUnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TypesTest : BaseUnitTest() {
    @Test
    fun testColumnModifier() {
        assertEquals("BLOB PRIMARY KEY", (BLOB + PRIMARY_KEY).text)
        assertEquals("INTEGER PRIMARY KEY ON CONFLICT FAIL", (INTEGER + PRIMARY_KEY(ConflictClause.FAIL)).text)
        assertEquals("REAL NOT NULL", (REAL + NOT_NULL).text)
        assertEquals("TEXT NOT NULL ON CONFLICT REPLACE", (TEXT + NOT_NULL(ConflictClause.REPLACE)).text)
        assertEquals("BLOB UNIQUE", (BLOB + UNIQUE).text)
        assertEquals("INTEGER UNIQUE ON CONFLICT IGNORE", (INTEGER + UNIQUE(ConflictClause.IGNORE)).text)
    }
}
