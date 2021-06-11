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

package com.nlab.practice2021.core.view.recyclerview.flow

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nlab.practice2021.core.view.recyclerview.ViewHolderLifecycle
import com.nlab.practice2021.core.view.recyclerview.ViewHolderMakerPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job

/**
 * @author Doohyun
 */
class FlowSupportViewHolderLifecycle<T : FlowItem>(
    coroutineScope: CoroutineScope,
    private val viewHolderMakerPolicy: ViewHolderMakerPolicy
) : ViewHolderLifecycle<FlowSupportViewHolder, T> {
    private val supervisorJob = SupervisorJob(coroutineScope.coroutineContext.job)
    private val coroutineScope = CoroutineScope(coroutineScope.coroutineContext + supervisorJob)

    override fun createViewHolder(context: Context, parent: ViewGroup, viewType: Int): FlowSupportViewHolder {
        return FlowSupportViewHolder(
            coroutineScope,
            viewHolderMakerPolicy.inflate(LayoutInflater.from(context), parent, viewType),
            render = viewHolderMakerPolicy.renderStrategy(viewType)
        )
    }

    override fun onBindViewHolder(holder: FlowSupportViewHolder, data: T) {
        holder.onBind(data.stateFlow)
    }

    override fun getItemViewType(data: T): Int = viewHolderMakerPolicy.getViewType(data::class)

    override fun onDetachedFromRecyclerView() {
        supervisorJob.cancelChildren()
    }

    override fun onDetachedRecyclerView() {
        supervisorJob.cancelChildren()
    }

}