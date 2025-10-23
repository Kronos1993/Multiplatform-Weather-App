package com.kronos.multiplatform.weatherapp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun <T> SwipeActionContainer(
    item: T,
    modifier: Modifier,
    enableStartToEnd: Boolean,
    startToEndIcon: ImageVector? = null,
    onSwipeStartToEnd: (T) -> Unit,
    enableEndToStart: Boolean,
    endToStartIcon: ImageVector? = null,
    onSwipeEndToStart: (T) -> Unit,
    animationDuration: Int = 500,
    resetSwipe: Boolean = false,
    content: @Composable (T) -> Unit
) {
    var isVisible by remember(resetSwipe) {
        mutableStateOf(true)
    }

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (!isVisible) return@rememberSwipeToDismissBoxState false

            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    if (enableEndToStart) {
                        isVisible = false
                        pendingAction = { onSwipeEndToStart(item) }
                        true
                    } else false
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (enableStartToEnd) {
                        isVisible = false
                        pendingAction = { onSwipeStartToEnd(item) }
                        true
                    } else false
                }
                else -> false
            }
        },
        positionalThreshold = { it * .85f }
    )

    // Ejecutar la acción después de la animación
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(animationDuration.toLong())
            pendingAction?.invoke()
            pendingAction = null
        }
    }

    // Reset cuando resetSwipe cambia a true
    LaunchedEffect(resetSwipe) {
        if (resetSwipe) {
            isVisible = true
            state.reset()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = state,
            modifier = modifier,
            backgroundContent = {
                SwipeBackground(
                    swipeDismissState = state,
                    startToEndIcon = startToEndIcon,
                    endToStartIcon = endToStartIcon
                )
            },
            content = { content(item) },
            enableDismissFromStartToEnd = enableStartToEnd,
            enableDismissFromEndToStart = enableEndToStart,
        )
    }
}

@Composable
fun SwipeBackground(
    swipeDismissState: SwipeToDismissBoxState,
    startToEndIcon: ImageVector? = null,
    endToStartIcon: ImageVector? = null,
) {
    val direction = swipeDismissState.dismissDirection

    val backgroundColor by animateColorAsState(
        targetValue = when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
            else -> Color.Transparent
        },
        label = "swipeBackground"
    )

    val icon = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> startToEndIcon
        SwipeToDismissBoxValue.EndToStart -> endToStartIcon
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart
        else Alignment.CenterEnd
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}