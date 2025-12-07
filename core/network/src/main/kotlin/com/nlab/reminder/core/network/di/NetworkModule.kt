/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.network.di

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader = ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(percent = 0.3) // recommended by GPT
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(directory = context.cacheDir.resolve("image_cache"))
                .maxSizeBytes(size = 200L * 1024 * 1024) // 200MB, recommended by GPT
                .build()
        }
        // From Now in Android.
        // https://github.com/android/nowinandroid/blob/55970c2487f82b8d33313e5d10dc97f2e5e531b9/core/network/src/main/kotlin/com/google/samples/apps/nowinandroid/core/network/di/NetworkModule.kt#L87
        //
        // Assume most content images are versioned urls
        // but some problematic images are fetching each time.
        .respectCacheHeaders(enable = false)
        .build()
}