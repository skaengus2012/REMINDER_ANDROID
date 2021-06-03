/*
 * Copyright (C) 2018 The N's lab Open Source Project
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
 *
 */

package com.nlab.practice.domain.home

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.nlab.practice.core.effect.system.Destination
import com.nlab.practice.core.effect.system.SystemEffect
import com.nlab.practice.core.view.recyclerview.flow.FlowItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
class HomeItemViewModel private constructor(
    systemEffect: SystemEffect,
    coroutineScope: CoroutineScope,
    destination: Destination,
    @StringRes titleResource: Int,
    @StringRes descriptionResource: Int,
    @ColorRes backgroundResource: Int,
) : FlowItem {

    override val stateFlow: Flow<State> = flow {
        emit(
            State(
                titleResource,
                descriptionResource,
                backgroundResource,
                onClickItem = {
                    coroutineScope.launch { systemEffect.navigateTo(destination) }
                }
            )
        )
    }

    data class State(
        @StringRes val titleResource: Int,
        @StringRes val descriptionResource: Int,
        @ColorRes val backgroundResource: Int,
        val onClickItem: () -> Unit
    ) : FlowItem.State

    class Factory(
        private val systemEffect: SystemEffect,
    ) {
        fun create(
            coroutineScope: CoroutineScope,
            destination: Destination,
            @StringRes titleResource: Int,
            @StringRes labelResource: Int,
            @ColorRes backgroundResource: Int
        ): HomeItemViewModel = HomeItemViewModel(
            systemEffect,
            coroutineScope,
            destination,
            titleResource,
            labelResource,
            backgroundResource
        )
    }

}