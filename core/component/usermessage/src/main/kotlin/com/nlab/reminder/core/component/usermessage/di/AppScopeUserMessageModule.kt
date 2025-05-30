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

package com.nlab.reminder.core.component.usermessage.di

import com.nlab.reminder.core.component.usermessage.UserMessageFactory
import com.nlab.reminder.core.component.usermessage.UserMessageId
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.UUID

/**
 * @author Thalys
 */
@Module
@InstallIn(SingletonComponent::class)
internal object AppScopeUserMessageModule {
    @Reusable
    @Provides
    fun provideUserMessageFactory(): UserMessageFactory = UserMessageFactory(
        generateUserMessageId = {
            // Using the method of creating a uuid in Compose example jetsnack
            // see https://github.com/android/compose-samples/blob/73b3a51e06a6520efb5b4931e71b771d257bf1dd/Jetsnack/app/src/main/java/com/example/jetsnack/model/SnackbarManager.kt#L39
            UserMessageId(rawId = UUID.randomUUID().mostSignificantBits)
        }
    )
}