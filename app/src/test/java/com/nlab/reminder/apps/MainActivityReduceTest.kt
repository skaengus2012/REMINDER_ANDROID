package com.nlab.reminder.apps

import com.nlab.reminder.core.component.usermessage.FeedbackPriority
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.text.genUiText
import com.nlab.statekit.test.reduce.transitionScenario
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Thalys
 */
class MainActivityReduceTest {
    @Test
    fun `Given success with empty userMessage, When user message posted, Then state changed with user message`() = runTest {
        genMainActivityReduce()
            .transitionScenario()
            .initState(genMainActivityUiStateSuccess(userMessages = emptyList()))
            .action(
                MainActivityAction.UserMessagePosted(
                    UserMessage(
                        message = genUiText(),
                        priority = FeedbackPriority.LOW
                    )
                )
            )
            .expectedStateFromInput {
                initState.copy(userMessages = listOf(action.message))
            }
            .verify()
    }

    @Test
    fun `Given success with single user message, When user message shown, Then state changed empty user message`() = runTest {
        val userMessage = UserMessage(
            message = genUiText(),
            priority = FeedbackPriority.URGENT
        )
        genMainActivityReduce()
            .transitionScenario()
            .initState(genMainActivityUiStateSuccess(listOf(userMessage)))
            .action(MainActivityAction.UserMessageShown(userMessage))
            .expectedStateFromInput { initState.copy(userMessages = emptyList()) }
            .verify()
    }
}

private fun genMainActivityReduce(): MainActivityReduce = MainActivityReduce()

private fun genMainActivityUiStateSuccess(
    userMessages: List<UserMessage> = emptyList()
): MainActivityUiState.Success = MainActivityUiState.Success(
    userMessages = userMessages
)