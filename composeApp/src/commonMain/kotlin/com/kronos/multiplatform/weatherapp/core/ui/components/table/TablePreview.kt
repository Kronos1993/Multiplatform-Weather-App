package com.kronos.multiplatform.weatherapp.core.ui.components.table

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import com.kronos.multiplatform.weatherapp.core.ui.components.BodyText


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