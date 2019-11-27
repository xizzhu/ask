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

sealed class Condition(internal val text: String) {
    object NoOp : Condition("")

    class IsNull(key: String) : Condition("$key IS NULL")

    class IsNotNull(key: String) : Condition("$key IS NOT NULL")

    class Equal<T>(key: String, value: T) : Condition("$key = '${value.toString()}'")

    class NotEqual<T>(key: String, value: T) : Condition("$key != '${value.toString()}'")

    class Less<T>(key: String, value: T) : Condition("$key < ${value.toString()}")

    class LessOrEqual<T>(key: String, value: T) : Condition("$key <= ${value.toString()}")

    class Greater<T>(key: String, value: T) : Condition("$key > ${value.toString()}")

    class GreaterOrEqual<T>(key: String, value: T) : Condition("$key >= ${value.toString()}")

    class Between<T>(key: String, from: T, to: T) : Condition("$key BETWEEN ${from.toString()} AND ${to.toString()}")

    class Like(key: String, pattern: String) : Condition("$key LIKE '$pattern'")

    class NotLike(key: String, pattern: String) : Condition("$key NOT LIKE '$pattern'")

    class Glob(key: String, pattern: String) : Condition("$key GLOB '$pattern'")

    class InList<T>(key: String, values: Iterable<T>) : Condition("$key IN (${values.joinToString(prefix = "'", postfix = "'", separator = "', '")})")

    class NotInList<T>(key: String, values: Iterable<T>) : Condition("$key NOT IN (${values.joinToString(prefix = "'", postfix = "'", separator = "', '")})")

    class And(exp1: Condition, exp2: Condition) : Condition("${exp1.text} AND ${exp2.text}")

    class Or(exp1: Condition, exp2: Condition) : Condition("${exp1.text} OR ${exp2.text}")

    class Not(exp: Condition) : Condition("NOT ${exp.text}")
}

object ConditionBuilder {
    fun noOp(): Condition.NoOp = Condition.NoOp

    fun String.isNull(): Condition.IsNull = Condition.IsNull(this)

    fun String.isNotNull(): Condition.IsNotNull = Condition.IsNotNull(this)

    infix fun <T> String.eq(value: T): Condition.Equal<T> = Condition.Equal(this, value)

    infix fun <T> String.neq(value: T): Condition.NotEqual<T> = Condition.NotEqual(this, value)

    infix fun <T> String.less(value: T): Condition.Less<T> = Condition.Less(this, value)

    infix fun <T> String.lessEq(value: T): Condition.LessOrEqual<T> = Condition.LessOrEqual(this, value)

    infix fun <T> String.greater(value: T): Condition.Greater<T> = Condition.Greater(this, value)

    infix fun <T> String.greaterEq(value: T): Condition.GreaterOrEqual<T> = Condition.GreaterOrEqual(this, value)

    fun <T> String.between(from: T, to: T): Condition.Between<T> = Condition.Between(this, from, to)

    infix fun String.like(pattern: String): Condition.Like = Condition.Like(this, pattern)

    infix fun String.notLike(pattern: String): Condition.NotLike = Condition.NotLike(this, pattern)

    infix fun String.glob(pattern: String): Condition.Glob = Condition.Glob(this, pattern)

    infix fun <T> String.inList(values: Iterable<T>): Condition.InList<T> = Condition.InList(this, values)

    infix fun <T> String.notInList(values: Iterable<T>): Condition.NotInList<T> = Condition.NotInList(this, values)

    infix fun Condition.and(another: Condition): Condition = Condition.And(this, another)

    infix fun Condition.or(another: Condition): Condition = Condition.Or(this, another)

    fun not(condition: Condition): Condition = Condition.Not(condition)
}
