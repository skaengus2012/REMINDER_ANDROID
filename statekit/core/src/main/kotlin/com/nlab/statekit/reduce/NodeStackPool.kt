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

package com.nlab.statekit.reduce

/**
 * @author Doohyun
 */
sealed interface NodeStackPool {
    fun <T : Any> request(): NodeStack<T>
    fun release(acc: NodeStack<out Any>)
}

fun NodeStackPool(): NodeStackPool = ThreadLocalsNodeStackPoolProxy()

private class DefaultNodeStackPool : NodeStackPool {
    private val pool = mutableListOf<NodeStack<out Any>>()

    override fun <T : Any> request(): NodeStack<T> {
        val ret = pool.find { it.isReady.not() } ?: NodeStack<Any>().also { pool.add(it) }
        ret.ready()
        @Suppress("UNCHECKED_CAST")
        return ret as NodeStack<T>
    }

    override fun release(acc: NodeStack<out Any>) {
        acc.release()
    }
}

private class ThreadLocalsNodeStackPoolProxy : NodeStackPool {
    private val locals = ThreadLocal<DefaultNodeStackPool>()
    private val localsPool: DefaultNodeStackPool
        get() = locals.get() ?: DefaultNodeStackPool().also { locals.set(it) }

    override fun <T : Any> request(): NodeStack<T> {
        return localsPool.request()
    }

    override fun release(acc: NodeStack<out Any>) {
        localsPool.release(acc)
    }
}

// FIXME Coverage increases only if there is actual code to execute.
//  Since it is already used internally, no annotations are added.
inline fun <T : Any, R> NodeStackPool.use(block: (NodeStack<T>) -> R): R {
    val acc = request<T>()
    val ret = block.invoke(acc)
    release(acc)
    return ret
}