/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.statekit.foundation.store

import com.nlab.statekit.bootstrap.Bootstrap
import com.nlab.statekit.bootstrap.EmptyBootstrap
import com.nlab.statekit.foundation.plugins.GlobalEffect
import com.nlab.statekit.foundation.plugins.GlobalSuspendEffect
import com.nlab.statekit.reduce.Effect
import com.nlab.statekit.reduce.EmptyReduce
import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.store.Store
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import com.nlab.statekit.store.createStore as createStoreOrigin

/**
 * @author Thalys
 */
fun <A : Any, S : Any> StoreMaterialScope.createStore(
    initState: S,
    reduce: Reduce<A, S> = EmptyReduce(),
    bootstrap: Bootstrap<A> = EmptyBootstrap()
): Store<A, S> = createStoreOrigin(
    coroutineScope = createStoreMaterialCoroutineScope(
        coroutineScope = baseCoroutineScope,
        preferredCoroutineDispatcher = configuration.preferredCoroutineDispatcher,
        defaultCoroutineExceptionHandler = configuration.defaultCoroutineExceptionHandler
    ),
    initState = initState,
    reduce = createStoreMaterialReduce(
        reduce = reduce,
        defaultEffects = configuration.defaultEffects,
        defaultSuspendEffects = configuration.defaultSuspendEffects
    ),
    bootstrap = bootstrap
)

private fun createStoreMaterialCoroutineScope(
    coroutineScope: CoroutineScope,
    preferredCoroutineDispatcher: CoroutineDispatcher?,
    defaultCoroutineExceptionHandler: CoroutineExceptionHandler?
): CoroutineScope {
    var ret = coroutineScope
    preferredCoroutineDispatcher?.let { coroutineDispatcher ->
        ret += coroutineDispatcher
    }

    val localHandler = coroutineScope.coroutineContext[CoroutineExceptionHandler]
    composeCoroutineExceptionHandler(
        firstHandler = localHandler,
        secondHandler = defaultCoroutineExceptionHandler
    )?.let { coroutineExceptionHandler ->
        if (coroutineExceptionHandler !== localHandler) {
            ret += coroutineExceptionHandler
        }
    }

    return ret
}

private fun composeCoroutineExceptionHandler(
    firstHandler: CoroutineExceptionHandler?,
    secondHandler: CoroutineExceptionHandler?,
): CoroutineExceptionHandler? = when {
    firstHandler == null -> secondHandler
    secondHandler == null -> firstHandler
    else -> CoroutineExceptionHandler { context, throwable ->
        firstHandler.handleException(context, throwable)
        secondHandler.handleException(context, throwable)
    }
}

private fun <A : Any, S : Any> createStoreMaterialReduce(
    reduce: Reduce<A, S>,
    defaultEffects: Collection<GlobalEffect>,
    defaultSuspendEffects: Collection<GlobalSuspendEffect>
): Reduce<A, S> {
    if (defaultEffects.isEmpty() && defaultSuspendEffects.isEmpty()) return reduce

    val additionalEffects: List<Effect<A, S>> = buildList {
        this += defaultEffects.map { defaultEffect ->
            Effect.Node { action, current -> defaultEffect.invoke(action, current) }
        }
        this += defaultSuspendEffects.map { defaultEffect ->
            Effect.SuspendNode { action, current, _ -> defaultEffect.invoke(action, current) }
        }
    }
    val newEffect = reduce.effect
        ?.let { originEffect -> Effect.Composite(head = originEffect, tails = additionalEffects) }
        ?: with(additionalEffects) {
            val head = additionalEffects.first()
            if (size == 1) head
            else Effect.Composite(
                head = head,
                tails = additionalEffects.drop(1)
            )
        }
    return Reduce(
        transition = reduce.transition,
        effect = newEffect
    )
}