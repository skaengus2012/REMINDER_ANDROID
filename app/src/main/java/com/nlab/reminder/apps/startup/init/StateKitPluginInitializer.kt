/*
 * Copyright (C) 2024 The N's lab Open Source Project
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
@file:Suppress("unused")

package com.nlab.reminder.apps.startup.init

import android.content.Context
import androidx.startup.Initializer
import com.nlab.reminder.core.statekit.plugins.StateKitPlugin
import com.nlab.reminder.apps.startup.EmptyDependencies
import timber.log.Timber

/**
 * @author Doohyun
 */
internal class StateKitPluginInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        StateKitPlugin.addGlobalExceptionHandler { _, throwable -> Timber.tag("StateKitGlobalErr").e(throwable) }
    }

    override fun dependencies() = EmptyDependencies()
}