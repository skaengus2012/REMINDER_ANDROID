package com.nlab.reminder.domain.common.android.widget

import android.content.Context
import androidx.annotation.StringRes

/**
 * @author Doohyun
 */
fun Context.showToast(@StringRes stringResource: Int) {
    widgetEntryPoint()
        .toastHandle()
        .showToast(stringResource)
}