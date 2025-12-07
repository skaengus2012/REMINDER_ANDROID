/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * @author Doohyun
 */
@ExcludeFromGeneratedTestReport
@HiltAndroidApp
class PlaneatApplication : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    override fun newImageLoader(): ImageLoader {
        return imageLoader.get()
    }
}