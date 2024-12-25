package com.nlab.reminder.domain.common.android.widget

/**
 * @author Doohyun
 */
/**
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {
    fun toastHandle(): Toast
}

internal fun Context.widgetEntryPoint(): WidgetEntryPoint =
    EntryPointAccessors
        .fromApplication(this, WidgetEntryPoint::class.java)*/