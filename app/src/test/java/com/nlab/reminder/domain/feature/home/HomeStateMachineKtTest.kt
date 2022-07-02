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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.effect.message.navigation.NavigationMessage
import com.nlab.reminder.core.effect.message.navigation.SendNavigationEffect
import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.domain.common.effect.message.navigation.AllEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TagEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TimetableEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TodayEndNavigationMessage
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.TagStyleResource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeStateMachineKtTest {
    private val dummyActions: Set<HomeAction> = setOf(
        HomeAction.Fetch,
        HomeAction.OnTodayCategoryClicked,
        HomeAction.OnTimetableCategoryClicked,
        HomeAction.OnAllCategoryClicked
    )

    private val dummyStates: Set<HomeState> = setOf(
        HomeState.Init,
        HomeState.Loading,
        HomeState.Loaded(HomeSummary())
    )

    private fun createHomeStateMachineFactory(
        getHomeSummary: GetHomeSummaryUseCase = mock(),
        getTagUsageCount: GetTagUsageCountUseCase = mock(),
        initState: HomeState = HomeState.Init
    ): HomeStateMachineFactory = HomeStateMachineFactory(getHomeSummary, getTagUsageCount, initState)

    private fun createHomeStateMachine(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        initState: HomeState = HomeState.Init,
        navigationEffect: SendNavigationEffect = mock(),
        getHomeSummary: GetHomeSummaryUseCase = mock { onBlocking { mock() } doReturn flow { emit(HomeSummary()) } },
        getTagUsageCount: GetTagUsageCountUseCase = mock(),
        onHomeSummaryLoaded: (HomeSummary) -> Unit = mock(),
    ): HomeStateMachine =
        createHomeStateMachineFactory(getHomeSummary, getTagUsageCount, initState)
            .create(scope, navigationEffect, onHomeSummaryLoaded)

    @Test
    fun `holds injected state when machine created`() = runTest {
        val initState = HomeState.Loaded(HomeSummary(todayNotificationCount = 1))
        val stateMachine = createHomeStateMachine(initState = initState)
        assertThat(stateMachine.state.value, sameInstance(initState))
    }

    @Test
    fun `holds init state when machine created`() {
        assertThat(
            createHomeStateMachineFactory()
                .create(
                    scope = CoroutineScope(Dispatchers.Default),
                    navigationEffect = mock(),
                    onHomeSummaryLoaded = mock()
                )
                .state
                .value,
            equalTo(HomeState.Init)
        )
    }

    @Test
    fun `keep state init even when action occurs until fetched`() = runTest {
        val stateMachine: HomeStateMachine = createHomeStateMachine()
        val initState: HomeState = HomeState.Init
        dummyActions
            .asSequence()
            .filter { it !is HomeAction.Fetch }
            .forEach { action ->
                stateMachine.send(action).join()
                assertThat(stateMachine.state.value, sameInstance(initState))
            }

        stateMachine.send(HomeAction.Fetch).join()
        assertThat(stateMachine.state.value, not(sameInstance(initState)))
    }

    @Test
    fun `fetch is executed when state is init`() = runTest {
        fun createHomeStateMachineWithEmptySummary(
            initState: HomeState
        ): HomeStateMachine = createHomeStateMachine(
            getHomeSummary = mock { onBlocking { invoke() } doReturn flow {} },
            initState = initState
        )

        dummyStates
            .asSequence()
            .filter { it !is HomeState.Init }
            .map { state -> createHomeStateMachineWithEmptySummary(state) }
            .forEach { machine ->
                val curState: HomeState = machine.state.value
                machine.send(HomeAction.Fetch).join()
                assertThat(machine.state.value, sameInstance(curState))
            }

        val stateMachine: StateMachine<HomeAction, HomeState> = createHomeStateMachineWithEmptySummary(HomeState.Init)
        stateMachine.send(HomeAction.Fetch).join()
        assertThat(stateMachine.state.value, equalTo(HomeState.Loading))
    }

    @Test
    fun `Notify Loaded when loaded action received`() = runTest {
        val homeSummary = HomeSummary(allNotificationCount = 10)
        val stateMachine: HomeStateMachine = createHomeStateMachine()
        stateMachine
            .send(HomeAction.HomeSummaryLoaded(homeSummary))
            .join()
        assertThat(
            stateMachine.state.value,
            equalTo(HomeState.Loaded(homeSummary))
        )
    }

    @Test
    fun `Notify Loaded when fetch is called`() = runTest {
        val getHomeSummaryUseCase: GetHomeSummaryUseCase = mock {
            whenever(mock()) doReturn flow {
                repeat(10) { number ->
                    emit(HomeSummary(todayNotificationCount = number.toLong()))
                }
            }
        }
        val collectedStates: MutableList<HomeSummary> = mutableListOf()
        val onHomeSummaryLoaded: (HomeSummary) -> Unit = { collectedStates += it }
        val stateMachine: HomeStateMachine = createHomeStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            getHomeSummary = getHomeSummaryUseCase,
            onHomeSummaryLoaded = onHomeSummaryLoaded
        )
        stateMachine
            .send(HomeAction.Fetch)
            .join()
        assertThat(
            collectedStates,
            equalTo((0..9).map { HomeSummary(todayNotificationCount = it.toLong()) })
        )
    }

    @Test
    fun `Navigate today end when today category clicked`() = runTest {
        testNavigationEnd(
            navigateAction = HomeAction.OnTodayCategoryClicked,
            expectedNavigationMessage = TodayEndNavigationMessage
        )
    }

    @Test
    fun `Navigate timetable end when timetable category clicked`() = runTest {
        testNavigationEnd(
            navigateAction = HomeAction.OnTimetableCategoryClicked,
            expectedNavigationMessage = TimetableEndNavigationMessage
        )
    }

    @Test
    fun `Navigate all end when all category clicked`() = runTest {
        testNavigationEnd(
            navigateAction = HomeAction.OnAllCategoryClicked,
            expectedNavigationMessage = AllEndNavigationMessage
        )
    }

    @Test
    fun `Navigate tag end when tag element clicked`() = runTest {
        val tag = Tag(text = "Test", TagStyleResource.TYPE3)
        listOf(HomeSummary(tags = listOf(tag)), HomeSummary(tags = emptyList())).forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateAction = HomeAction.OnTagClicked(tag),
                expectedNavigationMessage = TagEndNavigationMessage(tag)
            )
        }
    }

    @Test
    fun `Navigate tag config end when tag element long clicked without no items`() = runTest {
        val tag = Tag(text = "Test", TagStyleResource.TYPE3)
        listOf(HomeSummary(tags = listOf(tag)), HomeSummary(tags = emptyList())).forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateAction = HomeAction.OnTagLongClicked(tag),
                expectedNavigationMessage = HomeTagConfigNavigationMessage(tag)
            )
        }
    }

    @Test
    fun `Navigate tag rename config when tag rename request invoked`() = runTest {
        val tag = Tag(text = "Test", TagStyleResource.TYPE3)
        val fakeUsageCount = 10
        val getTagUsageCount: GetTagUsageCountUseCase = mock { whenever(mock(tag)) doReturn fakeUsageCount }

        listOf(HomeSummary(tags = listOf(tag)), HomeSummary(tags = emptyList())).forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                getTagUsageCount = getTagUsageCount,
                navigateAction = HomeAction.OnTagRenameRequestClicked(tag),
                expectedNavigationMessage = HomeTagRenameNavigationMessage(tag, fakeUsageCount)
            )
        }
        verify(getTagUsageCount, times(2))(tag)
    }

    @Test
    fun `Navigate tag delete confirm when tag delete request invoked`() = runTest {
        val tag = Tag(text = "Test", TagStyleResource.TYPE3)
        listOf(HomeSummary(tags = listOf(tag)), HomeSummary(tags = emptyList())).forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateAction = HomeAction.OnTagDeleteRequestClicked(tag),
                expectedNavigationMessage = HomeTagDeleteConfirmNavigationMessage(tag)
            )
        }
    }

    private suspend fun testNavigationEnd(
        initState: HomeState = HomeState.Loaded(HomeSummary(todayNotificationCount = 10)),
        getTagUsageCount: GetTagUsageCountUseCase = mock(),
        navigateAction: HomeAction,
        expectedNavigationMessage: NavigationMessage,
    ) {
        val navigationEffect: SendNavigationEffect = mock()
        val stateMachine: HomeStateMachine = createHomeStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            initState = initState,
            getTagUsageCount = getTagUsageCount,
            navigationEffect = navigationEffect
        )
        stateMachine
            .send(navigateAction)
            .join()
        verify(navigationEffect, times(1)).send(expectedNavigationMessage)
    }
}