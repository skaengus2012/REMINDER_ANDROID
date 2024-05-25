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

@file:Suppress("unused")

package com.nlab.reminder.internal.common.android.startup.init

import android.content.Context
import androidx.startup.Initializer
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.nlab.reminder.internal.common.android.startup.EmptyDependencies

/**
 * @author Doohyun
 */
internal class CoilInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Coil.setImageLoader {
            ImageLoader.Builder(context)
                .memoryCache {
                    MemoryCache.Builder(context)
                        .maxSizePercent(percent = 0.5)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.cacheDir.resolve("image_cache"))
                        .maxSizePercent(percent = 0.05)
                        .build()
                }
                .crossfade(enable = true)
                .allowRgb565(enable = true)
                .build()
        }
    }

    override fun dependencies() = EmptyDependencies()
}