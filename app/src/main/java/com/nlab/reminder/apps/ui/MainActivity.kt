/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.apps.ui

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nlab.reminder.core.android.widget.Toast
import com.nlab.reminder.core.androidx.compose.ui.LocalTimeZone
import com.nlab.reminder.core.component.schedulelist.ui.view.list.ScheduleListHolderActivity
import com.nlab.reminder.core.data.util.TimeZoneMonitor
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author Doohyun
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ScheduleListHolderActivity {
    @Inject
    lateinit var appToast: Toast

    @Inject
    lateinit var timeZoneMonitor: TimeZoneMonitor

    private lateinit var scheduleListDragAnchorOverlay: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = rememberPlaneatAppState(
                timeZoneMonitor = timeZoneMonitor,
                appToast = appToast
            )

            val currentTimeZone by appState.currentTimeZone.collectAsStateWithLifecycle()

            CompositionLocalProvider(
                LocalTimeZone provides currentTimeZone
            ) {
                PlaneatTheme {
                    PlaneatApp(appState = appState)
                }
            }
        }

        scheduleListDragAnchorOverlay = FrameLayout(/* context= */ this)
            .apply {
                layoutParams = FrameLayout.LayoutParams(
                    /*width = */ FrameLayout.LayoutParams.MATCH_PARENT,
                    /*height = */ FrameLayout.LayoutParams.MATCH_PARENT
                )
                clipChildren = false
                clipToPadding = false
                isClickable = false
                translationZ = SCHEDULE_LIST_DRAG_ANCHOR_OVERLAY_Z
                isVisible = false
            }
            .also { overlay -> addContentView(/*view=*/ overlay, /*params=*/ overlay.layoutParams) }
    }

    override fun requireScheduleListDragAnchorOverlay(): FrameLayout {
        return checkNotNull(scheduleListDragAnchorOverlay) {
            "ScheduleListDragAnchorOverlay is not initialized."
        }
    }

    companion object {
        private const val SCHEDULE_LIST_DRAG_ANCHOR_OVERLAY_Z = 10f
    }
}