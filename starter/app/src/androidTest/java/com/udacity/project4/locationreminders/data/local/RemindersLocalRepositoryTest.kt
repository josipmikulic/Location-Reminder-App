package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - a new reminder saved in the database
        val newReminder = ReminderDTO("title", "description", "Googleplex", 37.422131, -122.084801)
        localDataSource.saveReminder(newReminder)

        // WHEN  - Reminder retrieved by ID
        val result = localDataSource.getReminder(newReminder.id)

        // THEN - Same reminder is returned
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
        result as Result.Success
        assertThat(result.data.title, `is`("title"))
        assertThat(result.data.description, `is`("description"))
        assertThat(result.data.location, `is`("Googleplex"))
    }

    @Test
    fun getReminder_returnsErrorWhenReminderNotFound() = runBlocking {
        // GIVEN - empty database (or reminder with id not in database)

        // WHEN - Reminder retrieved by ID
        val result = localDataSource.getReminder("Any_String_ID")

        // THEN - Result returns error
        assertThat(result, `is`(instanceOf(Result.Error::class.java)))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun deleteAllReminders_clearsDatabase() = runBlocking {
        // GIVEN - reminders in database
        val newReminder = ReminderDTO("title", "description", "Googleplex", 37.422131, -122.084801)
        localDataSource.saveReminder(newReminder)

        // assert database not empty
        var result = localDataSource.getReminders()
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
        result as Result.Success
        assertTrue(result.data.isNotEmpty())

        // WHEN - deleteAllReminders called
        localDataSource.deleteAllReminders()

        // THEN - reminders database is empty
        result = localDataSource.getReminders()
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
        result as Result.Success
        assertTrue(result.data.isEmpty())
    }

}