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

fun avg(column: String, distinct: Boolean = false): String = "AVG(${if (distinct) "DISTINCT " else ""}$column)"

fun count(column: String, distinct: Boolean = false): String = "COUNT(${if (distinct) "DISTINCT " else ""}$column)"

fun min(column: String): String = "MIN($column)"

fun max(column: String): String = "MAX($column)"

fun sum(column: String, distinct: Boolean = false): String = "SUM(${if (distinct) "DISTINCT " else ""}$column)"
