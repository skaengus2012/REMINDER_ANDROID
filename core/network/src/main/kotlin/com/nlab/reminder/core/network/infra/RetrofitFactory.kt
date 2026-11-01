/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.core.network.infra

import dagger.Lazy
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Creates [Retrofit] instances that share the app's single [OkHttpClient].
 *
 * The client is taken as [Lazy] and resolved on the first request, so that building it (and every interceptor it
 * carries) does not happen at injection time. Create every [Retrofit] here instead of passing the client around.
 *
 * @author Doohyun
 */
internal class RetrofitFactory @Inject constructor(
    private val okHttpClientProvider: Lazy<OkHttpClient>
) {
    fun create(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .callFactory { request -> okHttpClientProvider.get().newCall(request) }
        .build()
}
