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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Doohyun
 */
interface ViewHolderLifecycle<T : RecyclerView.ViewHolder, U> {
    fun createViewHolder(context: Context, parent: ViewGroup, viewType: Int): T
    fun onBindViewHolder(holder: T, data: U)
    fun getItemViewType(data: U) = 0
    fun onAttachedToRecyclerView() = Unit
    fun onDetachedFromRecyclerView() = Unit
    fun onAttachedRecyclerView() = Unit
    fun onDetachedRecyclerView() = Unit
}