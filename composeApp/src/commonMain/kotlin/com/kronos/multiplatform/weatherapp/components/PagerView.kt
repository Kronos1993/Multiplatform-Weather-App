package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class PageItem(
    var index: Int,
    var screen: @Composable () -> Unit
)

@Composable
fun CarouselView(
    pages: List<PageItem>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    enableSwipe: Boolean = true,
    boxPadding: Dp = 16.dp
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        userScrollEnabled = enableSwipe
    ) { pageIndex ->
        Box(
            modifier = Modifier
                .padding(boxPadding)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            pages[pageIndex].screen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiBrowseCarouselView(
    pages: List<PageItem>,
    pagerState: CarouselState,
    modifier: Modifier = Modifier,
    itemPadding: Dp = 250.dp,
    boxPadding: Dp = 16.dp
) {
    HorizontalMultiBrowseCarousel(
        state = pagerState,
        preferredItemWidth = itemPadding,
        modifier = modifier,
        itemSpacing = 10.dp
    ) { pageIndex ->
        Box(
            modifier = Modifier
                .padding(boxPadding)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            pages[pageIndex].screen()
        }
    }
}