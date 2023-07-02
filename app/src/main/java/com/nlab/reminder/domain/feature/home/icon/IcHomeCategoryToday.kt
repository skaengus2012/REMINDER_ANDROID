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
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
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
val ReminderIcons.IcHomeCategoryToday: ImageVector
    get() {
        if (_icHomeCategoryToday != null) {
            return _icHomeCategoryToday!!
        }
        _icHomeCategoryToday = Builder(name = "IcHomeCategoryToday", defaultWidth = 23.0.dp,
                defaultHeight = 23.0.dp, viewportWidth = 23.0f, viewportHeight = 23.0f).apply {
            path(fill = SolidColor(Color(0xFF1CCF98)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(15.3002f, 12.4713f)
                horizontalLineTo(11.181f)
                curveTo(10.9287f, 12.4713f, 10.6871f, 12.3711f, 10.5089f, 12.1929f)
                curveTo(10.3306f, 12.0147f, 10.2305f, 11.7725f, 10.2305f, 11.5207f)
                verticalLineTo(6.7678f)
                horizontalLineTo(12.1316f)
                verticalLineTo(10.5701f)
                horizontalLineTo(15.3002f)
                verticalLineTo(12.4713f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1CCF98)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(18.8541f, 17.8094f)
                moveToRelative(-2.695f, 0.0f)
                arcToRelative(2.695f, 2.695f, 0.0f, true, true, 5.3899f, 0.0f)
                arcToRelative(2.695f, 2.695f, 0.0f, true, true, -5.3899f, 0.0f)
            }
            path(fill = SolidColor(Color(0xFF1CCF98)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(14.6906f, 19.5685f)
                curveTo(13.703f, 19.9594f, 12.6265f, 20.1742f, 11.4998f, 20.1742f)
                curveTo(6.7092f, 20.1742f, 2.8257f, 16.2907f, 2.8257f, 11.5001f)
                curveTo(2.8257f, 6.7095f, 6.7092f, 2.8259f, 11.4998f, 2.8259f)
                curveTo(16.2904f, 2.8259f, 20.174f, 6.7095f, 20.174f, 11.5001f)
                curveTo(20.174f, 12.185f, 20.0946f, 12.8515f, 19.9445f, 13.4907f)
                curveTo(20.6271f, 13.6606f, 21.248f, 13.9863f, 21.7674f, 14.4279f)
                curveTo(22.0322f, 13.4975f, 22.174f, 12.5154f, 22.174f, 11.5001f)
                curveTo(22.174f, 5.6049f, 17.395f, 0.8259f, 11.4998f, 0.8259f)
                curveTo(5.6047f, 0.8259f, 0.8257f, 5.6049f, 0.8257f, 11.5001f)
                curveTo(0.8257f, 17.3952f, 5.6047f, 22.1742f, 11.4998f, 22.1742f)
                curveTo(13.0614f, 22.1742f, 14.5447f, 21.8389f, 15.8816f, 21.2363f)
                curveTo(15.3664f, 20.7827f, 14.9558f, 20.2132f, 14.6906f, 19.5685f)
                close()
            }
        }
        .build()
        return _icHomeCategoryToday!!
    }

private var _icHomeCategoryToday: ImageVector? = null
