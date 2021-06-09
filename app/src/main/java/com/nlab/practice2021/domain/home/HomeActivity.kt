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

package com.nlab.practice2021.domain.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nlab.practice2021.core.effect.system.impl.ActivitySystemEffectReceiver
import com.nlab.practice2021.core.effect.system.impl.AndroidSystemEffect
import com.nlab.practice2021.core.effect.system.impl.AndroidSystemEffectFlowProvider
import com.nlab.practice2021.core.lifecycle.CompositeLifecycleEventObserver
import com.nlab.practice2021.core.lifecycle.SimpleViewModelFactory
import com.nlab.practice2021.core.lifecycle.lifecycleEventObserver
import com.nlab.practice2021.core.view.recyclerview.CommonRecyclerViewAdapter
import com.nlab.practice2021.core.view.recyclerview.flow.FlowItem
import com.nlab.practice2021.core.view.recyclerview.flow.FlowSupportViewHolder
import com.nlab.practice2021.core.view.recyclerview.flow.FlowSupportViewHolderLifecycle
import com.nlab.practice2021.databinding.ActivityHomeBinding
import com.nlab.practice2021.domain.detail.DomainDestinationToSystemEffect
import com.nlab.practice2021.domain.detail.DomainViewHolderMakerMapper
import com.nlab.practice2021.domain.detail.NavigateMenuRepositoryImpl
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHomeBinding.inflate(layoutInflater).also { setContentView(it.root) }
        val systemEffectFlowProvider = ViewModelProvider(this)[AndroidSystemEffectFlowProvider::class.java]
        val systemEffectReceiver = ActivitySystemEffectReceiver(
            this,
            commandFlow = systemEffectFlowProvider.effectFlow
        )

        lifecycle.addObserver(CompositeLifecycleEventObserver(
            lifecycleEventObserver(
                onCreate = { systemEffectReceiver.register() },
                onDestroy = { systemEffectReceiver.unRegister() }
            )
        ))

        binding.contentView.adapter = CommonRecyclerViewAdapter(
            this@HomeActivity,
            FlowSupportViewHolderLifecycle(
                lifecycleScope,
                DomainViewHolderMakerMapper()
            )
        )

        ViewModelProvider(this, SimpleViewModelFactory {
            HomeViewModel(
                NavigateMenuRepositoryImpl(),
                HomeItemViewModel.Factory(AndroidSystemEffect(
                    systemEffectFlowProvider.effectFlow,
                    DomainDestinationToSystemEffect()
                ))
            ) })[HomeViewModel::class.java]
            .stateFlow
            .onEach { state -> render(binding, state) }
            .launchIn(lifecycleScope)
    }

    @Suppress("UNCHECKED_CAST")
    private fun render(
        binding: ActivityHomeBinding,
        state: HomeState
    ) {
        if (state.isComplete) {
            val adapter = binding.contentView.adapter as CommonRecyclerViewAdapter<FlowSupportViewHolder, FlowItem>
            adapter.replaceItemTo(state.items)
            adapter.notifyDataSetChanged()
        }
    }

}