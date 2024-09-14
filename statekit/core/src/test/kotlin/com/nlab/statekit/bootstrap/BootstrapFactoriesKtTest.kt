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
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.instanceOf
import org.junit.Test

/**
 * @author Doohyun
 */
class BootstrapFactoriesKtTest {
    @Test
    fun `When called EmptyBootstrap, Then return singleton empty bootstrap`() {
        assertThat(EmptyBootstrap(), sameInstance(EmptyBootstrap))
    }

    @Test
    fun `Given action, When constructed with single action, Then return single bootstrap`() {
        val action = TestAction.genAction()
        val bootstrap = Bootstrap(action)
        assertThat(bootstrap, instanceOf(SingleBootstrap::class))
    }

    @Test
    fun `Given bootstrap list, When called Bootstrap, Then return valid bootstrap`() {
        // empty case
        assertThat(Bootstrap<TestAction>(emptyList()), instanceOf(EmptyBootstrap::class))

        // single case
        val singleBootstrap = TestBootstrap()
        assertThat(Bootstrap(listOf(singleBootstrap)), sameInstance(singleBootstrap))

        // multiple cases
        val multipleBootstraps = List(genInt(min = 2, max = 5)) { TestBootstrap() }
        assertThat((Bootstrap(multipleBootstraps) as MergeBootstrap).bootstraps, sameInstance(multipleBootstraps))
    }

    @Test
    fun `Given bootstraps, When constructed with multiple, Then return merge bootstrap`() {
        val boot1 = TestBootstrap()
        val boot2 = TestBootstrap()
        val boot3 = TestBootstrap()
        val bootstrap = Bootstrap(boot1, boot2, boot3)
        assertThat((bootstrap as MergeBootstrap).bootstraps, equalTo(listOf(boot1, boot2, boot3)))
    }

    @Test
    fun `Given multiple bootstraps, When plus, Then return merge bootstrap`() {
        fun getFlattenBootstraps(
            firstBootstrap: Bootstrap<TestAction>,
            secondBootstrap: Bootstrap<TestAction>
        ): List<Bootstrap<TestAction>> {
            val newBootstrap = firstBootstrap + secondBootstrap
            return (newBootstrap as MergeBootstrap).bootstraps
        }

        val firstBootstrap = TestBootstrap()
        val secondBootstrap = TestBootstrap()
        val thirdBootstrap = TestBootstrap()
        val fourthBootstrap = TestBootstrap()

        // merge & merge
        val mergeAndMergeFlattenBootstraps = getFlattenBootstraps(
            Bootstrap(firstBootstrap, secondBootstrap),
            Bootstrap(thirdBootstrap, fourthBootstrap)
        )
        assertThat(
            mergeAndMergeFlattenBootstraps,
            equalTo(listOf(firstBootstrap, secondBootstrap, thirdBootstrap, fourthBootstrap))
        )

        // merge & single
        val mergeAndSingleFlattenBootstraps = getFlattenBootstraps(
            Bootstrap(firstBootstrap, secondBootstrap),
            thirdBootstrap
        )
        assertThat(
            mergeAndSingleFlattenBootstraps,
            equalTo(listOf(firstBootstrap, secondBootstrap, thirdBootstrap))
        )

        // single & merge
        val singleAndMergeFlattenBootstraps = getFlattenBootstraps(
            firstBootstrap,
            Bootstrap(secondBootstrap, thirdBootstrap)
        )
        assertThat(
            singleAndMergeFlattenBootstraps,
            equalTo(listOf(firstBootstrap, secondBootstrap, thirdBootstrap))
        )

        // single & single
        val singleAndSingleFlattenBoot = getFlattenBootstraps(
            firstBootstrap,
            secondBootstrap
        )
        assertThat(
            singleAndSingleFlattenBoot,
            equalTo(listOf(firstBootstrap, secondBootstrap))
        )
    }
}