
package com.example.breedingcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breedingcalendar.data.entity.CalendarEvent
import com.example.breedingcalendar.data.entity.CustomEventType
import java.util.Calendar
import java.util.GregorianCalendar

private val CalendarBackground = Color(0xFF121212)
private val CalendarSurface = Color(0xFF1E1E1E)
private val CalendarSelected = Color(0xFF345B75)
private val CalendarAccent = Color(0xFF80CBC4)
private val CalendarText = Color(0xFFEAEAEA)
private val CalendarSubText = Color(0xFF999999)

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel
) {
    val state by viewModel.uiState.collectAsState()

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    val selectedEvents = state.events.filter {
        it.eventDate == state.selectedDate
    }

    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = CalendarAccent,
            secondary = CalendarAccent,
            background = CalendarBackground,
            surface = CalendarSurface,
            onPrimary = Color.Black,
            onBackground = CalendarText,
            onSurface = CalendarText
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CalendarBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                Spacer(modifier = Modifier.height(18.dp))

                // 상단 타이틀
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Breeding Calendar",
                        color = CalendarText,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = { viewModel.goToToday() }
                    ) {
                        Text(
                            text = "오늘",
                            color = CalendarAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 월 이동 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "${state.currentYear}년 ${state.currentMonth + 1}월",
                        color = CalendarText,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "‹",
                            modifier = Modifier
                                .clickable {
                                    viewModel.moveMonth(-1)
                                }
                                .padding(horizontal = 14.dp),
                            color = CalendarText,
                            fontSize = 32.sp
                        )

                        Text(
                            text = "›",
                            modifier = Modifier
                                .clickable {
                                    viewModel.moveMonth(1)
                                }
                                .padding(horizontal = 14.dp),
                            color = CalendarText,
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 요일 표시
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val weekdays = listOf(
                        "일", "월", "화", "수", "목", "금", "토"
                    )

                    weekdays.forEachIndexed { index, day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = if (index == 0) {
                                Color(0xFFE57373)
                            } else if (index == 6) {
                                Color(0xFF64B5F6)
                            } else {
                                CalendarSubText
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 월간 달력
                val firstDay = CalendarViewModel.dateToEpochDay(
                    state.currentYear,
                    state.currentMonth,
                    1
                )

                val firstCalendar =
                    CalendarViewModel.epochDayToCalendar(firstDay)

                val firstWeekday =
                    firstCalendar.get(Calendar.DAY_OF_WEEK) - 1

                val daysInMonth = GregorianCalendar(
                    state.currentYear,
                    state.currentMonth,
                    1
                ).getActualMaximum(Calendar.DAY_OF_MONTH)

                val calendarCells = (0 until 42).map { index ->
                    val day = index - firstWeekday + 1

                    if (day in 1..daysInMonth) {
                        CalendarViewModel.dateToEpochDay(
                            state.currentYear,
                            state.currentMonth,
                            day
                        )
                    } else {
                        null
                    }
                }

                Column {
                    calendarCells.chunked(7).forEach { week ->

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            week.forEachIndexed { index, date ->

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.78f),
                                    contentAlignment = Alignment.Center
                                ) {

                                    if (date != null) {

                                        val calendar =
                                            CalendarViewModel.epochDayToCalendar(date)

                                        val day = calendar.get(
                                            Calendar.DAY_OF_MONTH
                                        )

                                        val isSelected =
                                            date == state.selectedDate

                                        val isToday =
                                            date == CalendarViewModel.dateToEpochDay(
                                                Calendar.getInstance().get(
                                                    Calendar.YEAR
                                                ),
                                                Calendar.getInstance().get(
                                                    Calendar.MONTH
                                                ),
                                                Calendar.getInstance().get(
                                                    Calendar.DAY_OF_MONTH
                                                )
                                            )

                                        val hasEvents =
                                            state.events.any {
                                                it.eventDate == date
                                            }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable {
                                                    viewModel.selectDate(date)
                                                    showAddDialog = true
                                                }
                                                .background(
                                                    color = if (isSelected) {
                                                        CalendarSelected
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .padding(vertical = 7.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Top
                                        ) {

                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .background(
                                                        color = if (isToday) {
                                                            CalendarAccent
                                                        } else {
                                                            Color.Transparent
                                                        },
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = day.toString(),
                                                    color = if (isToday) {
                                                        Color.Black
                                                    } else if (index == 0) {
                                                        Color(0xFFE57373)
                                                    } else if (index == 6) {
                                                        Color(0xFF64B5F6)
                                                    } else {
                                                        CalendarText
                                                    },
                                                    fontSize = 14.sp,
                                                    fontWeight = if (
                                                        isSelected || isToday
                                                    ) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Normal
                                                    }
                                                )
                                            }

                                            Spacer(
                                                modifier = Modifier.height(4.dp)
                                            )

                                            if (hasEvents) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(
                                                            CalendarAccent,
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 선택 날짜 일정 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(state.selectedDate),
                        color = CalendarText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "+ 일정 추가",
                        modifier = Modifier
                            .clickable {
                                showAddDialog = true
                            }
                            .padding(8.dp),
                        color = CalendarAccent,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedEvents.isEmpty()) {
                    Text(
                        text = "등록된 일정이 없습니다.",
                        color = CalendarSubText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn {
                        items(selectedEvents) { event ->
                            EventItem(event)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            customEventTypes = state.customEventTypes,
            onDismiss = {
                showAddDialog = false
            },
            onSave = { type, memo ->
                viewModel.addEvent(type, memo)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EventItem(
    event: CalendarEvent
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(
                CalendarSurface,
                RoundedCornerShape(10.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (event.isScheduled) {
                        Color(0xFFFFB74D)
                    } else {
                        CalendarAccent
                    },
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = event.eventName,
                color = CalendarText,
                fontWeight = FontWeight.Medium
            )

            if (event.memo.isNotBlank()) {
                Text(
                    text = event.memo,
                    color = CalendarSubText,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = if (event.isScheduled) "예정" else "기록",
            color = if (event.isScheduled) {
                Color(0xFFFFB74D)
            } else {
                CalendarAccent
            },
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AddEventDialog(
    customEventTypes: List<CustomEventType>,
    onDismiss: () -> Unit,
    onSave: (CustomEventType, String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    var selectedType by remember {
        mutableStateOf<CustomEventType?>(null)
    }

    var memo by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CalendarSurface,
        title = {
            Text(
                text = "일정 추가",
                color = CalendarText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {

                if (customEventTypes.isEmpty()) {
                    Text(
                        text = "등록된 커스텀 일정 유형이 없습니다.\n먼저 커스텀 일정을 추가해 주세요.",
                        color = CalendarSubText
                    )
                } else {

                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expanded = true
                                },
                            color = Color(0xFF303030),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = selectedType?.name
                                    ?: "일정 유형 선택",
                                color = CalendarText,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            customEventTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(type.name)
                                    },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    OutlinedTextField(
                        value = memo,
                        onValueChange = {
                            memo = it
                        },
                        label = {
                            Text("메모")
                        },
                        placeholder = {
                            Text("메모를 입력하세요.")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedType?.let {
                        onSave(it, memo)
                    }
                },
                enabled = selectedType != null
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("취소")
            }
        }
    )
}

private fun formatDate(epochDay: Long): String {
    val calendar = CalendarViewModel.epochDayToCalendar(epochDay)

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return "${year}년 ${month}월 ${day}일"
}
