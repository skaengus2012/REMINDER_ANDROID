/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

@file:Suppress("FunctionName")

package com.nlab.reminder.core.state2.util

import com.nlab.reminder.core.state2.*
import com.nlab.reminder.core.state2.middleware.enhancer.ActionDispatcher
import com.nlab.reminder.core.state2.middleware.enhancer.Enhancer
import com.nlab.reminder.core.state2.middleware.epic.Epic
import com.nlab.reminder.core.state2.middleware.epic.EpicSource
import com.nlab.reminder.core.state2.store.DefaultStoreFactory
import kotlinx.coroutines.CoroutineScope

/**
 * @author thalys
 */
/**
private val defaultStoreFactory = DefaultStoreFactory()

private class EmptyReducer<A : Action, S : State> : Reducer<A, S> {
    override fun invoke(updateSource: UpdateSource<A, S>): S = updateSource.before
}

private class EmptyEnhancer<A : Action, S : State> : Enhancer<A, S> {
    override suspend fun invoke(p1: ActionDispatcher<A>, p2: UpdateSource<A, S>) = Unit
}

private class EmptyEpic<A : Action> : Epic<A> {
    override fun invoke(): List<EpicSource<A>> = emptyList()
}

fun <A : Action, S : State> createStore(
    coroutineScope: CoroutineScope,
    initState: S,
    reducer: Reducer<A, S> = EmptyReducer(),
    enhancer: Enhancer<A, S> = EmptyEnhancer(),
    epic: Epic<A> = EmptyEpic()
): Store<A, S> = defaultStoreFactory.createStore(
    coroutineScope,
    initState,
    reducer,
    enhancer,
    epic
)*/