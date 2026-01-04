/*
 * Copyright (C) 2026 The N's lab Open Source Project
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
import com.nlab.reminder.apps.startup.dependenciesOf
import com.nlab.reminder.core.component.schedule.RegisterScheduleCompleteJobUseCase
import com.nlab.reminder.core.inject.qualifiers.coroutine.AppScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * @author Doohyun
 */
class ScheduleCompletionCleanupInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication<ScheduleCompletionCleanupInitializerEntryPoint>(context)
        val applicationScope = entryPoint.applicationCoroutineScope()
        val registerScheduleCompleteJobUseCase = entryPoint.registerScheduleCompleteJob()
        applicationScope.launch(Dispatchers.Default) {
            registerScheduleCompleteJobUseCase(debounceTimeout = 0.seconds, processUntilPriority = null)
        }
    }

    override fun dependencies() = dependenciesOf(WorkManagerInitializer::class)
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ScheduleCompletionCleanupInitializerEntryPoint {
    @AppScope
    fun applicationCoroutineScope(): CoroutineScope
    fun registerScheduleCompleteJob(): RegisterScheduleCompleteJobUseCase
}