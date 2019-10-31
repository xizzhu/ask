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

import android.database.sqlite.SQLiteDatabase

class TransactionAbortedException : RuntimeException()

/**
 * Run [block] in a transaction. To abort the transaction, simply throw a [TransactionAbortedException].
 *
 * Due to limitation that transactions are thread confined, SQLite accessing code inside the [block]
 * can only run in the current thread.
 *
 * @param exclusive Run the transaction in EXCLUSIVE mode if true, or IMMEDIATE mode otherwise.
 */
inline fun SQLiteDatabase.transaction(exclusive: Boolean = true, block: SQLiteDatabase.() -> Unit) {
    if (exclusive) beginTransaction() else beginTransactionNonExclusive()

    try {
        block()
        setTransactionSuccessful()
    } catch (e: TransactionAbortedException) {
        // do nothing, just abort the transaction
    } finally {
        endTransaction()
    }
}
