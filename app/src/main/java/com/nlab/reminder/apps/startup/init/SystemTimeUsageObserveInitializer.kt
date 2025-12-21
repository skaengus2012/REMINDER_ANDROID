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
@file:Suppress("unused")

package com.nlab.reminder.apps.startup.init

import android.content.Context
import androidx.startup.Initializer
import com.nlab.reminder.apps.startup.EmptyDependencies
import com.nlab.reminder.core.component.currenttime.SystemTimeUsageMonitor
import com.nlab.reminder.core.component.usermessage.FeedbackPriority
import com.nlab.reminder.core.component.usermessage.UserMessageFactory
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageBroadcast
import com.nlab.reminder.core.inject.qualifiers.coroutine.AppScope
import com.nlab.reminder.core.kotlinx.coroutines.flow.throttleFirst
import com.nlab.reminder.core.text.UiText
import com.nlab.reminder.core.translation.StringIds
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
internal class SystemTimeUsageObserveInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication<SystemTimeUsageObserveInitEntryPoint>(context)
        val coroutineScope = entryPoint.appCoroutineScope()
        val systemTimeUsageMonitor = entryPoint.systemTimeUsageMonitor()
        val userMessageFactory = entryPoint.userMessageFactory()
        val userMessageBroadcast = entryPoint.userMessageBroadcast()
        coroutineScope.launch {
            systemTimeUsageMonitor.event
                .receiveAsFlow()
                .throttleFirst(windowDuration = TOAST_DISPLAY_BASE_THROTTLE_WINDOW_MS)
                .map {
                    userMessageFactory.createMessage(
                        message = UiText(resId = StringIds.message_error_remote_time_loading_failed),
                        priority = FeedbackPriority.LOW
                    )
                }
                .collect(userMessageBroadcast::send)
        }
    }

    override fun dependencies() = EmptyDependencies()

    companion object {
        // android toast is displayed about 3.5 seconds, so proceed to prevent duplicate notation
        private const val TOAST_DISPLAY_BASE_THROTTLE_WINDOW_MS = 4_000L
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SystemTimeUsageObserveInitEntryPoint {
    @AppScope
    fun appCoroutineScope(): CoroutineScope
    fun systemTimeUsageMonitor(): SystemTimeUsageMonitor
    fun userMessageBroadcast(): UserMessageBroadcast
    fun userMessageFactory(): UserMessageFactory
}