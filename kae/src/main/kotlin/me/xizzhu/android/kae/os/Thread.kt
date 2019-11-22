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

package me.xizzhu.android.kae.os

import android.os.Handler
import android.os.Looper

fun isUiThread(): Boolean = Looper.getMainLooper() == Looper.myLooper()

/**
 * Run the [action] on UI thread.
 *
 * If it is called on UI thread, [action] will be executed immediately.
 */
inline fun runOnUiThread(crossinline action: () -> Unit) {
    if (isUiThread()) action()
    else Handler(Looper.getMainLooper()).post { action() }
}

/**
 * Run the [action] on UI thread with the given [delayMillis].
 */
inline fun runDelayedOnUiThread(delayMillis: Long, crossinline action: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({ action() }, delayMillis)
}

/**
 * Run the [action] on current thread with the given [delayMillis].
 */
inline fun runDelayed(delayMillis: Long, crossinline action: () -> Unit) {
    Handler().postDelayed({ action() }, delayMillis)
}
