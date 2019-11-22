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

package me.xizzhu.android.kae.view

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes

fun View.hideKeyboard() {
    if (hasFocus()) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

/**
 * Set the background to the given attribute with [resId].
 *
 * Application can apply ripple effect to the [View] with [android.R.attr.selectableItemBackground]
 * or [android.R.attr.selectableItemBackgroundBorderless].
 */
fun View.setBackground(@AttrRes resId: Int) = with(TypedValue()) {
    context.theme.resolveAttribute(resId, this, true)
    setBackgroundResource(resourceId)
}
