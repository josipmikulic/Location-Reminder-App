package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private val viewModel: RemindersListViewModel by inject(RemindersListViewModel::class.java)
    private lateinit var dataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun initRepository() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanupDb() = runBlocking {
        dataSource.deleteAllReminders()
    }

    @Test
    fun clickAddReminderButton_navigateToAddReminderFragment() {
        // GIVEN - On the reminders list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the add reminder screen
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun remindersList_DisplayedInUi() = runTest {
        // GIVEN - Add reminder  to the DB
        val reminder = ReminderDTO("title", "description", "Googleplex", 37.422131, -122.084801)
        dataSource.saveReminder(reminder)

        // WHEN - Reminder list fragment launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - Reminder list is displayed on the screen
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderssRecyclerView)).check { view, _ ->
            assertTrue(view is RecyclerView && view.adapter != null && (view.adapter?.itemCount ?: 0) > 0)
        }
    }
}