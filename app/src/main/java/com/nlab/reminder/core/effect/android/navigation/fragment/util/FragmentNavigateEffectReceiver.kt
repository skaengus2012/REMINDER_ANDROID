package com.nlab.reminder.core.effect.android.navigation.fragment.util

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nlab.reminder.core.effect.android.navigation.NavigationEffectReceiver
import com.nlab.reminder.core.effect.android.navigation.fragment.FragmentNavigateUseCase
import com.nlab.reminder.core.effect.android.navigation.util.NavigationEffectReceiver

/**
 * @author Doohyun
 */
class FragmentNavigateEffectReceiver(
    fragment: Fragment,
    fragmentNavigateUseCase: FragmentNavigateUseCase
) : NavigationEffectReceiver by NavigationEffectReceiver(
    lifecycleOwner = { fragment.viewLifecycleOwner },
    onNavigationMessageReceived = { message -> fragmentNavigateUseCase(fragment.findNavController(), message) }
)