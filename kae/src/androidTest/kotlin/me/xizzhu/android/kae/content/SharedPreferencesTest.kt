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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.kae.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedPreferencesTest : BaseUnitTest() {
    @BeforeTest
    override fun setup() {
        super.setup()

        ApplicationProvider.getApplicationContext<Context>()
                .getSharedPreferences("test_shared_pref", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
    }

    @Test
    fun testEdit() {
        ApplicationProvider.getApplicationContext<Context>()
                .getSharedPreferences("test_shared_pref", Context.MODE_PRIVATE)
                .edit {
                    putLong("test_key_1", 1L)
                    putString("test_key_2", "test_value_2")
                }

        val actual = ApplicationProvider.getApplicationContext<Context>()
                .getSharedPreferences("test_shared_pref", Context.MODE_PRIVATE).all
        assertEquals(2, actual.size)
        assertEquals(1L, actual["test_key_1"])
        assertEquals("test_value_2", actual["test_key_2"])
    }
}
