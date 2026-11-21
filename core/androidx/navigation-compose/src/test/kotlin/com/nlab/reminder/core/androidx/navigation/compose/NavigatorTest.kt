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

package com.nlab.reminder.core.androidx.navigation.compose

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

private object StartKey : NavKey
private object SecondKey : NavKey
private object ThirdKey : NavKey

/**
 * @author Thalys
 */
class NavigatorTest {
    private fun genNavigator(): Pair<NavBackStack<NavKey>, Navigator> {
        val backStack = NavBackStack<NavKey>(StartKey)
        return backStack to Navigator(backStack)
    }

    @Test
    fun `Given initial stack, When navigate to key, Then key is added to the top`() {
        val (backStack, navigator) = genNavigator()

        navigator.navigate(SecondKey)

        assertThat(backStack.toList(), equalTo(listOf<NavKey>(StartKey, SecondKey)))
    }

    @Test
    fun `Given key on top, When navigate to same key, Then stack has no duplicate`() {
        val (backStack, navigator) = genNavigator()
        navigator.navigate(SecondKey)

        navigator.navigate(SecondKey)

        assertThat(backStack.toList(), equalTo(listOf<NavKey>(StartKey, SecondKey)))
    }

    @Test
    fun `Given key in the middle, When navigate to it again, Then it moves to the top`() {
        val (backStack, navigator) = genNavigator()
        navigator.navigate(SecondKey)
        navigator.navigate(ThirdKey)

        navigator.navigate(SecondKey)

        assertThat(backStack.toList(), equalTo(listOf<NavKey>(StartKey, ThirdKey, SecondKey)))
    }

    @Test
    fun `Given multiple keys, When navigate to start key, Then start key moves to the top`() {
        val (backStack, navigator) = genNavigator()
        navigator.navigate(SecondKey)

        navigator.navigate(StartKey)

        assertThat(backStack.toList(), equalTo(listOf<NavKey>(SecondKey, StartKey)))
    }

    @Test
    fun `Given start key moved to the top, When go back, Then previous key becomes the only key`() {
        val (backStack, navigator) = genNavigator()
        navigator.navigate(SecondKey)
        navigator.navigate(StartKey)

        navigator.goBack()

        assertThat(backStack.toList(), equalTo(listOf<NavKey>(SecondKey)))
    }

    @Test
    fun `Given only start key, When navigate to start key, Then stack remains start key`() {
        val (backStack, navigator) = genNavigator()

        navigator.navigate(StartKey)

        assertThat(backStack.toList(), equalTo(listOf<NavKey>(StartKey)))
    }

    @Test
    fun `Given three keys, When go back twice, Then stack has only start key`() {
        val (backStack, navigator) = genNavigator()
        navigator.navigate(SecondKey)
        navigator.navigate(ThirdKey)

        navigator.goBack()
        assertThat(backStack.last(), equalTo(SecondKey))
        navigator.goBack()

        assertThat(backStack.toList(), equalTo(listOf<NavKey>(StartKey)))
    }

    @Test
    fun `Given only start key, When go back, Then stack remains start key`() {
        val (backStack, navigator) = genNavigator()

        navigator.goBack()

        assertThat(backStack.toList(), equalTo(listOf<NavKey>(StartKey)))
    }

    @Test
    fun `Given stack with two keys, When go back, Then returns true`() {
        val (_, navigator) = genNavigator()
        navigator.navigate(SecondKey)

        val actual = navigator.goBack()

        assertThat(actual, equalTo(true))
    }

    @Test
    fun `Given only start key, When go back, Then returns false`() {
        val (_, navigator) = genNavigator()

        val actual = navigator.goBack()

        assertThat(actual, equalTo(false))
    }
}
