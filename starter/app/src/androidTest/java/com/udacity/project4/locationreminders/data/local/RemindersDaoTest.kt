package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder = ReminderDTO("title", "description", "Googleplex", 37.422131, -122.084801)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
    }

    @Test
    fun getAllReminders() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder1 = ReminderDTO("title1", "description1", "Googleplex1", 37.422131, -122.084801)
        val reminder2 = ReminderDTO("title2", "description2", "Googleplex2", 37.422131, -122.084801)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - get all reminders
        val loaded = database.reminderDao().getReminders()

        // THEN - all data is returned
        assertThat(loaded.size, equalTo(2))
        assertThat(loaded[0].id, `is`(reminder1.id))
        assertThat(loaded[1].id, `is`(reminder2.id))
    }
}