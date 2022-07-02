package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.effect.android.navigation.NavigationMessage
import com.nlab.reminder.core.effect.android.navigation.SendNavigationEffect
import com.nlab.reminder.core.util.annotation.test.Generated
import com.nlab.reminder.domain.common.tag.Tag

/**
 * @author Doohyun
 */
@Generated
data class HomeTagConfigNavigation(val tag: Tag) : NavigationMessage

suspend fun SendNavigationEffect.navigateHomeTagConfig(tag: Tag) = send(HomeTagConfigNavigation(tag))