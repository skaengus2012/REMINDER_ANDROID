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

package com.nlab.practice2021.core.view.recyclerview.flow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.nlab.practice2021.core.view.recyclerview.ViewHolderMaker
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
class FlowItemTypeViewHolderMaker private constructor(
    private val delegate: ViewHolderMaker
) : ViewHolderMaker by delegate {

    companion object {
        operator fun <T : FlowItem, U : FlowItem.State, V : ViewBinding> invoke(
            flowItemClazz: KClass<T>,
            flowItemStateClazz: KClass<U>,
            viewBindingClazz: KClass<V>,
            inflate: (LayoutInflater, ViewGroup) -> V,
            render: (V, U) -> Unit
        ) = FlowItemTypeViewHolderMaker(
            object : ViewHolderMaker {
                override val modelClazz: KClass<out Any> = flowItemClazz
                override fun inflate(
                    layoutInflater: LayoutInflater,
                    parent: ViewGroup
                ): ViewBinding = inflate(layoutInflater, parent)
                override fun render(viewBinding: ViewBinding, model: Any) = render(
                    checkNotNull(viewBindingClazz.java.cast(viewBinding)),
                    checkNotNull(flowItemStateClazz.java.cast(model))
                )
            }
        )
    }
}