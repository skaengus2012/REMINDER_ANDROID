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

package com.nlab.reminder.core.entrypoint.util

import com.nlab.reminder.core.effect.message.navigation.NavigationEffectReceiver
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class DefaultFragmentEntryPointInitTest {
    @Test
    fun testInitialize() {
        val navigationEffectReceiver: NavigationEffectReceiver = mock()
        val block: () -> Unit = mock()
        val defaultFragmentEntryPointInit = DefaultEntryPointInit(
            navigationEffectReceiver,
            EntryBlock { block() }
        )

        defaultFragmentEntryPointInit.initialize(
            navigationEffect = mock()
        )

        defaultFragmentEntryPointInit.initialize()

        verify(navigationEffectReceiver, times(1)).register(any())
        verify(block, times(2))()
    }

    @Test
    fun testGet() {
        val navigationEffectReceiver: NavigationEffectReceiver = mock()
        val entryBlock = EntryBlock { }
        val defaultFragmentEntryPointInit = DefaultEntryPointInit(
            navigationEffectReceiver,
            entryBlock
        )
        assertThat(
            defaultFragmentEntryPointInit.navigationEffectReceiver,
            equalTo(navigationEffectReceiver)
        )
        assertThat(
            defaultFragmentEntryPointInit.block,
            equalTo(entryBlock)
        )
    }
}