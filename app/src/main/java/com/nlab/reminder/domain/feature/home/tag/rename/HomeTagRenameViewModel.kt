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

package com.nlab.reminder.domain.feature.home.tag.rename

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlab.reminder.core.effect.SideEffectContainer
import com.nlab.reminder.core.effect.asReceived
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import javax.inject.Inject

/**
 * @author Doohyun
 */
@HiltViewModel
class HomeTagRenameViewModel @Inject constructor(
    stateControllerFactory: HomeTagRenameStateContainerFactory
) : ViewModel() {
    private val sideEffectController = SideEffectContainer<HomeTagRenameSideEffect>()
    private val stateContainer = stateControllerFactory.create(viewModelScope, sideEffectController)

    val homeTagRenameSideEffect = sideEffectController.asReceived()
    val state = stateContainer.stateFlow

    fun invoke(event: HomeTagRenameEvent): Job = stateContainer.send(event)
}