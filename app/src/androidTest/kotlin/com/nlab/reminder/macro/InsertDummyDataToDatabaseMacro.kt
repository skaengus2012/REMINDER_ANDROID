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

package com.nlab.reminder.macro

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.local.database.configuration.ReminderDatabase
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleHeadlineSaveInput
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTimingSaveInput
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.entity.ScheduleTagListEntity
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.time.Clock

/**
 * @author Doohyun
 */
@RunWith(AndroidJUnit4::class)
class InsertDummyDataToDatabaseMacro {
    private val faker: Faker = Faker(Locale.forLanguageTag(/* languageTag = */ "ko"))
    private val tagTextInputs: List<String> = listOf(
        "집안일",
        "약속",
        "건강",
        "공과금 내는 날~!!",
        "장보러 가는 날",
        "결혼준비",
        "경제",
        "핸드폰",
        "개발",
        "스터디",
        "데이트 장소 알아보기",
        "뭔가 엄청엄청엄청 긴 태그~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
    )
    private val scheduleHeadlineSaveInputs: List<ScheduleHeadlineSaveInput> = buildList {
        this += List(300) {
            ScheduleHeadlineSaveInput(
                title = "Programming STUDY!".toNonBlankString(),
                description = "Good to know about [${faker.programmingLanguage().name()}] with ${faker.name().fullName()}"
                    .toNonBlankString(),
                link = "https://github.com/skaengus2012/REMINDER_ANDROID".toNonBlankString()
            )
        }

        this += List(300) {
            ScheduleHeadlineSaveInput(
                title = "Travel ✈️".toNonBlankString(),
                description = "Go to [${faker.nation().capitalCity()}] with ${faker.name().fullName()}"
                    .toNonBlankString(),
                link = null
            )
        }

        this += List(300) {
            val book = faker.book()
            ScheduleHeadlineSaveInput(
                title = "Book club".toNonBlankString(),
                description = "About [${book.title()} of ${book.author()}]".toNonBlankString(),
                link = null
            )
        }
    }
    private val scheduleTimingSaveInput: ScheduleTimingSaveInput = ScheduleTimingSaveInput(
        triggerAt = Clock.System.now(),
        isTriggerAtDateOnly = false,
        repeatInput = null
    )

    private lateinit var database: ReminderDatabase
    private lateinit var scheduleDao: ScheduleDAO
    private lateinit var tagDao: TagDAO
    private lateinit var scheduleTagListDao: ScheduleTagListDAO

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = ReminderDatabase(context)
        scheduleDao = database.scheduleDAO()
        tagDao = database.tagDAO()
        scheduleTagListDao = database.scheduleTagListDAO()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertDummyData() = runBlocking {
        replaceDummyData(
            shuffledSchedules = false,
            scheduleCountLimit = Int.MAX_VALUE
        )
    }

    @Test
    fun insertShuffledDummyData() = runBlocking {
        replaceDummyData(
            shuffledSchedules = true,
            scheduleCountLimit = Int.MAX_VALUE
        )
    }

    @Test
    fun insertSmallAmountDummyData() = runBlocking {
        replaceDummyData(
            shuffledSchedules = false,
            scheduleCountLimit = genInt(min = 10, max = 20)
        )
    }

    @Test
    fun insertSmallAmountShuffledDummyData() = runBlocking {
        replaceDummyData(
            shuffledSchedules = true,
            scheduleCountLimit = genInt(min = 10, max = 20)
        )
    }

    private suspend fun replaceDummyData(shuffledSchedules: Boolean, scheduleCountLimit: Int) {
        database.clearAllTables()

        val savedTagEntities = tagTextInputs.map { tagText ->
            tagDao.insertAndGet(name = tagText.toNonBlankString())
        }
        val savedScheduleEntities = run {
            var ret = scheduleHeadlineSaveInputs.map { headlineInput ->
                scheduleDao.insertAndGet(headline = headlineInput, timing = scheduleTimingSaveInput)
            }
            if (shuffledSchedules) {
                ret = ret.shuffled()
            }
            ret.take(scheduleCountLimit)
        }
        val scheduleTagListEntities = savedScheduleEntities
            .map { scheduleEntity ->
                val shuffledTagEntities = savedTagEntities.shuffled()
                List(size = (1..shuffledTagEntities.size).random()) { shuffledTagEntities[it] }.map { tagEntity ->
                    ScheduleTagListEntity(
                        scheduleId = scheduleEntity.scheduleId,
                        tagId = tagEntity.tagId
                    )
                }
            }
            .flatten()
            .toSet()
        scheduleTagListDao.insert(entities = scheduleTagListEntities)
    }
}