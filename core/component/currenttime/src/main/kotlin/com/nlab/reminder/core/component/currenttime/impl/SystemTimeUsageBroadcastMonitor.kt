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

package com.nlab.reminder.core.component.currenttime.impl

import com.nlab.reminder.core.component.currenttime.SystemTimeUsageBroadcast
import com.nlab.reminder.core.component.currenttime.SystemTimeUsageMonitor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * @author Doohyun
 */
internal class SystemTimeUsageBroadcastMonitor : SystemTimeUsageMonitor, SystemTimeUsageBroadcast {
    private val _event = Channel<Unit>(capacity = Channel.BUFFERED)
    override val event: ReceiveChannel<Unit> = _event

    override suspend fun notifyEvent() {
        _event.send(Unit)
    }
}