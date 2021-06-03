/*
 * Copyright (C) 2018 The N's lab Open Source Project
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
 *
 */

package com.nlab.practice.core.view.recyclerview

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Doohyun
 */
class CommonRecyclerViewAdapter<T : RecyclerView.ViewHolder, U>(
    context: Context,
    viewHolderLifecycle: ViewHolderLifecycle<T, U>
) : RecyclerView.Adapter<T>() {
    private val viewHolderLifecycleDelegate = ViewHolderLifecycleDelegate(context, viewHolderLifecycle)
    private var models: List<U> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): T = viewHolderLifecycleDelegate.onCreateViewHolder(parent, viewType)

    override fun onBindViewHolder(
        holder: T,
        position: Int
    ) = viewHolderLifecycleDelegate.onBindViewHolder(holder, models[position])

    override fun getItemCount(): Int = models.size

    override fun getItemViewType(
        position: Int
    ): Int = viewHolderLifecycleDelegate.getItemViewType(models[position])

    override fun onAttachedToRecyclerView(
        recyclerView: RecyclerView
    ) = viewHolderLifecycleDelegate.onAttachedToRecyclerView(recyclerView)

    override fun onDetachedFromRecyclerView(
        recyclerView: RecyclerView
    ) = viewHolderLifecycleDelegate.onDetachedFromRecyclerView(recyclerView)

    fun replaceItemTo(models: List<U>) { this.models = models }
}