/*
 * Copyright (C) 2022 The N's lab Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nlab.practice2021.core.view.recyclerview

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Doohyun
 */
class ViewHolderLifecycleDelegate<T : RecyclerView.ViewHolder, U>(
    private val context: Context,
    private val viewHolderLifecycle: ViewHolderLifecycle<T, U>
) {
    private val attachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            viewHolderLifecycle.onAttachedRecyclerView()
        }

        override fun onViewDetachedFromWindow(v: View) {
            viewHolderLifecycle.onDetachedRecyclerView()
        }
    }

    fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): T = viewHolderLifecycle.createViewHolder(context, parent, viewType)

    fun onBindViewHolder(
        holder: T,
        model: U
    ) = viewHolderLifecycle.onBindViewHolder(holder, model)

    fun getItemViewType(model: U): Int = viewHolderLifecycle.getItemViewType(model)

    fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        viewHolderLifecycle.onAttachedToRecyclerView()
        recyclerView.addOnAttachStateChangeListener(attachStateChangeListener)
    }

    fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        recyclerView.removeOnAttachStateChangeListener(attachStateChangeListener)
        viewHolderLifecycle.onDetachedFromRecyclerView()
    }

}