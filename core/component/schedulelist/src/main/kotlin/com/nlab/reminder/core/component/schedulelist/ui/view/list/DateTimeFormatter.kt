/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.ui.view.list

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter as JvmDateTimeFormatter

/**
 * @author Thalys
 */
internal class DateTimeFormatter(
    private val jvmDateTimeFormatter: JvmDateTimeFormatter
) {
    private val cache = hashMapOf<Any, String>()

    fun format(localDate: LocalDate): String {
        return cache.getOrPut(localDate) { jvmDateTimeFormatter.format(localDate.toJavaLocalDate()) }
    }

    fun format(localDateTime: LocalDateTime): String {
        return cache.getOrPut(localDateTime) { jvmDateTimeFormatter.format(localDateTime.toJavaLocalDateTime()) }
    }
}