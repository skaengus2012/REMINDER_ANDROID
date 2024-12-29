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

package com.nlab.reminder.feature.all.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.androidx.fragment.compose.ComposableFragment
import com.nlab.reminder.core.androidx.fragment.compose.ComposableInject
import com.nlab.reminder.core.androidx.fragment.viewLifecycleScope
import com.nlab.reminder.core.androix.recyclerview.verticalScrollRange
import com.nlab.reminder.feature.all.databinding.FragmentAllBinding
import com.nlab.reminder.feature.all.databinding.LayoutTestBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
internal class AllFragment : ComposableFragment() {
    private var _binding: FragmentAllBinding? = null
    private val binding: FragmentAllBinding get() = checkNotNull(_binding)

    @ComposableInject
    lateinit var fragmentStateBridge: AllFragmentStateBridge

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        FragmentAllBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onComposed() {
        binding.recyclerviewSchedule
            .verticalScrollRange()
            .distinctUntilChanged()
            .onEach { fragmentStateBridge.verticalScrollRange = it }
            .launchIn(viewLifecycleScope)

        val adapter = TestAdapter()
        binding.recyclerviewSchedule.adapter = adapter

        viewLifecycleScope.launch {
            /**
            delay(2000)
            adapter.a.add(1)
            adapter.notifyDataSetChanged()*/
            delay(2000)
            adapter.a.addAll((1..1000).toList())
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TestAdapter : RecyclerView.Adapter<TestViewHolder>() {
    var a = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder =
        TestViewHolder(LayoutTestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.onBind(a[position])
    }

    override fun getItemCount(): Int = a.size
}

class TestViewHolder(private val binding: LayoutTestBinding) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(poisition: Int) {
        binding.editText.setText(poisition.toString())
    }
}