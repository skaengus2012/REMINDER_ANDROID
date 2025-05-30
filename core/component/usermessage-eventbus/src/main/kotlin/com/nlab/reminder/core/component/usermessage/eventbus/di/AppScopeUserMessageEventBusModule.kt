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

package com.nlab.reminder.core.component.usermessage.eventbus.di

import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageBroadcast
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageMonitor
import com.nlab.reminder.core.component.usermessage.eventbus.impl.UserMessageBroadcastMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Thalys
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class AppScopeUserMessageEventBusModule {
    @Binds
    abstract fun bindUserMessageMonitor(impl: UserMessageBroadcastMonitor): UserMessageMonitor

    @Binds
    abstract fun bindUserMessageBroadcast(impl: UserMessageBroadcastMonitor): UserMessageBroadcast

    companion object {
        @Singleton
        @Provides
        fun provideUserMessageBroadcastMonitor() = UserMessageBroadcastMonitor()
    }
}