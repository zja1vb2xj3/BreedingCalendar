
package com.example.breedingcalendar.ui.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breedingcalendar.data.entity.CalendarEvent
import com.example.breedingcalendar.data.entity.CustomEventType
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.abs

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

    var showCustomTypeDialog by remember {
        mutableStateOf(false)
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    var eventToDelete by remember {
        mutableStateOf<CalendarEvent?>(null)
    }

    // 달력 확장 상태
    var isCalendarExpanded by remember {
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

                Spacer(modifier = Modifier.height(32.dp))

                // 좌측 상단 더보기 메뉴
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        TextButton(
                            onClick = {
                                menuExpanded = true
                            }
                        ) {
                            Text(
                                text = "⋮",
                                color = CalendarText,
                                fontSize = 28.sp
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = {
                                menuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("커스텀 일정 추가")
                                },
                                onClick = {
                                    menuExpanded = false
                                    showCustomTypeDialog = true
                                }
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            viewModel.goToToday()
                        }
                    ) {
                        Text(
                            text = "오늘",
                            color = CalendarAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

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
                            textAlign = TextAlign.Center,
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

                // 달력 스와이프 영역
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(
                            isCalendarExpanded,
                            state.currentYear,
                            state.currentMonth
                        ) {
                            var dragX = 0f
                            var dragY = 0f

                            detectDragGestures(
                                onDragStart = {
                                    dragX = 0f
                                    dragY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    dragX += dragAmount.x
                                    dragY += dragAmount.y
                                    change.consume()
                                },
                                onDragEnd = {
                                    val threshold = 70f

                                    if (
                                        abs(dragX) > abs(dragY) &&
                                        abs(dragX) > threshold
                                    ) {
                                        // 왼쪽 스와이프: 다음 달
                                        if (dragX < 0) {
                                            viewModel.moveMonth(1)
                                        }
                                        // 오른쪽 스와이프: 이전 달
                                        else {
                                            viewModel.moveMonth(-1)
                                        }
                                    } else if (
                                        abs(dragY) > abs(dragX) &&
                                        abs(dragY) > threshold
                                    ) {
                                        // 아래 스와이프: 확장
                                        if (dragY > 0) {
                                            isCalendarExpanded = true
                                        }
                                        // 위 스와이프: 축소
                                        else {
                                            isCalendarExpanded = false
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    calendarCells.chunked(7).forEach { week ->

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            week.forEachIndexed { index, date ->

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (isCalendarExpanded) {
                                                Modifier.height(88.dp)
                                            } else {
                                                Modifier.aspectRatio(0.78f)
                                            }
                                        ),
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

                                        val today = Calendar.getInstance()

                                        val isToday =
                                            date == CalendarViewModel.dateToEpochDay(
                                                today.get(Calendar.YEAR),
                                                today.get(Calendar.MONTH),
                                                today.get(Calendar.DAY_OF_MONTH)
                                            )

                                        val dateEvents = state.events.filter {
                                            it.eventDate == date
                                        }

                                        val hasEvents = dateEvents.isNotEmpty()

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(
                                                    RoundedCornerShape(14.dp)
                                                )
                                                .clickable {
                                                    viewModel.selectDate(date)
                                                    showAddDialog = true
                                                }
                                                .background(
                                                    color = if (isSelected) {
                                                        CalendarSelected
                                                    } else {
                                                        Color.Transparent
                                                    }
                                                )
                                                .padding(
                                                    horizontal = 2.dp,
                                                    vertical = 7.dp
                                                ),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Top
                                        ) {

                                            // 날짜 숫자
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

                                            if (isCalendarExpanded) {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    dateEvents.take(2).forEach { event ->

                                                        val sourceEvent =
                                                            if (event.isScheduled) {
                                                                state.events.firstOrNull {
                                                                    it.id == event.sourceEventId
                                                                }
                                                            } else {
                                                                null
                                                            }

                                                        // 실제 기록은 메모 + 일정 이름 순서
                                                        val displayText =
                                                            if (event.isScheduled) {
                                                                val prefix = sourceEvent?.memo?.trim().orEmpty()

                                                                if (prefix.isNotBlank()) {
                                                                    "$prefix ${event.eventName}예정일"
                                                                } else {
                                                                    "${event.eventName} 예정일"
                                                                }
                                                            } else {
                                                                if (event.memo.isNotBlank()) {
                                                                    "${event.memo.trim()} ${event.eventName}"
                                                                } else {
                                                                    event.eventName
                                                                }
                                                            }

                                                        Text(
                                                            text = displayText,
                                                            color = if (event.isScheduled) {
                                                                Color(0xFFFFB74D)
                                                            } else {
                                                                CalendarSubText
                                                            },
                                                            fontSize = 8.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.fillMaxWidth()
                                                        )
                                                    }

                                                    if (dateEvents.size > 2) {
                                                        Text(
                                                            text = "...",
                                                            color = CalendarSubText,
                                                            fontSize = 8.sp,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.fillMaxWidth()
                                                        )
                                                    }
                                                }
                                            } else if (hasEvents) {
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

                            // 예정 일정의 원본 기록 찾기
                            val sourceEvent = if (event.isScheduled) {
                                state.events.firstOrNull {
                                    it.id == event.sourceEventId
                                }
                            } else {
                                null
                            }

                            EventItem(
                                event = event,
                                sourceMemo = sourceEvent?.memo.orEmpty(),
                                onLongClick = {
                                    eventToDelete = event
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 일반 일정 추가 팝업
    if (showAddDialog) {
        AddEventDialog(
            customEventTypes = state.customEventTypes,
            onDismiss = {
                showAddDialog = false
            },
            onSaveCustom = { type, memo ->
                viewModel.addEvent(type, memo)
                showAddDialog = false
            },
            onSaveMemo = { memo ->
                viewModel.addMemoEvent(memo)
                showAddDialog = false
            }
        )
    }

    // 커스텀 일정 등록 팝업
    if (showCustomTypeDialog) {
        AddCustomEventTypeDialog(
            onDismiss = {
                showCustomTypeDialog = false
            },
            onSave = { name, intervalDays ->
                viewModel.addCustomEventType(name, intervalDays)
                showCustomTypeDialog = false
            }
        )
    }

    // 일정 삭제 확인 팝업
    eventToDelete?.let { event ->
        AlertDialog(
            onDismissRequest = {
                eventToDelete = null
            },
            containerColor = CalendarSurface,
            title = {
                Text(
                    text = "일정 삭제",
                    color = CalendarText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "삭제하시겠습니까?",
                    color = CalendarText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvent(event)
                        eventToDelete = null
                    }
                ) {
                    Text(
                        text = "확인",
                        color = CalendarAccent
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        eventToDelete = null
                    }
                ) {
                    Text(
                        text = "취소",
                        color = CalendarSubText
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EventItem(
    event: CalendarEvent,
    sourceMemo: String = "",
    onLongClick: () -> Unit
) {
    // 실제 기록은 메모 + 일정 이름 순서
    // 예정 일정은 기존 표시 방식 유지
    val displayName = if (event.isScheduled) {
        val memoPrefix = sourceMemo.trim()

        if (memoPrefix.isNotBlank()) {
            "$memoPrefix ${event.eventName}예정일"
        } else {
            "${event.eventName} 예정일"
        }
    } else {
        if (event.memo.isNotBlank()) {
            "${event.memo.trim()} ${event.eventName}"
        } else {
            event.eventName
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
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
                text = displayName,
                color = CalendarText,
                fontWeight = FontWeight.Medium
            )
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
    onSaveCustom: (CustomEventType, String) -> Unit,
    onSaveMemo: (String) -> Unit
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
                                ?: "커스텀 일정 선택 (선택 사항)",
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

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = if (selectedType == null) {
                        "커스텀 일정을 선택하지 않으면 메모만 저장됩니다."
                    } else {
                        "선택한 커스텀 일정의 주기에 따라 예정일이 생성됩니다."
                    },
                    color = CalendarSubText,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val type = selectedType

                    if (type != null) {
                        onSaveCustom(type, memo)
                    } else if (memo.isNotBlank()) {
                        onSaveMemo(memo)
                    }
                },
                enabled = selectedType != null || memo.isNotBlank()
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

@Composable
private fun AddCustomEventTypeDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    var intervalText by remember {
        mutableStateOf("")
    }

    val interval = intervalText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CalendarSurface,
        title = {
            Text(
                text = "커스텀 일정 추가",
                color = CalendarText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("알림 이름")
                    },
                    placeholder = {
                        Text("예: 메이팅")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { value ->
                        if (
                            value.isEmpty() ||
                            value.all { it.isDigit() }
                        ) {
                            intervalText = value
                        }
                    },
                    label = {
                        Text("알림 주기 (Day)")
                    },
                    placeholder = {
                        Text("예: 30")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name.trim(), interval!!)
                },
                enabled = name.isNotBlank() &&
                        interval != null &&
                        interval >= 0
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
