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

package com.nlab.reminder.core.foundation.di

import com.nlab.reminder.core.foundation.qualifiers.coroutine.AppScope
import com.nlab.reminder.core.foundation.qualifiers.coroutine.Dispatcher
import com.nlab.reminder.core.foundation.qualifiers.coroutine.DispatcherOption.*
import com.nlab.reminder.core.foundation.time.TimestampProvider
import com.nlab.reminder.core.foundation.time.infra.DefaultTimestampProvider
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal class AppScopeFoundationModule {
    @Provides
    @Dispatcher(Default)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Dispatcher(IO)
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(Main)
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    @AppScope
    fun provideAppScope(
        @Dispatcher(Default) dispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(
        context = SupervisorJob() + dispatcher + CoroutineName(name = "ReminderAppScope")
    )

    @Reusable
    @Provides
    fun provideTimestampProvider(): TimestampProvider = DefaultTimestampProvider()
}