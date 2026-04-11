package com.nlab.reminder.core.component.bottomappbar.ui

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember

/**
 * @author Doohyun
 */
@Stable
interface BottomAppbarState {
    @get:FloatRange(from = 0.0, to = 1.0)
    val backgroundAlpha: Float
}

private fun requireValidBackgroundAlpha(alpha: Float) {
    require(alpha in 0f..1f) {
        "backgroundAlpha must be between 0 and 1, but was $alpha"
    }
}

/**
 * A mutable implementation of [BottomAppbarState].
 */
class MutableBottomAppbarState(
    @FloatRange(from = 0.0, to = 1.0) initialBackgroundAlpha: Float = 0f
) : BottomAppbarState {
    private val mutableAlphaState: MutableFloatState = run {
        requireValidBackgroundAlpha(initialBackgroundAlpha)
        mutableFloatStateOf(initialBackgroundAlpha)
    }

    override var backgroundAlpha: Float
        get() = mutableAlphaState.floatValue
        set(value) {
            requireValidBackgroundAlpha(value)
            mutableAlphaState.floatValue = value
        }
}

private class DelegatedBottomAppbarState(
    private val backgroundAlphaState: State<Float>
) : BottomAppbarState {
    override val backgroundAlpha: Float
        get() {
            val alpha = backgroundAlphaState.value
            requireValidBackgroundAlpha(alpha)
            return alpha
        }
}

/**
 * Creates and remembers a [BottomAppbarState] that delegates to the given [State].
 */
@Composable
fun rememberDelegatedBottomAppbarState(
    backgroundAlphaState: State<Float>
): BottomAppbarState = remember(backgroundAlphaState) {
    DelegatedBottomAppbarState(backgroundAlphaState)
}