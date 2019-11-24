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

class SQLBuilderTest : BaseUnitTest() {
    @Test
    fun testBuildSqlForCreatingIndex() {
        assertEquals(
                "CREATE INDEX testIndex ON testTables (testColumn);",
                buildSqlForCreatingIndex("testIndex", "testTables", arrayOf("testColumn"), false)
        )
        assertEquals(
                "CREATE INDEX testIndex ON testTables (testColumn1, testColumn2);",
                buildSqlForCreatingIndex("testIndex", "testTables", arrayOf("testColumn1, testColumn2"), false)
        )
        assertEquals(
                "CREATE INDEX IF NOT EXISTS testIndex ON testTables (testColumn);",
                buildSqlForCreatingIndex("testIndex", "testTables", arrayOf("testColumn"), true)
        )
        assertEquals(
                "CREATE INDEX IF NOT EXISTS testIndex ON testTables (testColumn1, testColumn2);",
                buildSqlForCreatingIndex("testIndex", "testTables", arrayOf("testColumn1, testColumn2"), true)
        )
    }
}
