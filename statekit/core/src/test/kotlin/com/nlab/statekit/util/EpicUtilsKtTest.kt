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

package com.nlab.statekit.util

import com.nlab.statekit.TestAction
import com.nlab.testkit.genIntGreaterThanZero
import com.nlab.statekit.middleware.epic.EpicSource
import com.nlab.statekit.middleware.epic.SubscriptionStrategy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
internal class EpicUtilsKtTest {
    @Test
    fun testBuildEpic() {
        val expectedSources: List<EpicSource<TestAction>> = List(genIntGreaterThanZero()) {
            EpicSource(
                flowOf(TestAction.genAction()),
                SubscriptionStrategy.WhileStateUsed
            )
        }
        val epic = buildEpic(*expectedSources.toTypedArray())
        val actualSources = epic()
        assertThat(actualSources, equalTo(expectedSources))
    }

    @Test
    fun testBuildDslEpic() = runTest {
        val expectedAction = TestAction.genAction()
        val epic = buildDslEpic {
            whileStateUsed {
                flowOf(expectedAction)
            }
        }
        val epicSource = epic().first()

        assertThat(epicSource.subscriptionStrategy, equalTo(SubscriptionStrategy.WhileStateUsed))
        assertThat(epicSource.stream.first(), equalTo(expectedAction))
    }
}