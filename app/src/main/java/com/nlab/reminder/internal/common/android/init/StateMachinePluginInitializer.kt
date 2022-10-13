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

package com.nlab.reminder.internal.common.android.init

import android.content.Context
import androidx.startup.Initializer
import com.nlab.reminder.core.state.StateMachinePlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted

/**
 * @author thalys
 */
@Suppress("unused")
class StateMachinePluginInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        StateMachinePlugin.defaultDispatcher = Dispatchers.Default
        StateMachinePlugin.defaultSharingStarted = SharingStarted.WhileSubscribed(5_000)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}