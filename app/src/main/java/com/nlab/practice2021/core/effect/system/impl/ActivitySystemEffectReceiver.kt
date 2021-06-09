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

package com.nlab.practice2021.core.effect.system.impl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nlab.practice2021.core.effect.system.EffectObserver
import com.nlab.practice2021.core.effect.system.impl.AndroidSystemEffect.Command
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
class ActivitySystemEffectReceiver(
    private val activity: AppCompatActivity,
    private val commandFlow: Flow<Command>
) : EffectObserver {
    private var job: Job? = null

    override fun register() {
        job = activity.lifecycleScope.launch {
            commandFlow.collect { it() }
        }
    }

    private operator fun Command.invoke() = when(this) {
        is Command.NavigateActivity -> onStartNavigateActivity(this)
    }

    override fun unRegister() {
        job?.cancel()
    }

    private fun onStartNavigateActivity(command: Command.NavigateActivity) {
        activity.startActivity(Intent(activity, command.clazz.java).apply {
            putExtras(command.bundle)
        })
    }
}