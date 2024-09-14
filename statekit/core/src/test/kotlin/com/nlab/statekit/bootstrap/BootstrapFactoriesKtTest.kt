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

package com.nlab.statekit.bootstrap

import com.nlab.statekit.TestAction
import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.instanceOf
import org.junit.Test

/**
 * @author Doohyun
 */
class BootstrapFactoriesKtTest {
    @Test
    fun `Given action, When constructed with single action, Then return single bootstrap`() {
        val action = TestAction.genAction()
        val bootstrap = Bootstrap(action)
        assertThat(bootstrap, instanceOf(SingleBootstrap::class))
    }

    @Test
    fun `Given bootstrap list with size 0, When constructed with list, Then return empty bootstrap`() {
        val boots = emptyList<Bootstrap<TestAction>>()
        val bootstrap = Bootstrap(boots)
        assertThat(bootstrap, instanceOf(EmptyBootstrap::class))
    }

    @Test
    fun `Given bootstrap list with size 1, When constructed with list, Then return single bootstrap`() {
        val boots = listOf(Bootstrap(TestAction.genAction()))
        val bootstrap = Bootstrap(boots)
        assertThat(bootstrap, instanceOf(SingleBootstrap::class))
    }

    @Test
    fun `Given bootstrap list with size greater than 2, When constructed with list, Then return merge bootstrap`() {
        val boots = List(genInt(min = 2, max = 10)) { Bootstrap(TestAction.genAction()) }
        val bootstrap = Bootstrap(boots)
        assertThat(bootstrap, instanceOf(MergeBootstrap::class))
    }

    @Test
    fun `Given bootstraps, When constructed with multiple, Then return merge bootstrap`() {
        val boot1 = Bootstrap(TestAction.Action1)
        val boot2 = Bootstrap(TestAction.Action2)
        val boot3 = Bootstrap(TestAction.Action3)
        val bootstrap = Bootstrap(boot1, boot2, boot3)
        assertThat(bootstrap, instanceOf(MergeBootstrap::class))
    }

    @Test
    fun testEmptyBootstrap() {
        assertThat(emptyBootstrap(), equalTo(EmptyBootstrap))
    }
}