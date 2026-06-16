package com.nlab.reminder.core.component.schedulelist.ui.content

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import com.nlab.reminder.core.component.bottomappbar.ui.BottomAppbarDefaults

/**
 * A ViewHolder that renders the bottom-appbar padding spacer.
 *
 * @author Doohyun
 */
internal class BottomAppbarPaddingViewHolder(
    binding: ComposeViewItemBinding
) : ScheduleAdapterItemViewHolder(binding.root) {

    init {
        binding.setContent {
            Spacer(modifier = Modifier.height(BottomAppbarDefaults.Height))
        }
    }
}
