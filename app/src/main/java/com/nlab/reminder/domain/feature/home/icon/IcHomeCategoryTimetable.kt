/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.home.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.android.designsystem.icon.ReminderIcons

/**
 * @author Doohyun
 */
@Suppress("UnusedReceiverParameter")
val ReminderIcons.IcHomeCategoryTimetable: ImageVector
    get() {
        if (_icHomeCategoryTimetable != null) {
            return _icHomeCategoryTimetable!!
        }
        _icHomeCategoryTimetable = Builder(name = "IcHomeCategoryTimetable", defaultWidth = 19.0.dp,
                defaultHeight = 21.0.dp, viewportWidth = 19.0f, viewportHeight = 21.0f).apply {
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFBE94F3)),
                    strokeLineWidth = 2.16164f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(1.283f, 1.9415f)
                horizontalLineToRelative(16.4339f)
                verticalLineToRelative(17.1171f)
                horizontalLineToRelative(-16.4339f)
                close()
            }
            path(fill = SolidColor(Color(0xFFBE94F3)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(15.1255f, 13.0599f)
                horizontalLineTo(11.8743f)
                verticalLineTo(16.311f)
                horizontalLineTo(15.1255f)
                verticalLineTo(13.0599f)
                verticalLineTo(13.0599f)
                close()
            }
            path(fill = SolidColor(Color(0xFFBE94F3)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.2021f, 1.9972f)
                horizontalLineToRelative(18.5955f)
                verticalLineToRelative(4.3069f)
                horizontalLineToRelative(-18.5955f)
                close()
            }
        }
        .build()
        return _icHomeCategoryTimetable!!
    }

private var _icHomeCategoryTimetable: ImageVector? = null
