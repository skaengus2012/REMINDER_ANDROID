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
import com.nlab.reminder.domain.common.effect.message.navigation.AllEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TagEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TimetableEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TodayEndNavigationMessage
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.genTag
import com.nlab.reminder.domain.common.tag.genTagWithResource
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genLetterify
import com.nlab.reminder.test.genLong
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.emptyFlow
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
    private val dummyEvents: Set<HomeEvent> = setOf(
        HomeEvent.Fetch,
        HomeEvent.OnTodayCategoryClicked,
        HomeEvent.OnTimetableCategoryClicked,
        HomeEvent.OnAllCategoryClicked,
        HomeEvent.OnTagClicked(genTag()),
        HomeEvent.OnTagLongClicked(genTag()),
        HomeEvent.OnTagRenameConfirmClicked(genTag(), renameText = genLetterify()),
        HomeEvent.OnTagDeleteRequestClicked(genTag()),
        HomeEvent.OnTagDeleteConfirmClicked(genTag()),
        HomeEvent.OnHomeSummaryLoaded(genHomeSummary())
    )

    private val dummyStates: Set<HomeState> = setOf(
        HomeState.Init,
        HomeState.Loading,
        HomeState.Loaded(genHomeSummary())
    )

    private fun createStateMachine(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        initState: HomeState = HomeState.Init,
        navigationEffect: SendNavigationEffect = mock(),
        getHomeSummary: GetHomeSummaryUseCase = mock { onBlocking { mock() } doReturn flow { emit(genHomeSummary()) } },
        getTagUsageCount: GetTagUsageCountUseCase = mock(),
        modifyTagName: ModifyTagNameUseCase = mock(),
        deleteTag: DeleteTagUseCase = mock()
    ): HomeStateMachine = HomeStateMachine(
        scope,
        initState,
        navigationEffect,
        getHomeSummary,
        getTagUsageCount,
        modifyTagName,
        deleteTag
    )

    @Test
    fun `holds injected state when machine created`() = runTest {
        val initState = HomeState.Loaded(genHomeSummary())
        val stateMachine = createStateMachine(initState = initState)
        assertThat(stateMachine.state.value, sameInstance(initState))
    }

    @Test
    fun `keep state init even when event occurs until fetched`() = runTest {
        val initState: HomeState = HomeState.Init
        val stateMachine: HomeStateMachine = createStateMachine(initState = initState)
        dummyEvents
            .asSequence()
            .filter { it !is HomeEvent.Fetch }
            .forEach { event ->
                stateMachine.send(event).join()
                assertThat(stateMachine.state.value, sameInstance(initState))
            }

        stateMachine.send(HomeEvent.Fetch).join()
        assertThat(stateMachine.state.value, not(sameInstance(initState)))
    }

    @Test
    fun `fetch is executed when state is init`() = runTest {
        dummyStates
            .map { state ->
                state to createStateMachine(
                    initState = state,
                    getHomeSummary = mock { whenever(mock()) doReturn emptyFlow() }
                )
            }
            .forEach { (initState, stateMachine) ->
                stateMachine
                    .send(HomeEvent.Fetch)
                    .join()
                assertThat(
                    stateMachine.state.value,
                    equalTo(if (initState is HomeState.Init) HomeState.Loading else initState)
                )
            }
    }

    @Test
    fun `Notify Loaded when loaded event received after loading`() = runTest {
        val homeSummary = genHomeSummary()
        dummyStates
            .map { state -> state to createStateMachine(initState = state) }
            .forEach { (initState, stateMachine) ->
                stateMachine
                    .send(HomeEvent.OnHomeSummaryLoaded(homeSummary))
                    .join()
                assertThat(
                    stateMachine.state.value,
                    equalTo(if (initState is HomeState.Init) initState else HomeState.Loaded(homeSummary))
                )
            }
    }

    @Test
    fun `Notify Loaded when fetch is called`() = runTest {
        val homeSummary = genHomeSummary()
        val getHomeSummaryUseCase: GetHomeSummaryUseCase = mock {
            whenever(mock()) doReturn flow { emit(homeSummary) }
        }
        val stateMachine: HomeStateMachine = createStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            getHomeSummary = getHomeSummaryUseCase
        )
        stateMachine
            .send(HomeEvent.Fetch)
            .join()
        assertThat(
            stateMachine.state.value,
            equalTo(HomeState.Loaded(homeSummary))
        )
    }

    @Test
    fun `Navigate today end when today category clicked`() = runTest {
        testNavigationEnd(
            navigateEvent = HomeEvent.OnTodayCategoryClicked,
            expectedNavigationMessage = TodayEndNavigationMessage
        )
    }

    @Test
    fun `Navigate timetable end when timetable category clicked`() = runTest {
        testNavigationEnd(
            navigateEvent = HomeEvent.OnTimetableCategoryClicked,
            expectedNavigationMessage = TimetableEndNavigationMessage
        )
    }

    @Test
    fun `Navigate all end when all category clicked`() = runTest {
        testNavigationEnd(
            navigateEvent = HomeEvent.OnAllCategoryClicked,
            expectedNavigationMessage = AllEndNavigationMessage
        )
    }

    @Test
    fun `Navigate tag end when tag element clicked`() = runTest {
        val testTag: Tag = genTag()
        val testSummaries = listOf(
            genHomeSummary(tags = listOf(genTagWithResource(testTag))),
            genHomeSummary(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagClicked(testTag),
                expectedNavigationMessage = TagEndNavigationMessage(testTag)
            )
        }
    }

    @Test
    fun `Navigate tag config end when tag element long clicked`() = runTest {
        val testTag: Tag = genTag()
        val testSummaries = listOf(
            genHomeSummary(tags = listOf(genTagWithResource(testTag))),
            genHomeSummary(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagLongClicked(testTag),
                expectedNavigationMessage = HomeTagConfigNavigationMessage(testTag)
            )
        }
    }

    @Test
    fun `Navigate tag rename config when tag rename request invoked`() = runTest {
        val testTag: Tag = genTag()
        val testUsageCount = genLong()
        val testSummaries = listOf(
            genHomeSummary(tags = listOf(genTagWithResource(testTag))),
            genHomeSummary(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                getTagUsageCount = mock { whenever(mock(testTag)) doReturn testUsageCount },
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagRenameRequestClicked(testTag),
                expectedNavigationMessage = HomeTagRenameNavigationMessage(testTag, testUsageCount)
            )
        }
    }

    @Test
    fun `Navigate tag delete confirm when tag delete request invoked`() = runTest {
        val testTag: Tag = genTag()
        val testUsageCount = genLong()
        val testSummaries = listOf(
            genHomeSummary(tags = listOf(genTagWithResource(testTag))),
            genHomeSummary(tags = emptyList())
        )
        testSummaries.forEach { homeSummary ->
            testNavigationEnd(
                getTagUsageCount = mock { whenever(mock(testTag)) doReturn testUsageCount },
                initState = HomeState.Loaded(homeSummary),
                navigateEvent = HomeEvent.OnTagDeleteRequestClicked(testTag),
                expectedNavigationMessage = HomeTagDeleteNavigationMessage(testTag, testUsageCount)
            )
        }
    }

    private suspend fun testNavigationEnd(
        getTagUsageCount: GetTagUsageCountUseCase = mock(),
        initState: HomeState = HomeState.Loaded(genHomeSummary()),
        navigateEvent: HomeEvent,
        expectedNavigationMessage: NavigationMessage,
    ) {
        val navigationEffect: SendNavigationEffect = mock()
        val stateMachine: HomeStateMachine = createStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            initState = initState,
            navigationEffect = navigationEffect,
            getTagUsageCount = getTagUsageCount
        )
        stateMachine
            .send(navigateEvent)
            .join()
        verify(navigationEffect, times(1)).send(expectedNavigationMessage)
    }

    @Test
    fun `Modify tags when tag rename confirmed`() = runTest {
        val renameText = genBothify()
        val testTag: Tag = genTag()
        val modifyTagNameUseCase: ModifyTagNameUseCase = mock()
        val stateMachine: HomeStateMachine = createStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            initState = HomeState.Loaded(genHomeSummary()),
            modifyTagName = modifyTagNameUseCase
        )
        stateMachine
            .send(HomeEvent.OnTagRenameConfirmClicked(testTag, renameText))
            .join()
        verify(modifyTagNameUseCase, times(1))(testTag, renameText)
    }

    @Test
    fun `Delete tag when delete confirm invoked`() = runTest {
        val deleteTagUseCase: DeleteTagUseCase = mock()
        val testTag: Tag = genTag()
        val stateMachine: HomeStateMachine = createStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            initState = HomeState.Loaded(genHomeSummary()),
            deleteTag = deleteTagUseCase
        )
        stateMachine
            .send(HomeEvent.OnTagDeleteConfirmClicked(testTag))
            .join()
        verify(deleteTagUseCase, times(1))(testTag)
    }
}