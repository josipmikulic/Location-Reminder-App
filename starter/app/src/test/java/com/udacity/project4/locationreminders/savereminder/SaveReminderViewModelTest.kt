package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

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
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

    }

    @After
    fun clearViewModel() {
        saveReminderViewModel.showSnackBarInt.value = null
        stopKoin()
    }

    @Test
    fun invalidTitleValidateReminder_showsTitleErrorSnackbar() {
        // Given reminderTitle empty
        saveReminderViewModel.reminderTitle.value = null
        saveReminderViewModel.reminderDescription.value = "Description"
        saveReminderViewModel.reminderSelectedLocationStr.value = "Googleplex"

        // When calling validateAndSaveReminder()
        mainCoroutineRule.launch {
            saveReminderViewModel.validateAndSaveReminder()
        }

        // Then error message is shown
        val value = saveReminderViewModel.showSnackBarInt.value
        assertThat(value, `is`(R.string.err_enter_title))
    }

    @Test
    fun invalidDescriptionValidateReminder_showsDescriptionErrorSnackbar() {
        // Given reminderDescription empty
        saveReminderViewModel.reminderTitle.value = "Title"
        saveReminderViewModel.reminderDescription.value = null
        saveReminderViewModel.reminderSelectedLocationStr.value = "Googleplex"

        // When calling validateAndSaveReminder()
        mainCoroutineRule.launch {
            saveReminderViewModel.validateAndSaveReminder()
        }

        // Then error message is shown
        val value = saveReminderViewModel.showSnackBarInt.value
        assertThat(value, `is`(R.string.err_enter_description))
    }

    @Test
    fun invalidLocationValidateReminder_showsLocationErrorSnackbar() {
        // Given reminderDescription empty
        saveReminderViewModel.reminderTitle.value = "Title"
        saveReminderViewModel.reminderDescription.value = "Description"
        saveReminderViewModel.reminderSelectedLocationStr.value = null

        // When calling validateAndSaveReminder()
        mainCoroutineRule.launch {
            saveReminderViewModel.validateAndSaveReminder()
        }

        // Then error message is shown
        val value = saveReminderViewModel.showSnackBarInt.value
        assertThat(value, `is`(R.string.err_select_location))
    }
}