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

package com.nlab.practice2021.domain.home.view

import androidx.core.content.ContextCompat
import com.nlab.practice2021.core.view.recyclerview.flow.FlowItemTypeViewHolderMaker
import com.nlab.practice2021.core.view.recyclerview.ViewHolderMaker
import com.nlab.practice2021.databinding.ViewHomeItemBinding
import com.nlab.practice2021.domain.home.HomeItemViewModel

/**
 * @author Doohyun
 */
class HomeItemViewModelViewHolderMaker : ViewHolderMaker by FlowItemTypeViewHolderMaker(
    HomeItemViewModel::class,
    HomeItemViewModel.State::class,
    ViewHomeItemBinding::class,
    inflate = { inflater, parent -> ViewHomeItemBinding.inflate(inflater, parent, false) },
    render = { binding, state ->
        val context = binding.root.context
        binding.root.setOnClickListener { state.onClickItem() }
        binding.root.setBackgroundColor(ContextCompat.getColor(context, state.backgroundResource))
        binding.title.setText(state.titleResource)
        binding.description.setText(state.descriptionResource)
    }
)