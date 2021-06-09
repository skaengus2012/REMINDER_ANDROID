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

package com.nlab.practice2021.core.view.recyclerview

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.nlab.practice2021.core.view.recyclerview.flow.EmptyViewHolderMaker
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
class MappingViewHolderMakerPolicy(
    vararg viewHolderMakers: ViewHolderMaker
) : ViewHolderMakerPolicy {
    private val emptyViewType: Int = -1
    private val emptyViewHolderMaker = EmptyViewHolderMaker()
    private val viewTypeToViewHolderMakerGroup: SparseArray<ViewHolderMaker> = SparseArray()
    private val modelClazzToViewTypeGroup = mutableMapOf<KClass<out Any>, Int>()

    init {
        viewHolderMakers.distinctBy { it.modelClazz }.withIndex().forEach { (index, dataSet) ->
            viewTypeToViewHolderMakerGroup.append(index, dataSet)
            modelClazzToViewTypeGroup[dataSet.modelClazz] = index
        }
    }

    override fun inflate(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return getViewHolderMaker(viewType).inflate(layoutInflater, parent)
    }

    override fun renderStrategy(viewType: Int): (ViewBinding, Any) -> Unit = { viewBinding, model ->
        getViewHolderMaker(viewType).render(viewBinding, model)
    }

    override fun getViewType(modelClazz: KClass<out Any>): Int {
        return modelClazzToViewTypeGroup[modelClazz] ?: emptyViewType
    }

    private fun getViewHolderMaker(
        viewType: Int
    ): ViewHolderMaker = viewTypeToViewHolderMakerGroup.get(viewType, emptyViewHolderMaker)
}
