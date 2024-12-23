package com.nlab.reminder.domain.common.android.widget

import android.content.Context
import com.nlab.reminder.core.android.widget.Toast
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * @author Doohyun
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {
    fun toastHandle(): Toast
}

internal fun Context.widgetEntryPoint(): WidgetEntryPoint =
    EntryPointAccessors
        .fromApplication(this, WidgetEntryPoint::class.java)