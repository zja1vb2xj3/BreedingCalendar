package com.example.breedingcalendar.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.breedingcalendar.data.database.AppDatabase
import com.example.breedingcalendar.data.entity.CalendarEvent
import com.example.breedingcalendar.data.entity.CustomEventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

data class CalendarUiState(
    val currentYear: Int,
    val currentMonth: Int,
    val selectedDate: Long,
    val events: List<CalendarEvent> = emptyList(),
    val customEventTypes: List<CustomEventType> = emptyList()
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val dao = database.calendarDao()
    private val today = Calendar.getInstance()

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            currentYear = today.get(Calendar.YEAR),
            currentMonth = today.get(Calendar.MONTH),
            selectedDate = dateToEpochDay(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
        )
    )

    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        observeEvents()
        observeCustomEventTypes()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            dao.getAllEvents().collect { events ->
                _uiState.value = _uiState.value.copy(events = events)
            }
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            database.withTransaction {
                if (event.isScheduled) {
                    // 예정 일정만 삭제
                    dao.deleteEventById(event.id)
                } else {
                    // 실제 기록과 연결된 예정 일정 삭제
                    dao.deleteScheduledEventBySourceId(event.id)
                    dao.deleteEventById(event.id)
                }
            }
        }
    }

    private fun observeCustomEventTypes() {
        viewModelScope.launch {
            dao.getActiveCustomEventTypes().collect { types ->
                _uiState.value = _uiState.value.copy(customEventTypes = types)
            }
        }
    }

    fun moveMonth(amount: Int) {
        val state = _uiState.value
        val calendar = GregorianCalendar(
            state.currentYear,
            state.currentMonth,
            1
        )

        calendar.add(Calendar.MONTH, amount)

        _uiState.value = state.copy(
            currentYear = calendar.get(Calendar.YEAR),
            currentMonth = calendar.get(Calendar.MONTH)
        )
    }

    fun goToToday() {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        _uiState.value = _uiState.value.copy(
            currentYear = year,
            currentMonth = month,
            selectedDate = dateToEpochDay(year, month, day)
        )
    }

    fun selectDate(date: Long) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date
        )
    }

    // 커스텀 일정 등록
    fun addCustomEventType(
        name: String,
        intervalDays: Int
    ) {
        val safeName = name.trim()

        if (safeName.isBlank() || intervalDays < 0) return

        viewModelScope.launch {
            dao.insertCustomEventType(
                CustomEventType(
                    name = safeName,
                    repeatIntervalDays = intervalDays
                )
            )
        }
    }

    // 커스텀 일정 선택 시 기록 및 예정일 생성
    fun addEvent(
        type: CustomEventType,
        memo: String
    ) {
        val selectedDate = _uiState.value.selectedDate

        viewModelScope.launch {
            database.withTransaction {
                val eventId = dao.insertEvent(
                    CalendarEvent(
                        eventName = type.name,
                        customEventTypeId = type.id,
                        eventDate = selectedDate,
                        isScheduled = false,
                        memo = memo
                    )
                )

                if (type.repeatIntervalDays > 0) {
                    dao.insertEvent(
                        CalendarEvent(
                            eventName = type.name,
                            customEventTypeId = type.id,
                            eventDate = selectedDate + type.repeatIntervalDays,
                            isScheduled = true,
                            sourceEventId = eventId,
                            memo = ""
                        )
                    )
                }
            }
        }
    }

    // 커스텀 일정 미선택 시 메모만 기록
    fun addMemoEvent(memo: String) {
        val safeMemo = memo.trim()

        if (safeMemo.isBlank()) return

        val selectedDate = _uiState.value.selectedDate

        viewModelScope.launch {
            dao.insertEvent(
                CalendarEvent(
                    eventName = "메모",
                    customEventTypeId = null,
                    eventDate = selectedDate,
                    isScheduled = false,
                    memo = safeMemo
                )
            )
        }
    }

    companion object {

        fun dateToEpochDay(
            year: Int,
            month: Int,
            day: Int
        ): Long {
            val calendar = GregorianCalendar(
                TimeZone.getTimeZone("UTC")
            )

            calendar.clear()
            calendar.set(year, month, day)

            return calendar.timeInMillis / 86_400_000L
        }

        fun epochDayToCalendar(epochDay: Long): Calendar {
            val calendar = GregorianCalendar(
                TimeZone.getTimeZone("UTC")
            )

            calendar.timeInMillis = epochDay * 86_400_000L

            return calendar
        }
    }
}