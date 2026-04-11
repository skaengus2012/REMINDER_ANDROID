package com.nlab.reminder.core.component.schedulelist.content.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * A ViewHolder that renders the headline-to-content padding spacer (15 dp).
 *
 * @author Doohyun
 */
internal class HeadlinePaddingViewHolder(
    binding: ComposeViewItemBinding
) : ScheduleAdapterItemViewHolder(binding.root) {

    init {
        binding.setContent {
            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}
