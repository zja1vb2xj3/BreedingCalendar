package com.example.breedingcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.breedingcalendar.ui.calendar.CalendarScreen
import com.example.breedingcalendar.ui.calendar.CalendarViewModel
import com.example.breedingcalendar.ui.theme.BreedingCalendarTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val calendarViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CalendarViewModel::class.java]

        setContent {
            BreedingCalendarTheme {
                CalendarScreen(
                    viewModel = calendarViewModel
                )
            }
        }
    }
}
