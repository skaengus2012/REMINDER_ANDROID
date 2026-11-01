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

package com.nlab.reminder.core.network.datasource

import com.nlab.reminder.core.network.BaseUrls
import com.nlab.reminder.core.network.infra.RetrofitFactory
import dagger.Reusable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import javax.inject.Inject

/**
 * Fetches a page from any absolute URL. The body is returned unread, so the caller must close it and read it off the
 * main thread.
 *
 * @author Doohyun
 */
@Reusable
internal class HtmlDataSource @Inject constructor(
    retrofitFactory: RetrofitFactory
) : HtmlApiService by retrofitFactory
    .create(BaseUrls.UNUSED)
    .create(HtmlApiService::class.java)

private interface HtmlApiService {
    /**
     * Fetches the page at the absolute [url]. [Streaming] is required: without it Retrofit buffers the whole body in
     * memory, which defeats the stream parsing of the head element.
     */
    @Streaming
    @GET
    suspend fun getHtml(@Url url: String): ResponseBody
}
