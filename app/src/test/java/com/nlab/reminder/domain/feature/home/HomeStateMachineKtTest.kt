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
import com.nlab.reminder.test.dummyTag
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
        HomeAction.OnAllCategoryClicked,
        HomeAction.OnTagClicked(dummyTag),
        HomeAction.OnTagLongClicked(dummyTag),
        HomeAction.OnTagRenameConfirmClicked(dummyTag, renameText = ""),
        HomeAction.OnTagDeleteRequestClicked(dummyTag)
    )

    private val dummyStates: Set<HomeState> = setOf(
        HomeState.Init,
        HomeState.Loading,
        HomeState.Loaded(HomeSummary())
    )

    private fun createStateMachine(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        initState: HomeState = HomeState.Init,
        navigationEffect: SendNavigationEffect = mock(),
        getHomeSummary: GetHomeSummaryUseCase = mock { onBlocking { mock() } doReturn flow { emit(HomeSummary()) } },
        modifyTagName: ModifyTagNameUseCase = mock(),
        onHomeSummaryLoaded: (HomeSummary) -> Unit = mock(),
    ): HomeStateMachine = HomeStateMachine(
        scope, initState, navigationEffect, getHomeSummary, modifyTagName, onHomeSummaryLoaded
    )

    @Test
    fun `holds injected state when machine created`() = runTest {
        val initState = HomeState.Loaded(HomeSummary(todayNotificationCount = 1))
        val stateMachine = createStateMachine(initState = initState)
        assertThat(stateMachine.state.value, sameInstance(initState))
    }

    @Test
    fun `holds init state when machine created by factory`() {
        assertThat(
            HomeStateMachineFactory(getHomeSummary = mock(), modifyTagName = mock())
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
        val stateMachine: HomeStateMachine = createStateMachine()
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
        ): HomeStateMachine = createStateMachine(
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
        val stateMachine: HomeStateMachine = createStateMachine()
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
        val stateMachine: HomeStateMachine = createStateMachine(
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
        listOf(HomeSummary(tags = listOf(dummyTag)), HomeSummary(tags = emptyList())).forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateAction = HomeAction.OnTagClicked(dummyTag),
                expectedNavigationMessage = TagEndNavigationMessage(dummyTag)
            )
        }
    }

    @Test
    fun `Navigate tag config end when tag element long clicked without no items`() = runTest {
        listOf(HomeSummary(tags = listOf(dummyTag)), HomeSummary(tags = emptyList())).forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateAction = HomeAction.OnTagLongClicked(dummyTag),
                expectedNavigationMessage = HomeTagConfigNavigationMessage(dummyTag)
            )
        }
    }

    @Test
    fun `Navigate tag rename config when tag rename request invoked`() = runTest {
        listOf(HomeSummary(tags = listOf(dummyTag)), HomeSummary(tags = emptyList())).forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateAction = HomeAction.OnTagRenameRequestClicked(dummyTag),
                expectedNavigationMessage = HomeTagRenameNavigationMessage(dummyTag)
            )
        }
    }

    @Test
    fun `Navigate tag delete confirm when tag delete request invoked`() = runTest {
        listOf(HomeSummary(tags = listOf(dummyTag)), HomeSummary(tags = emptyList())).forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateAction = HomeAction.OnTagDeleteRequestClicked(dummyTag),
                expectedNavigationMessage = HomeTagDeleteNavigationMessage(dummyTag)
            )
        }
    }

    private suspend fun testNavigationEnd(
        initState: HomeState = HomeState.Loaded(HomeSummary(todayNotificationCount = 10)),
        navigateAction: HomeAction,
        expectedNavigationMessage: NavigationMessage,
    ) {
        val navigationEffect: SendNavigationEffect = mock()
        val stateMachine: HomeStateMachine = createStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            initState = initState,
            navigationEffect = navigationEffect
        )
        stateMachine
            .send(navigateAction)
            .join()
        verify(navigationEffect, times(1)).send(expectedNavigationMessage)
    }

    @Test
    fun `Modify tags when tag rename confirmed`() = runTest {
        val renameText = "fix"
        val modifyTagNameUseCase: ModifyTagNameUseCase = mock()
        val stateMachine: HomeStateMachine = createStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            initState = HomeState.Loaded(HomeSummary()),
            modifyTagName = modifyTagNameUseCase
        )
        stateMachine
            .send(HomeAction.OnTagRenameConfirmClicked(dummyTag, renameText))
            .join()
        verify(modifyTagNameUseCase, times(1))(dummyTag, renameText)
    }
}