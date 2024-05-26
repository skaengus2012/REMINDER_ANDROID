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

package com.nlab.reminder.core.di.coroutine

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.annotation.AnnotationRetention.BINARY


/**
 * Annotations for CoroutineDispatcher injection.
 * Using [option], you can define the Dispatcher to be injected.
 *
 * @see [com.nlab.reminder.core.di.coroutine.DispatcherOption]
 * @author Doohyun
 */
@Qualifier
@Retention(BINARY)
annotation class Dispatcher(val option: DispatcherOption)

enum class DispatcherOption {
    Default,
    IO,
    Main
}

@Module
@InstallIn(SingletonComponent::class)
internal class DispatcherModule {
    @Provides
    @Dispatcher(DispatcherOption.Default)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Dispatcher(DispatcherOption.IO)
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(DispatcherOption.Main)
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}