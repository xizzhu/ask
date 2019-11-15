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

sealed class Where(internal val text: String) {
    class IsNull(key: String) : Where("$key IS NULL")

    class IsNotNull(key: String) : Where("$key IS NOT NULL")

    class Equal<T>(key: String, value: T) : Where("$key = '${value.toString()}'")

    class NotEqual<T>(key: String, value: T) : Where("$key != '${value.toString()}'")

    class Less<T>(key: String, value: T) : Where("$key < '${value.toString()}'")

    class LessOrEqual<T>(key: String, value: T) : Where("$key <= '${value.toString()}'")

    class Greater<T>(key: String, value: T) : Where("$key > '${value.toString()}'")

    class GreaterOrEqual<T>(key: String, value: T) : Where("$key >= '${value.toString()}'")

    class Like(key: String, pattern: String) : Where("$key LIKE '$pattern'")

    class NotLike(key: String, pattern: String) : Where("$key NOT LIKE '$pattern'")

    class And(exp1: Where, exp2: Where) : Where("${exp1.text} AND ${exp2.text}")

    class Or(exp1: Where, exp2: Where) : Where("${exp1.text} OR ${exp2.text}")
}

object WhereBuilder {
    fun String.isNull(): Where.IsNull = Where.IsNull(this)

    fun String.isNotNull(): Where.IsNotNull = Where.IsNotNull(this)

    infix fun <T> String.eq(value: T): Where.Equal<T> = Where.Equal(this, value)

    infix fun <T> String.neq(value: T): Where.NotEqual<T> = Where.NotEqual(this, value)

    infix fun <T> String.less(value: T): Where.Less<T> = Where.Less(this, value)

    infix fun <T> String.lessEq(value: T): Where.LessOrEqual<T> = Where.LessOrEqual(this, value)

    infix fun <T> String.greater(value: T): Where.Greater<T> = Where.Greater(this, value)

    infix fun <T> String.greaterEq(value: T): Where.GreaterOrEqual<T> = Where.GreaterOrEqual(this, value)

    infix fun String.like(pattern: String): Where.Like = Where.Like(this, pattern)

    infix fun String.notLike(pattern: String): Where.NotLike = Where.NotLike(this, pattern)

    infix fun Where.and(another: Where): Where = Where.And(this, another)

    infix fun Where.or(another: Where): Where = Where.Or(this, another)
}
