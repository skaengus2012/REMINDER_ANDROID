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
@file:Generated
package com.nlab.reminder.core.effect.util

import com.nlab.reminder.core.effect.SideEffect
import com.nlab.reminder.core.effect.SideEffectController
import com.nlab.reminder.core.effect.SideEffectReceiver
import com.nlab.reminder.core.util.test.annotation.Generated
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel

/**
 * @author thalys
 */
@Suppress("FunctionName")
fun <T : SideEffect> SideEffectController(
    eventChannel: Channel<T> = Channel(Channel.BUFFERED),
    dispatcher: CoroutineDispatcher = Dispatchers.Main
): SideEffectController<T> = SideEffectController(eventChannel, dispatcher)


fun <T : SideEffect> SideEffectController<T>.asReceived(): SideEffectReceiver<T> = this