package com.kronos.multiplatform.weatherapp.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
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
    resetSwipe: Boolean = false,
    content: @Composable (T) -> Unit
) {
    var swipeDirection by remember { mutableStateOf<SwipeToDismissBoxValue?>(null) }
    var shouldAnimateExit by remember { mutableStateOf(false) }
    var isSwiped by remember { mutableStateOf(false) }

    // Estado para controlar el rebote
    val animatedOffset = remember { Animatable(0f) }

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    if (enableEndToStart) {
                        swipeDirection = SwipeToDismissBoxValue.EndToStart
                        isSwiped = true
                        false
                    } else {
                        false
                    }
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    if (enableStartToEnd) {
                        swipeDirection = SwipeToDismissBoxValue.StartToEnd
                        isSwiped = true
                        false
                    } else {
                        false
                    }
                }

                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.85f }
    )

    // Efecto para resetear el swipe
    LaunchedEffect(resetSwipe) {
        if (resetSwipe) {
            state.reset()
            swipeDirection = null
            isSwiped = false
            shouldAnimateExit = false
        }
    }

    // Efecto para animar el rebote cuando se hace swipe
    LaunchedEffect(isSwiped) {
        if (!isSwiped) return@LaunchedEffect

        when (swipeDirection) {
            SwipeToDismissBoxValue.EndToStart -> onSwipeEndToStart(item)
            SwipeToDismissBoxValue.StartToEnd -> onSwipeStartToEnd(item)
            else -> {}
        }

        animatedOffset.animateTo(
            targetValue = 20f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        animatedOffset.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        state.reset()
        isSwiped = false
    }


    // Modificador para el rebote
    val bounceModifier = if (isSwiped) {
        Modifier.offset {
            IntOffset(animatedOffset.value.toInt(), 0)
        }
    } else {
        Modifier
    }

    SwipeToDismissBox(
        state = state,
        modifier = modifier.then(bounceModifier),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(
    swipeDismissState: SwipeToDismissBoxState,
    startToEndIcon: ImageVector? = null,
    endToStartIcon: ImageVector? = null,
) {
    val progress = swipeDismissState.dismissDirection

    // Color de fondo que varía según el progreso del swipe
    val backgroundColor by animateColorAsState(
        targetValue = when (progress) {
            SwipeToDismissBoxValue.Settled -> Color.Transparent // Color transparente mientras el swipe es pequeño
            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary// Cambiar a rojo si el swipe ha pasado el 50%
            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error // Cambiar a verde si el swipe va hacia la izquierda
            else -> Color.Transparent
        }
    )

    // Mostrar el ícono dependiendo de la dirección del swipe
    val icon = when (progress) {
        SwipeToDismissBoxValue.StartToEnd -> {
            startToEndIcon ?: Icons.Filled.Archive
        }

        SwipeToDismissBoxValue.EndToStart -> {
            endToStartIcon ?: Icons.Filled.Delete
        }

        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = if (progress == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart
        else Alignment.CenterEnd
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}