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

enum class ConflictClause(val text: String) {
    ABORT("ON CONFLICT ABORT"),
    FAIL("ON CONFLICT FAIL"),
    IGNORE("ON CONFLICT IGNORE"),
    REPLACE("ON CONFLICT REPLACE"),
    ROLLBACK("ON CONFLICT ROLLBACK")
}

class ColumnModifier(val text: String) {
    operator fun plus(other: ColumnModifier): ColumnModifier {
        return ColumnModifier("$text ${other.text}")
    }
}

val BLOB = ColumnModifier("BLOB")
val INTEGER = ColumnModifier("INTEGER")
val REAL = ColumnModifier("REAL")
val TEXT = ColumnModifier("TEXT")

val PRIMARY_KEY = ColumnModifier("PRIMARY KEY")
fun PRIMARY_KEY(conflictClause: ConflictClause) = ColumnModifier("PRIMARY KEY ${conflictClause.text}")

val NOT_NULL = ColumnModifier("NOT NULL")
fun NOT_NULL(conflictClause: ConflictClause) = ColumnModifier("NOT NULL ${conflictClause.text}")

val UNIQUE = ColumnModifier("UNIQUE")
fun UNIQUE(conflictClause: ConflictClause) = ColumnModifier("UNIQUE ${conflictClause.text}")
