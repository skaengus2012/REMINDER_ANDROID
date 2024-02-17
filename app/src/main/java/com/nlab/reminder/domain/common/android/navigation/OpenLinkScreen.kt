package com.nlab.reminder.domain.common.android.navigation

import android.content.Context
import androidx.annotation.StringRes
import com.nlab.reminder.R
import com.nlab.reminder.core.android.navigation.Screen
import com.nlab.reminder.core.kotlin.onFailure
import com.nlab.reminder.domain.common.android.widget.showToast
import timber.log.Timber

/**
 * @author Doohyun
 */
@JvmInline
value class OpenLinkScreen(val link: String) : Screen

fun Context.navigateOpenLink(
    link: String,
    @StringRes errorMessageRes: Int = R.string.open_link_default_error
) {
    applicationContext.navGraphEntryPoint()
        .contextGraph()
        .navigateAsResult(navHandle = this, OpenLinkScreen(link))
        .onFailure { Timber.e(it) }
        .onFailure { showToast(errorMessageRes) }
}