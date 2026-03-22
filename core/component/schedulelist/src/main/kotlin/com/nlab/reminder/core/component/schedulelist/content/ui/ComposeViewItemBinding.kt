package com.nlab.reminder.core.component.schedulelist.content.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemComposeViewBinding

/**
 * @author Doohyun
 */
internal class ComposeViewItemBinding(
    layoutInflater: LayoutInflater,
    parent: ViewGroup,
) {
    private val binding = LayoutScheduleAdapterItemComposeViewBinding
        .inflate(
            layoutInflater,
            parent,
            /* attachToParent = */ false
        )
        .apply {
            composeView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
        }

    val root: View get() = binding.root

    fun setContent(content: @Composable () -> Unit) {
        binding.composeView.setContent(content = content)
    }
}