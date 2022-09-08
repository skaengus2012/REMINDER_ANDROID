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

package com.nlab.reminder.domain.common.effect.message.navigation.util

import com.nlab.reminder.core.effect.message.navigation.SendNavigationEffect
import com.nlab.reminder.domain.common.effect.message.navigation.AllEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TagEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TimetableEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TodayEndNavigationMessage
import com.nlab.reminder.domain.common.tag.Tag

/**
 * @author Doohyun
 */
suspend fun SendNavigationEffect.navigateAllEnd() = post(AllEndNavigationMessage)
suspend fun SendNavigationEffect.navigateTagEnd(tag: Tag) = post(TagEndNavigationMessage(tag))
suspend fun SendNavigationEffect.navigateTimetableEnd() = post(TimetableEndNavigationMessage)
suspend fun SendNavigationEffect.navigateTodayEnd() = post(TodayEndNavigationMessage)