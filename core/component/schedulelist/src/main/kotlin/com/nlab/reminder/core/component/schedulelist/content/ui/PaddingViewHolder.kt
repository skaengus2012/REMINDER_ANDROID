package com.nlab.reminder.core.component.schedulelist.content.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @author Doohyun
 */
internal class PaddingViewHolder(
    private val binding: ComposeViewItemBinding
) : ScheduleAdapterItemViewHolder(binding.root) {
    fun bind(item: ScheduleListItem.Padding) {
        binding.setContent {
            PlaneatTheme {
                Spacer(
                    modifier = Modifier.height(
                        when (item.type) {
                            ScheduleListItemPaddingType.Headline -> 15.dp
                            // TODO encapsulation 50.dp
                            ScheduleListItemPaddingType.Footer -> 50.dp
                        }
                    )
                )
            }
        }
    }
}