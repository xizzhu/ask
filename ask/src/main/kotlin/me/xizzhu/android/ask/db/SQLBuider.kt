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

import me.xizzhu.android.ask.utils.forEachIndexed

/**
 * @internal
 */
fun buildSqlForCreatingTable(table: String, ifNotExists: Boolean, columnDefinitions: Map<String, ColumnModifiers>): String {
    val sqlBuilder = StringBuilder("CREATE TABLE ")
    if (ifNotExists) sqlBuilder.append("IF NOT EXISTS ")
    sqlBuilder.append(table)

    sqlBuilder.append(" (")

    val primaryKeys = arrayListOf<String>()
    var primaryKeyConflictClause: ConflictClause? = null
    val foreignKeys = hashMapOf<String, ForeignKey>()
    columnDefinitions.forEachIndexed { index, (columnName, modifiers) ->
        if (index > 0) sqlBuilder.append(", ")
        sqlBuilder.append(columnName)

        modifiers.modifiers.forEach { modifier ->
            when (modifier) {
                is PrimaryKey -> {
                    primaryKeys.add(columnName)
                    if (primaryKeyConflictClause == null) primaryKeyConflictClause = modifier.conflictClause
                }
                is ForeignKey -> {
                    foreignKeys[columnName] = modifier
                }
                else -> {
                    sqlBuilder.append(' ').append(modifier.text)
                }
            }
        }
    }
    if (primaryKeys.isNotEmpty()) {
        sqlBuilder.append(", PRIMARY KEY(")
        primaryKeys.forEachIndexed { index, primaryKey ->
            if (index > 0) sqlBuilder.append(", ")
            sqlBuilder.append(primaryKey)
        }
        sqlBuilder.append(')')
        primaryKeyConflictClause?.let { sqlBuilder.append(' ').append(it.text) }
    }
    foreignKeys.forEach { (columnName, foreignKey) ->
        sqlBuilder.append(", FOREIGN KEY(")
                .append(columnName)
                .append(") REFERENCES ")
                .append(foreignKey.referenceTable)
                .append('(')
                .append(foreignKey.referenceColumn)
                .append(')')

        foreignKey.constraints.forEach { constraint ->
            sqlBuilder.append(' ').append(constraint.text)
        }
    }

    sqlBuilder.append(");")

    return sqlBuilder.toString()
}

/**
 * @internal
 */
fun buildSqlForCreatingIndex(index: String, table: String, columns: Array<out String>, ifNotExists: Boolean)
        : String = StringBuilder().run {
    append("CREATE INDEX ")
    if (ifNotExists) append("IF NOT EXISTS ")
    append(index)
    append(" ON ")
    append(table)
    append(" (")
    columns.forEachIndexed { index, column ->
        if (index > 0) append(", ")
        append(column)
    }
    append(");")

    toString()
}

/**
 * @internal
 */
fun buildSqlForWhere(condition: Condition): String = condition.text

// A string constant is formed by enclosing the string in single quotes ('). A single quote within
// the string can be encoded by putting two single quotes in a row - as in Pascal. C-style escapes
// using the backslash character are not supported because they are not standard SQL.
// Ref. https://www.sqlite.org/lang_expr.html
internal fun String.escape(): String = replace("'", "''")

internal fun <T> Iterable<T>.toSqlInExpression(): String =
        joinToString(prefix = "'", postfix = "'", separator = "', '", transform = { it.toString().escape() })
