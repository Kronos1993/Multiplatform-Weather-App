package com.kronos.multiplatform.weatherapp.core.ui.components.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kronos.multiplatform.weatherapp.core.ui.components.BodyText
import com.kronos.multiplatform.weatherapp.core.ui.components.ComponentSize
import com.kronos.multiplatform.weatherapp.core.ui.components.IconPosition
import com.kronos.multiplatform.weatherapp.core.ui.components.LoadingDialog
import com.kronos.multiplatform.weatherapp.core.ui.components.TextInputType
import com.kronos.multiplatform.weatherapp.core.ui.components.TextInputView
import com.kronos.multiplatform.weatherapp.core.ui.components.TitleText
import com.kronos.multiplatform.weatherapp.core.ui.components.button.Button
import com.kronos.multiplatform.weatherapp.core.ui.components.button.ButtonType
import com.kronos.multiplatform.weatherapp.core.util.format
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.loading_dialog_text
import weather_app.composeapp.generated.resources.loading_dialog_title
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class SortOrder {
    ASC, DESC
}

sealed class PagingMode {
    object Scroll : PagingMode()
    object Buttons : PagingMode()
}

data class PagingState(
    val mode: PagingMode,
    val isLoading: Boolean,
    val error: String? = null,
    val canGoNext: Boolean = false,
    val canGoPrevious: Boolean = false
)

sealed class ColumnHeader {

    data class Text(
        val text: String
    ) : ColumnHeader()

    data class Custom(
        val content: @Composable () -> Unit
    ) : ColumnHeader()
}

sealed class TableColumnItem<T> {

    data class TableHeader<T>(
        val header: ColumnHeader,
        val sortOrder: SortOrder? = null,
        val sortable: Boolean = false,
        val onSort: ((SortOrder) -> Unit)? = null
    ) : TableColumnItem<T>()

    data class TableData<T>(
        val value: T,
        val content: @Composable (T) -> Unit
    ) : TableColumnItem<T>()
}

data class TableColumn<T>(
    val id: String,
    val width: Dp? = null,
    val weight: Float? = null,
    val items: List<TableColumnItem<T>>
)

data class TableState<T> @OptIn(ExperimentalTime::class) constructor(
    val columns: List<TableColumn<T>>,
    val rowCount: Int,

    val isLoading: Boolean = false,
    val error: String? = null,

    val focusManager: FocusManager,
    val showSearch: Boolean = false,
    val query: String? = null,
    val queryPlaceholder: String? = null,
    val onSearch: ((String) -> Unit)? = null,

    val showDateFilter: Boolean = false,
    val dateTimeFormat: String,
    val fromLabel: String,
    val fromDate: Instant? = null,
    val toLabel: String,
    val toDate: Instant? = null,
    val onFromDateChange: ((Instant) -> Unit)? = null,
    val onToDateChange: ((Instant) -> Unit)? = null,
    val acceptDateText: String? = null,
    val cancelDateText: String? = null,

    val showTotalCount: Boolean = false,
    val totalCountText: String? = null,

    val pagingMode: PagingMode? = null,
    val nextText: String? = null,
    val backText: String? = null,
    val canGoNext: Boolean = false,
    val canGoPrevious: Boolean = false,

    val pageTextIndicator: String,
)

@OptIn(ExperimentalTime::class)
@Composable
fun <T> TableView(
    state: TableState<T>,
    modifier: Modifier = Modifier,
    onEndReached: (() -> Unit)? = null,
    isLoading: Boolean = false,
    onNextPage: (() -> Unit)? = null,
    onPreviousPage: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state) {
        if (state.pagingMode == PagingMode.Buttons) {
            listState.scrollToItem(0)
        }
    }

    // ---- END REACHED (SCROLL PAGING) ----
    if (state.pagingMode == PagingMode.Scroll && onEndReached != null) {
        LaunchedEffect(listState) {
            snapshotFlow {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val total = listState.layoutInfo.totalItemsCount
                lastVisible to total
            }.collect { (lastVisible, total) ->
                if (lastVisible != null && lastVisible >= total - 1) {
                    onEndReached()
                }
            }
        }
    }

    Column(
        Modifier
            .padding(12.dp)
    ) {
        // ---- SEARCH ----
        if (
            state.showSearch ||
            state.showDateFilter ||
            state.showTotalCount
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                // ---- START : TOTAL COUNT (ALWAYS START) ----
                if (state.showTotalCount) {
                    BodyText(
                        text = state.totalCountText.orEmpty(),
                        size = ComponentSize.MEDIUM,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // ---- CENTER : DATE ----
                if (state.showDateFilter && state.onFromDateChange != null && state.onToDateChange != null) {
                    Row(
                        modifier = Modifier.weight(2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        DateTimeRangeFilter(
                            acceptDateText = state.acceptDateText.orEmpty(),
                            cancelDateText = state.cancelDateText.orEmpty(),
                            from = state.fromDate,
                            to = state.toDate,
                            dateTimeFormat = state.dateTimeFormat,
                            fromLabel = state.fromLabel,
                            toLabel = state.toLabel,
                            onFromChange = state.onFromDateChange,
                            onToChange = state.onToDateChange
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (state.showSearch && state.onSearch != null) {
                    Row(
                        modifier = Modifier
                            .weight(2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Spacer(modifier = Modifier.weight(1f)) // 👈 empuja todo a la derecha

                        BodyText(
                            text = "Filter by:",
                            size = ComponentSize.MEDIUM
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        SearchBar(
                            modifier = Modifier.weight(3f),
                            query = state.query.orEmpty(),
                            onQueryChange = state.onSearch,
                            placeHolder = state.queryPlaceholder,
                            focusManager = state.focusManager
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Column(
            modifier
                .border(1.dp, Color.LightGray)
                .weight(1f)
        ) {
            // ---- HEADER ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                state.columns.forEach { column ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .then(
                                when {
                                    column.width != null -> Modifier.width(column.width)
                                    column.weight != null -> Modifier.weight(column.weight)
                                    else -> Modifier.weight(1f)
                                }
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val tableHeader = column.items.firstOrNull() as? TableColumnItem.TableHeader
                        tableHeader?.let { HeaderCell(it) }
                    }
                }
            }

            HorizontalDivider()

            // ---- BODY (SCROLLABLE) ----
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(state.rowCount) { rowIndex ->
                    Row(
                        Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        state.columns.forEach { column ->
                            Box(
                                modifier = Modifier.then(
                                    when {
                                        column.width != null -> Modifier.width(column.width)
                                        column.weight != null -> Modifier.weight(column.weight)
                                        else -> Modifier.weight(1f)
                                    }
                                ).padding(start = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val item = column.items[rowIndex + 1] as TableColumnItem.TableData
                                item.content(item.value)
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        LoadingDialog(
            showDialog = isLoading,
            title = Res.string.loading_dialog_title,
            message = Res.string.loading_dialog_text
        )
        when {
            state.error != null -> {
                TableStatusRow {
                    BodyText(state.error, textColor = Color.Red)
                }
            }
        }

        // ---- FOOTER (BUTTONS ONLY) ----
        if (state.pagingMode == PagingMode.Buttons) {
            PagingButtons(
                isLoading = isLoading,
                backText = state.backText,
                nextText = state.nextText,
                canGoNext = state.canGoNext,
                canGoPrevious = state.canGoPrevious,
                onNextPage = onNextPage,
                onPreviousPage = onPreviousPage,
                pageIndicatorText = state.pageTextIndicator,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun DateTimeRangeFilter(
    acceptDateText: String,
    cancelDateText: String,
    from: Instant?,
    to: Instant?,
    dateTimeFormat: String,
    fromLabel: String,
    toLabel: String,
    onFromChange: ((Instant) -> Unit),
    onToChange: ((Instant) -> Unit)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {

        DateTimeField(
            acceptDateText = acceptDateText,
            cancelDateText = cancelDateText,
            label = fromLabel,
            dateTimeFormat = dateTimeFormat,
            value = from,
            onValueChange = onFromChange,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        DateTimeField(
            acceptDateText = acceptDateText,
            cancelDateText = cancelDateText,
            label = toLabel,
            dateTimeFormat = dateTimeFormat,
            value = to,
            onValueChange = onToChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun DateTimeField(
    modifier: Modifier = Modifier,
    acceptDateText: String,
    cancelDateText: String,
    label: String,
    dateTimeFormat: String = "MM/dd/yyyy hh:mm a",
    value: Instant?,
    onValueChange: (Instant) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val timeZone = TimeZone.currentSystemDefault()

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            )
            .clickable { showDialog = true }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BodyText(
                modifier = Modifier.weight(1f),
                text = value?.format(dateTimeFormat, timeZone) ?: label,
                size = ComponentSize.LARGE
            )

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDialog) {
        DateTimePickerDialog(
            primaryText = acceptDateText,
            secondaryText = cancelDateText,
            initialValue = value,
            onDismiss = { showDialog = false },
            onConfirm = {
                onValueChange(it)
                showDialog = false
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun DateTimePickerDialog(
    primaryText: String,
    secondaryText: String,
    initialValue: Instant?,
    onDismiss: () -> Unit,
    onConfirm: (Instant) -> Unit
) {
    val timeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now()

    val initialDateTime = initialValue
        ?.toLocalDateTime(timeZone)
        ?: now.toLocalDateTime(timeZone)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialValue?.toEpochMilliseconds(),
        initialDisplayMode = DisplayMode.Input
    )

    val timePickerState = rememberTimePickerState(
        initialHour = initialDateTime.hour,
        initialMinute = initialDateTime.minute,
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                DatePicker(state = datePickerState)
                Spacer(Modifier.height(12.dp))

                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        type = ButtonType.OUTLINED,
                        text = secondaryText
                    )

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val millis = datePickerState.selectedDateMillis
                                ?: return@Button

                            val date = Instant
                                .fromEpochMilliseconds(millis)
                                .toLocalDateTime(timeZone)
                                .date

                            val result = LocalDateTime(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute,
                                year = date.year,
                                month = date.month.number,
                                day = date.day,
                            )

                            onConfirm(result.toInstant(timeZone))
                        },
                        type = ButtonType.FILLED,
                        text = primaryText
                    )
                }
            }
        }
    }
}


@Composable
private fun <T> HeaderCell(item: TableColumnItem.TableHeader<T>) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        when (val h = item.header) {
            is ColumnHeader.Text -> TitleText(
                h.text,
                size = ComponentSize.MEDIUM,
                fontWeight = FontWeight.Bold
            )

            is ColumnHeader.Custom -> h.content()
        }

        if (item.sortable && item.sortOrder != null && item.onSort != null) {
            Spacer(Modifier.width(6.dp))

            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = {
                    val next = when (item.sortOrder) {
                        SortOrder.ASC -> SortOrder.DESC
                        SortOrder.DESC -> SortOrder.ASC
                    }
                    item.onSort.invoke(next)
                }
            ) {
                Icon(
                    imageVector = when (item.sortOrder) {
                        SortOrder.ASC -> Icons.Default.ArrowUpward
                        SortOrder.DESC -> Icons.Default.ArrowDownward
                    },
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun TableStatusRow(
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        content()
    }
}

@Composable
private fun PagingButtons(
    isLoading: Boolean,
    backText: String?,
    nextText: String?,
    pageIndicatorText: String,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    onNextPage: (() -> Unit)?,
    onPreviousPage: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Button(
            onClick = { onPreviousPage?.invoke() },
            type = ButtonType.OUTLINED,
            enabled = canGoPrevious && !isLoading,
            text = backText,
            icon = Icons.Filled.ChevronLeft
        )

        Spacer(Modifier.width(8.dp))

        // ---- PAGE INDICATOR ----
        BodyText(
            text = pageIndicatorText,
            size = ComponentSize.SMALL,
            textAlign = TextAlign.End
        )

        Spacer(Modifier.width(8.dp))

        Button(
            onClick = { onNextPage?.invoke() },
            text = nextText,
            type = ButtonType.FILLED,
            enabled = canGoNext && !isLoading,
            icon = Icons.Filled.ChevronRight,
            iconPosition = IconPosition.END
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
    placeHolder: String? = null,
    onQueryChange: (String) -> Unit,
) {

    TextInputView(
        type = TextInputType.OUTLINED,
        modifier = modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            if (placeHolder != null) {
                BodyText(
                    placeHolder
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        isError = null,
        supportingText = null,
        trailingIcon = {},
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
}


@Preview(showBackground = true, widthDp = 900, heightDp = 600)
@Composable
fun TableViewPreview() {

    data class User(
        val id: Int,
        val name: String,
        val age: Int
    )

    val focusManager = LocalFocusManager.current

    val users = listOf(
        User(1, "Alice", 25),
        User(2, "Bob", 30),
        User(3, "Charlie", 22),
        User(4, "Diana", 28)
    )

    val columns = listOf(

        TableColumn<User>(
            id = "id",
            weight = 1f,
            items = listOf(
                TableColumnItem.TableHeader<User>(
                    header = ColumnHeader.Text("ID"),
                    sortable = true,
                    sortOrder = SortOrder.ASC,
                    onSort = {}
                )
            ) + users.map {
                TableColumnItem.TableData(it) { user ->
                    BodyText(user.id.toString())
                }
            }
        ),

        TableColumn<User>(
            id = "name",
            weight = 2f,
            items = listOf(
                TableColumnItem.TableHeader<User>(
                    header = ColumnHeader.Text("Name"),
                    sortable = true,
                    sortOrder = SortOrder.DESC,
                    onSort = {}
                )
            ) + users.map {
                TableColumnItem.TableData(it) { user ->
                    BodyText(user.name)
                }
            }
        ),

        TableColumn<User>(
            id = "age",
            weight = 1f,
            items = listOf(
                TableColumnItem.TableHeader<User>(
                    header = ColumnHeader.Text("Age")
                )
            ) + users.map {
                TableColumnItem.TableData(it) { user ->
                    BodyText("${user.age}")
                }
            }
        )
    )

    val state = TableState(
        columns = columns,
        rowCount = users.size,

        focusManager = focusManager,

        // SEARCH
        showSearch = true,
        query = "",
        queryPlaceholder = "Search user...",
        onSearch = {},

        // DATE FILTER
        showDateFilter = true,
        dateTimeFormat = "MM/dd/yyyy hh:mm a",
        fromLabel = "From",
        toLabel = "To",
        onFromDateChange = {},
        onToDateChange = {},
        acceptDateText = "OK",
        cancelDateText = "Cancel",

        // TOTAL COUNT
        showTotalCount = true,
        totalCountText = "Total: ${users.size}",

        // PAGING
        pagingMode = PagingMode.Buttons,
        nextText = "Next",
        backText = "Back",
        canGoNext = true,
        canGoPrevious = false,
        pageTextIndicator = "Page 1 of 3"
    )

    MaterialTheme {
        TableView(
            state = state,
            isLoading = false,
            onNextPage = {},
            onPreviousPage = {}
        )
    }
}