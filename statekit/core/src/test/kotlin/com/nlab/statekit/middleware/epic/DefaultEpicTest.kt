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

package com.nlab.statekit.middleware.epic

import com.nlab.statekit.TestAction
import com.nlab.testkit.faker.genIntGreaterThanZero
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class DefaultEpicTest {
    @Test
    fun testInvoke() {
        val expectedSources: List<EpicSource<TestAction>> = List(genIntGreaterThanZero()) {
            EpicSource(
                flowOf(TestAction.genAction()),
                SubscriptionStrategy.WhileStateUsed
            )
        }
        val epic = DefaultEpic(expectedSources)
        val actualSources = epic()
        assertThat(actualSources, equalTo(expectedSources))
    }
}