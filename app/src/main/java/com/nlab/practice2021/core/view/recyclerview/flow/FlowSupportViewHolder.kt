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

import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.properties.Delegates

/**
 * @author Doohyun
 */
class FlowSupportViewHolder(
    coroutineScope: CoroutineScope,
    private val viewBinding: ViewBinding,
    private val render: (ViewBinding, Any) -> Unit
) : RecyclerView.ViewHolder(viewBinding.root) {
    private val superVisorJob = SupervisorJob(coroutineScope.coroutineContext.job)
    private val coroutineScope = CoroutineScope(coroutineScope.coroutineContext + superVisorJob)

    private var job: Job by Delegates.observable(Job()) { _, old, _ -> old.cancel() }

    init {
        itemView.doOnDetach { superVisorJob.cancelChildren() }
    }

    fun <T : Any> onBind(flow: Flow<T>) {
        job = coroutineScope.launch { flow.collect { data -> render(viewBinding, data) } }
    }

}