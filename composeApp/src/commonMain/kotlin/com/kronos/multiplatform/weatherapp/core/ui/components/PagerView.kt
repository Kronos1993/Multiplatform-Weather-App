package com.kronos.multiplatform.weatherapp.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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

@Preview(showBackground = true, widthDp = 400, heightDp = 250)
@Composable
fun CarouselViewPreview() {

    val pagerState = rememberPagerState(pageCount = { 3 })

    val pages = listOf(
        PageItem(1) {
            LabelText("Page 1")
        },
        PageItem(2) {
            LabelText("Page 2")
        },
        PageItem(3) {
            LabelText("Page 3")
        }
    )

    MaterialTheme {
        CarouselView(
            pages = pages,
            pagerState = pagerState
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 250)
@Composable
fun CarouselViewNoSwipePreview() {

    val pagerState = rememberPagerState(pageCount = { 3 })

    val pages = listOf(
        PageItem(1) { LabelText("Locked 1") },
        PageItem(2) { LabelText("Locked 2") },
        PageItem(3) { LabelText("Locked 3") }
    )

    MaterialTheme {
        CarouselView(
            pages = pages,
            pagerState = pagerState,
            enableSwipe = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 800, heightDp = 300)
@Composable
fun MultiBrowseCarouselViewPreview() {

    val carouselState = rememberCarouselState { 5 }

    val pages = List(5) { index ->
        PageItem(index) {
            LabelText("Item $index")
        }
    }

    MaterialTheme {
        MultiBrowseCarouselView(
            pages = pages,
            pagerState = carouselState,
            itemPadding = 200.dp
        )
    }
}
