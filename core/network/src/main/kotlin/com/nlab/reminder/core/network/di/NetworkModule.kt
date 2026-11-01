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
import com.nlab.reminder.core.network.datasource.HtmlApi
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * `Retrofit.Builder.build()` throws without a base URL, but [HtmlApi] always receives an absolute `@Url`,
 * which Retrofit uses as is. So this value is never requested and any valid URL works.
 */
private const val UNUSED_BASE_URL = "https://localhost/"

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        interceptors: Set<@JvmSuppressWildcards Interceptor>
    ): OkHttpClient = OkHttpClient.Builder()
        .apply { interceptors.forEach(::addInterceptor) }
        .build()

    @Provides
    @Singleton
    fun provideHtmlApi(
        okHttpClientProvider: Lazy<OkHttpClient>
    ): HtmlApi = Retrofit.Builder()
        .baseUrl(UNUSED_BASE_URL)
        .callFactory { request -> okHttpClientProvider.get().newCall(request) }
        .build()
        .create(HtmlApi::class.java)

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClientProvider: Lazy<OkHttpClient>
    ): ImageLoader = ImageLoader.Builder(context)
        .okHttpClient { okHttpClientProvider.get() }
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

/**
 * Declares an empty [Interceptor] set so that builds without any contributor (e.g. release) still compile.
 * Debug-only interceptors are contributed with `@IntoSet` from the app's debug source set.
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkInterceptorModule {
    @Multibinds
    fun bindInterceptors(): Set<Interceptor>
}
