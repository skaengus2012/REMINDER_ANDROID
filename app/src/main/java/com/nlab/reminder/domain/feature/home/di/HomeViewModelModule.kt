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

package com.nlab.reminder.domain.feature.home.di

import com.nlab.reminder.core.effect.SideEffectController
import com.nlab.reminder.core.effect.SideEffectReceiver
import com.nlab.reminder.core.effect.util.SideEffectController
import com.nlab.reminder.domain.feature.home.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * @author Doohyun
 */
@Module
@InstallIn(ViewModelComponent::class)
class HomeViewModelModule {
    @ViewModelScoped
    @Provides
    fun provideHomeSideEffectController(): SideEffectController<HomeSideEffect> = SideEffectController()

    @Provides
    fun provideHomeSideEffectReceiver(
        controller: SideEffectController<HomeSideEffect>
    ): SideEffectReceiver<HomeSideEffect> = controller

    @Provides
    fun provideHomeStateMachine(
        homeSideEffect: SideEffectController<HomeSideEffect>,
        getHomeSummary: GetHomeSummaryUseCase,
        getTagUsageCount: GetTagUsageCountUseCase,
        modifyTagName: ModifyTagNameUseCase,
        deleteTag: DeleteTagUseCase
    ): HomeStateMachine = HomeStateMachine(
        homeSideEffect,
        getHomeSummary,
        getTagUsageCount,
        modifyTagName,
        deleteTag
    )
}