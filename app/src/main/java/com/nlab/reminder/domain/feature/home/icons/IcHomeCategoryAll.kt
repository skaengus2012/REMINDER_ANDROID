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

package com.nlab.reminder.domain.feature.home.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.android.designsystem.component.ReminderIcons

/**
 * @author Doohyun
 */
@Suppress("UnusedReceiverParameter")
val ReminderIcons.IcHomeCategoryAll: ImageVector
    get() {
        if (_icHomeCategoryAll != null) {
            return _icHomeCategoryAll!!
        }
        _icHomeCategoryAll = Builder(name = "IcHomeCategoryAll", defaultWidth = 19.0.dp,
                defaultHeight = 17.0.dp, viewportWidth = 19.0f, viewportHeight = 17.0f).apply {
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFFFB631)),
                    strokeLineWidth = 2.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(5.021f, 2.125f)
                lineTo(18.611f, 2.125f)
            }
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFFFB631)),
                    strokeLineWidth = 2.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(5.021f, 8.4797f)
                lineTo(18.611f, 8.4797f)
            }
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFFFB631)),
                    strokeLineWidth = 2.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(5.021f, 14.8344f)
                lineTo(18.611f, 14.8344f)
            }
            path(fill = SolidColor(Color(0xFFFFB631)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(3.397f, 0.5353f)
                horizontalLineTo(0.3889f)
                verticalLineTo(3.5433f)
                horizontalLineTo(3.397f)
                verticalLineTo(0.5353f)
                verticalLineTo(0.5353f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFB631)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(3.397f, 6.996f)
                horizontalLineTo(0.3889f)
                verticalLineTo(10.004f)
                horizontalLineTo(3.397f)
                verticalLineTo(6.996f)
                verticalLineTo(6.996f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFB631)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(3.397f, 13.4567f)
                horizontalLineTo(0.3889f)
                verticalLineTo(16.4647f)
                horizontalLineTo(3.397f)
                verticalLineTo(13.4567f)
                verticalLineTo(13.4567f)
                close()
            }
        }
        .build()
        return _icHomeCategoryAll!!
    }

private var _icHomeCategoryAll: ImageVector? = null
