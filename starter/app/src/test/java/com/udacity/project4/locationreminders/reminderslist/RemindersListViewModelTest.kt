package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var dataSource: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        dataSource = FakeDataSource()
        mainCoroutineRule.launch {
            dataSource.saveReminder(reminder1)
            dataSource.saveReminder(reminder2)
            dataSource.saveReminder(reminder3)
        }

        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun clear() {
        stopKoin()
    }

    @Test
    fun loadReminders_setsRemindersList() {
        // When loading reminders
        mainCoroutineRule.launch {
            remindersListViewModel.loadReminders()
        }

        // Then reminders are saved in view model
        val value = remindersListViewModel.remindersList.value

        assertThat(value.isNullOrEmpty(), `is`(false))
        assertThat(value?.getOrNull(0), `is`(reminderData1))
        assertThat(value?.getOrNull(1), `is`(reminderData2))
        assertThat(value?.getOrNull(2), `is`(reminderData3))
    }

    @Test
    fun loadReminders_showsLoading() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()

        //WHEN
        remindersListViewModel.loadReminders()

        // THEN
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadRemindersError_showsSnackbar() = mainCoroutineRule.runBlockingTest {
        // GIVEN
        dataSource.setReturnError(true)

        // WHEN
        remindersListViewModel.loadReminders()

        // THEN
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test error"))
    }

    val reminder1 = ReminderDTO("title1", "description1", "Googleplex1", 37.422131, -122.084801)
    val reminder2 = ReminderDTO("title2", "description2", "Googleplex2", 37.422131, -122.084801)
    val reminder3 = ReminderDTO("title3", "description3", "Googleplex3", 37.422131, -122.084801)
    val reminderData1 = ReminderDataItem(
        reminder1.title, reminder1.description, reminder1.location, reminder1.latitude, reminder1.longitude,
        reminder1.id
    )
    val reminderData2 = ReminderDataItem(
        reminder2.title, reminder2.description, reminder2.location, reminder2.latitude, reminder2.longitude,
        reminder2.id
    )
    val reminderData3 = ReminderDataItem(
        reminder3.title, reminder3.description, reminder3.location, reminder3.latitude, reminder3.longitude,
        reminder3.id
    )
}