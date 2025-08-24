/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.apps.startup.init

import android.content.Context
import androidx.startup.Initializer
import com.nlab.statekit.foundation.plugins.GlobalEffect
import com.nlab.statekit.foundation.plugins.StateKitPlugin
import timber.log.Timber

/**
 * @author Doohyun
 */
internal class StateKitPluginDebugBuildInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        StateKitPlugin.configGlobalStore { currentConfiguration ->
            currentConfiguration.copy(defaultEffects = listOf(GlobalEffect { action, current ->
                Timber.tag(tag = "reduce.trace").d(
                    message = "action: ${action::class.qualifiedName}, current: ${current::class.qualifiedName}"
                )
            }))
        }
    }

    override fun dependencies() = listOf(
        StateKitPluginInitializer::class.java
    )
}