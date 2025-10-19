package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


class TabItem(
    var name: String = "",
    var iconSelected: ImageVector,
    var iconUnselected: ImageVector,
    var index: Int,
    var screen: @Composable () -> Unit
)

@Composable
fun ScrollableTabView(
    tabs: List<TabItem>,
    paddingValues: PaddingValues,
) {
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    val selectedTabIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { index ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    tabs[index].screen()
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(3.dp)
                            .padding(start = 5.dp, end = 5.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            TitleText(
                                text = tab.name,
                                textColor = MaterialTheme.colorScheme.primary
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (index == selectedTabIndex) {
                                    tab.iconSelected
                                } else tab.iconUnselected,
                                contentDescription = tab.name
                            )
                        }
                    )
                }
            }

        }
    }
}
