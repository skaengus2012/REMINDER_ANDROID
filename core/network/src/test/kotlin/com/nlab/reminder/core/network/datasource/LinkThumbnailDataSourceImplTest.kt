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

import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.toNonBlankString
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author Doohyun
 */
class LinkThumbnailDataSourceImplTest {
    @Test
    fun `Given html with og tags, When getting thumbnail, Then return title and image`() = runTest {
        val dataSource = genDataSource(html = html(head = OG_TITLE_TAG + OG_IMAGE_TAG))

        val actual = dataSource.getLinkThumbnail(URL.toNonBlankString())

        assertThat(actual, equalTo(Result.Success(LinkThumbnailResponse(title = "title", image = "image.png"))))
    }

    @Test
    fun `Given html without og tags, When getting thumbnail, Then return null title and image`() = runTest {
        val dataSource = genDataSource(html = html(head = "<title>no og</title>"))

        val actual = dataSource.getLinkThumbnail(URL.toNonBlankString())

        assertThat(actual, equalTo(Result.Success(LinkThumbnailResponse(title = null, image = null))))
    }

    @Test
    fun `Given html with only og title, When getting thumbnail, Then return null image`() = runTest {
        val dataSource = genDataSource(html = html(head = OG_TITLE_TAG))

        val actual = dataSource.getLinkThumbnail(URL.toNonBlankString())

        assertThat(actual, equalTo(Result.Success(LinkThumbnailResponse(title = "title", image = null))))
    }

    @Test
    fun `Given error status code, When getting thumbnail, Then return failure`() = runTest {
        val dataSource = genDataSource(html = html(head = OG_TITLE_TAG + OG_IMAGE_TAG), code = 404)

        val actual = dataSource.getLinkThumbnail(URL.toNonBlankString())

        assertThat(actual, instanceOf(Result.Failure::class.java))
    }

    @Test
    fun `Given network failure, When getting thumbnail, Then return failure`() = runTest {
        val dataSource = genDataSource(failure = IOException("network error"))

        val actual = dataSource.getLinkThumbnail(URL.toNonBlankString())

        assertThat(actual, instanceOf(Result.Failure::class.java))
    }

    @Test
    fun `Given interceptor registered in client, When getting thumbnail, Then request passes through the interceptor`() =
        runTest {
            var intercepted = false
            val dataSource = genDataSource(
                html = html(head = OG_TITLE_TAG),
                extraInterceptor = Interceptor { chain -> intercepted = true; chain.proceed(chain.request()) }
            )

            dataSource.getLinkThumbnail(URL.toNonBlankString())

            assertThat(intercepted, equalTo(true))
        }

    @Test
    fun `Given legacy charset declared only in meta tag, When getting thumbnail, Then decode title correctly`() =
        runTest {
            val html = """<html><head><meta charset="euc-kr"><meta property="og:title" content="네이버"></head></html>"""
            val dataSource = genDataSource(
                body = html.toByteArray(charset("euc-kr")),
                contentType = "text/html"
            )

            val actual = dataSource.getLinkThumbnail(URL.toNonBlankString())

            assertThat(actual, equalTo(Result.Success(LinkThumbnailResponse(title = "네이버", image = null))))
        }

    @Test
    fun `Given in-flight request, When coroutine cancelled, Then underlying call is cancelled`() = runTest {
        val started = CountDownLatch(1)
        val callCancelled = CountDownLatch(1)
        val dataSource = genDataSource(
            extraInterceptor = Interceptor { chain ->
                started.countDown()
                while (!chain.call().isCanceled()) Thread.sleep(10)
                callCancelled.countDown()
                throw IOException("Canceled")
            }
        )

        val job = launch { dataSource.getLinkThumbnail(URL.toNonBlankString()) }
        runCurrent()
        assertThat(started.await(5, TimeUnit.SECONDS), equalTo(true))
        job.cancel()

        assertThat(callCancelled.await(5, TimeUnit.SECONDS), equalTo(true))
    }
}

private const val URL = "https://example.com/page"
private const val OG_TITLE_TAG = """<meta property="og:title" content="title">"""
private const val OG_IMAGE_TAG = """<meta property="og:image" content="image.png">"""

private fun html(head: String): String = "<html><head>$head</head><body>body</body></html>"

private fun TestScope.genDataSource(
    html: String = "",
    body: ByteArray = html.toByteArray(),
    contentType: String = "text/html; charset=utf-8",
    code: Int = 200,
    failure: IOException? = null,
    extraInterceptor: Interceptor? = null
): LinkThumbnailDataSource = LinkThumbnailDataSourceImpl(
    dispatcher = StandardTestDispatcher(testScheduler),
    okHttpClient = OkHttpClient.Builder()
        .apply { extraInterceptor?.let(::addInterceptor) }
        .addInterceptor(fakeServer(body, contentType, code, failure))
        .build()
)

private fun fakeServer(body: ByteArray, contentType: String, code: Int, failure: IOException?) =
    Interceptor { chain ->
        failure?.let { throw it }
        chain.request().toResponse(body, contentType, code)
    }

private fun Request.toResponse(body: ByteArray, contentType: String, code: Int): Response = Response.Builder()
    .request(this)
    .protocol(Protocol.HTTP_1_1)
    .code(code)
    .message("fake")
    .body(body.toResponseBody(contentType.toMediaType()))
    .build()
