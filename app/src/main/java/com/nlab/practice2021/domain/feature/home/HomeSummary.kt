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

package com.nlab.practice2021.domain.feature.home

import com.nlab.practice2021.core.util.annotation.test.Generated

/**
 * @author Doohyun
 */
@Generated
data class HomeSummary(
    val todayNotificationCount: Long = 0,
    val scheduledNotificationCount: Long = 0,
    val allNotificationCount: Long = 0,
    val flaggedNotificationCount: Long = 0
)