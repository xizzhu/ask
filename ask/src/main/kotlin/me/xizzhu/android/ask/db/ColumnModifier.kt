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

enum class ConflictClause(internal val text: String) {
    ABORT("ON CONFLICT ABORT"),
    FAIL("ON CONFLICT FAIL"),
    IGNORE("ON CONFLICT IGNORE"),
    REPLACE("ON CONFLICT REPLACE"),
    ROLLBACK("ON CONFLICT ROLLBACK")
}

sealed class ColumnModifier(internal val text: String)

private class ColumnModifierImpl(text: String) : ColumnModifier(text)

class PrimaryKey(val conflictClause: ConflictClause? = null) : ColumnModifier("")

class ForeignKey(val referenceTable: String, val referenceColumn: String, val constraints: List<Constraint>) : ColumnModifier("") {
    sealed class Constraint(internal val text: String) {
        enum class Action(internal val text: String) {
            CASCADE("CASCADE"),
            NO_ACTION("NO ACTION"),
            SET_DEFAULT("SET DEFAULT"),
            SET_NULL("SET NULL"),
            SET_RESTRICT("SET RESTRICT")
        }

        class OnDelete(action: Action) : Constraint("ON DELETE ${action.text}")
        class OnUpdate(action: Action) : Constraint("ON UPDATE ${action.text}")
    }
}

class ColumnModifiers(val modifiers: List<ColumnModifier>) {
    constructor(modifier: ColumnModifier) : this(listOf(modifier))

    operator fun plus(modifier: ColumnModifier): ColumnModifiers {
        return ColumnModifiers(ArrayList<ColumnModifier>(modifiers).apply { add(modifier) })
    }
}

val PRIMARY_KEY: ColumnModifier = PrimaryKey()
fun PRIMARY_KEY(conflictClause: ConflictClause): ColumnModifier = PrimaryKey(conflictClause)

val NOT_NULL: ColumnModifier = ColumnModifierImpl("NOT NULL")
fun NOT_NULL(conflictClause: ConflictClause): ColumnModifier = ColumnModifierImpl("NOT NULL ${conflictClause.text}")

val UNIQUE: ColumnModifier = ColumnModifierImpl("UNIQUE")
fun UNIQUE(conflictClause: ConflictClause): ColumnModifier = ColumnModifierImpl("UNIQUE ${conflictClause.text}")

fun DEFAULT(value: String): ColumnModifier = ColumnModifierImpl("DEFAULT $value")

fun FOREIGN_KEY(referenceTable: String, referenceColumn: String,
                vararg constraints: ForeignKey.Constraint)
        : ForeignKey = ForeignKey(referenceTable, referenceColumn, constraints.toList())

fun ON_DELETE(action: ForeignKey.Constraint.Action): ForeignKey.Constraint.OnDelete =
        ForeignKey.Constraint.OnDelete(action)

fun ON_UPDATE(action: ForeignKey.Constraint.Action): ForeignKey.Constraint.OnUpdate =
        ForeignKey.Constraint.OnUpdate(action)

val BLOB = ColumnModifiers(ColumnModifierImpl("BLOB"))
val INTEGER = ColumnModifiers(ColumnModifierImpl("INTEGER"))
val REAL = ColumnModifiers(ColumnModifierImpl("REAL"))
val TEXT = ColumnModifiers(ColumnModifierImpl("TEXT"))
