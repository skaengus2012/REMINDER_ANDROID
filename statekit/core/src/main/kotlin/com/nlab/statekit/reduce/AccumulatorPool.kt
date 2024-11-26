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

import com.nlab.statekit.internal.Generated

/**
 * @author Doohyun
 */
interface AccumulatorPool {
    fun <T : Any> request(): Accumulator<T>
    fun release(pool: Accumulator<out Any>)
}

fun AccumulatorPool(): AccumulatorPool = ThreadLocalsAccumulatorPoolProxy()

private class DefaultAccumulatorPool : AccumulatorPool {
    private val pool = mutableListOf<Accumulator<out Any>>()

    override fun <T : Any> request(): Accumulator<T> {
        val ret = pool.find { it.isReady.not() } ?: Accumulator<Any>().also { pool.add(it) }
        ret.ready()
        @Suppress("UNCHECKED_CAST")
        return ret as Accumulator<T>
    }

    override fun release(pool: Accumulator<out Any>) {
        pool.release()
    }
}

private class ThreadLocalsAccumulatorPoolProxy : AccumulatorPool {
    private val locals = ThreadLocal<DefaultAccumulatorPool>()
    private val localsPool: DefaultAccumulatorPool
        get() = locals.get() ?: DefaultAccumulatorPool().also { locals.set(it) }

    override fun <T : Any> request(): Accumulator<T> {
        return localsPool.request()
    }

    override fun release(pool: Accumulator<out Any>) {
        localsPool.release(pool)
    }
}

// Test OK @see {com.nlab.statekit.reduce.AccumulatorPoolKtTest}
// TODO remove Generated annotation after deploy below issue
// https://github.com/jacoco/jacoco/pull/1670
@Generated
inline fun <T : Any, R> AccumulatorPool.use(block: (Accumulator<T>) -> R): R {
    val acc = request<T>()
    val ret = block.invoke(acc)
    release(acc)
    return ret
}