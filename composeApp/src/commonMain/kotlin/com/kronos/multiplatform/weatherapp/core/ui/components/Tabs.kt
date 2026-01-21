package com.kronos.multiplatform.weatherapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview


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
                            BodyText(
                                text = tab.name,
                                textColor = MaterialTheme.colorScheme.primary,
                                textOverflow = TextOverflow.Ellipsis,
                                maxLines = 1
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

@Composable
fun PillTabsHeader(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colors.secondaryContainer)
            .border(
                width = 1.dp,
                color = colors.onPrimaryContainer,
                shape = RoundedCornerShape(50)
            )
            .padding(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) colors.primary
                            else Color.Transparent
                        )
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) {
                                tab.iconSelected
                            } else {
                                tab.iconUnselected
                            },
                            contentDescription = tab.name,
                            tint = if (isSelected) {
                                colors.onPrimary
                            } else {
                                colors.onSurfaceVariant
                            }
                        )

                        TitleText(
                            text = tab.name,
                            textColor = if (isSelected) {
                                colors.onPrimary
                            } else {
                                colors.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PillTabPagerView(
    tabs: List<TabItem>,
    paddingValues: PaddingValues,
) {
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        PillTabsHeader(
            tabs = tabs,
            selectedIndex = pagerState.currentPage,
            onTabSelected = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            modifier = Modifier.padding(vertical = 12.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            tabs[page].screen()
        }
    }
}

@Preview(
    name = "Pill Tabs - Dark",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun PillTabsHeaderPreview() {
    val tabs = listOf(
        TabItem(
            name = "En vivo ahora",
            iconSelected = Icons.Default.Wifi,
            iconUnselected = Icons.Default.Wifi,
            index = 0,
            screen = {}
        ),
        TabItem(
            name = "Explorar",
            iconSelected = Icons.Default.Whatshot,
            iconUnselected = Icons.Default.Whatshot,
            index = 1,
            screen = {}
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        PillTabPagerView(
            tabs = tabs,
            paddingValues = PaddingValues(8.dp)
        )
    }
}