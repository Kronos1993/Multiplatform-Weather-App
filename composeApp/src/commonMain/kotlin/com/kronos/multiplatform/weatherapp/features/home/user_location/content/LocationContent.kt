package com.kronos.multiplatform.weatherapp.features.home.user_location.content

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.kronos.multiplatform.weatherapp.components.UserCustomLocationItem
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation

@Composable
fun GridList(
    listState: LazyGridState,
    gridColumns: Int = 1,
    items: List<UserCustomLocation>,
    urlProvider: UrlProvider,
    imageQuality: String,
    darkTheme: Boolean,
    enableStartToEnd: Boolean = true,
    startToEndIcon: ImageVector,
    onSwipeStartToEnd: (UserCustomLocation) -> Unit,
    enableEndToStart: Boolean = true,
    endToStartIcon: ImageVector,
    onSwipeEndToStart: (UserCustomLocation) -> Unit,
    onItemClick: (UserCustomLocation) -> Unit,
    onItemLongClick: (UserCustomLocation) -> Unit,
    resetSwipe: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Fixed(gridColumns),
        modifier = modifier,
    ) {
        items(
            items = items,
            key = { it.id }
        ) {
            UserCustomLocationItem(
                item = it,
                urlProvider = urlProvider,
                imageQuality = imageQuality,
                darkTheme = darkTheme,
                enableStartToEnd = enableStartToEnd,
                startToEndIcon = startToEndIcon,
                onSwipeStartToEnd = onSwipeStartToEnd,
                enableEndToStart = !(it.isCurrent || it.isSelected),
                endToStartIcon = endToStartIcon,
                onSwipeEndToStart = onSwipeEndToStart,
                resetSwipe = resetSwipe,
                onItemClick = onItemClick,
                onItemLongClick = onItemLongClick,
            )
        }
    }
}