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

package com.nlab.reminder.internal.common.di

import android.app.Application
import com.nlab.reminder.core.android.widget.ToastHandle
import com.nlab.reminder.domain.common.util.transaction.TransactionIdGenerator
import com.nlab.reminder.domain.common.util.transaction.impl.DefaultTransactionIdGenerator
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.*
import javax.inject.Singleton

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
class UtilityModule {
    @Reusable
    @Provides
    fun provideCoroutineScope(): TransactionIdGenerator = DefaultTransactionIdGenerator(
        randomPrefix = { UUID.randomUUID().toString() },
        timestamp = { Calendar.getInstance().timeInMillis }
    )

    @Singleton
    @Provides
    fun provideToastHandle(application: Application): ToastHandle = ToastHandle(application)
}