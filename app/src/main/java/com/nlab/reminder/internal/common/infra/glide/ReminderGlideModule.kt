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

package com.nlab.reminder.internal.common.infra.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber

/**
 * @author thalys
 */
@GlideModule
class ReminderGlideModule : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean = false
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        try {
            val calculator = MemorySizeCalculator.Builder(context).build()
            val defaultMemoryCacheSize: Int = (calculator.memoryCacheSize / 1.5f).toInt()
            val defaultBitmapPoolSize: Int = (calculator.bitmapPoolSize / 1.5f).toInt()

            builder
                .setDefaultRequestOptions(
                    RequestOptions()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .disallowHardwareConfig()
                )
                .setMemoryCache(LruResourceCache(defaultMemoryCacheSize.toLong()))
                .setBitmapPool(LruBitmapPool(defaultBitmapPoolSize.toLong()))
                .setDiskCache {
                    DiskLruCacheWrapper.create(Glide.getPhotoCacheDir(context), MAX_DISK_CACHE_SIZE)
                }

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        private const val MAX_DISK_CACHE_SIZE: Long = 1024 * 1024 * 100
    }
}