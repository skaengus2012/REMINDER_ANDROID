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

package com.nlab.practice2021.core.view.recyclerview.flow

import com.nlab.practice2021.core.view.recyclerview.ViewHolderMaker
import com.nlab.practice2021.databinding.ViewEmptyItemBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * @author Doohyun
 */
class EmptyViewHolderMaker : ViewHolderMaker by FlowItemTypeViewHolderMaker(
    EmptyFlowItem::class,
    EmptyFlowItem.State::class,
    ViewEmptyItemBinding::class,
    inflate = { inflater, parent -> ViewEmptyItemBinding.inflate(inflater, parent, false) },
    render = { _, _ -> }
) {
    object EmptyFlowItem : FlowItem {
        override val stateFlow: Flow<FlowItem.State> = flow { emit(State()) }
        class State : FlowItem.State
    }
}