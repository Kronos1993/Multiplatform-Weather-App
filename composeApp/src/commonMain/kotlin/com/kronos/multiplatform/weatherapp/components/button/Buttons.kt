package com.kronos.multiplatform.weatherapp.components.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kronos.multiplatform.weatherapp.components.BodyText
import com.kronos.multiplatform.weatherapp.components.ComponentSize
import com.kronos.multiplatform.weatherapp.components.IconPosition

@Composable
fun Button(
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    text: String? = null,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.FILLED,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    iconPosition: IconPosition = IconPosition.START,
    size: ComponentSize = ComponentSize.SMALL,
    shape: ButtonShape = ButtonShape.CIRCLE
) {
    val buttonColors = getButtonColors(style, type)
    val contentColor = if (enabled) buttonColors.contentColor else buttonColors.disabledContentColor
    val containerColor =
        if (enabled) buttonColors.containerColor else buttonColors.disabledContainerColor

    val buttonShape = when (shape) {
        ButtonShape.SQUARE -> when (size) {
            ComponentSize.EXTRA_SMALL -> MaterialTheme.shapes.extraSmall.copy(CornerSize(4.dp))
            ComponentSize.SMALL -> MaterialTheme.shapes.small.copy(CornerSize(4.dp))
            ComponentSize.MEDIUM -> MaterialTheme.shapes.medium.copy(CornerSize(6.dp))
            ComponentSize.LARGE -> MaterialTheme.shapes.large.copy(CornerSize(8.dp))
            ComponentSize.EXTRA_LARGE -> MaterialTheme.shapes.extraLarge.copy(CornerSize(10.dp))
        }

        ButtonShape.CIRCLE -> when (size) {
            ComponentSize.EXTRA_SMALL -> CircleShape
            ComponentSize.SMALL -> CircleShape
            ComponentSize.MEDIUM -> CircleShape
            ComponentSize.LARGE -> CircleShape
            ComponentSize.EXTRA_LARGE -> CircleShape
        }

        ButtonShape.CUT -> when (size) {
            ComponentSize.EXTRA_SMALL -> CutCornerShape(12.dp)
            ComponentSize.SMALL -> CutCornerShape(12.dp)
            ComponentSize.MEDIUM -> CutCornerShape(16.dp)
            ComponentSize.LARGE -> CutCornerShape(20.dp)
            ComponentSize.EXTRA_LARGE -> CutCornerShape(24.dp)
        }
    }

    val buttonModifier = modifier
        .then(
            when (size) {
                ComponentSize.EXTRA_SMALL -> Modifier
                    .height(32.dp)
                    .defaultMinSize(minWidth = if (text == null) 32.dp else 64.dp)

                ComponentSize.SMALL -> Modifier
                    .height(36.dp)
                    .defaultMinSize(minWidth = if (text == null) 36.dp else 72.dp)

                ComponentSize.MEDIUM -> Modifier
                    .height(40.dp)
                    .defaultMinSize(minWidth = if (text == null) 40.dp else 80.dp)

                ComponentSize.LARGE -> Modifier
                    .height(48.dp)
                    .defaultMinSize(minWidth = if (text == null) 48.dp else 88.dp)

                ComponentSize.EXTRA_LARGE -> Modifier
                    .height(56.dp)
                    .defaultMinSize(minWidth = if (text == null) 56.dp else 96.dp)
            }
        )

    // Ajustar padding para botones circulares
    val horizontalPadding = when {
        shape == ButtonShape.CIRCLE && text == null -> when (size) {
            ComponentSize.EXTRA_SMALL -> 0.dp
            ComponentSize.SMALL -> 0.dp
            ComponentSize.MEDIUM -> 0.dp
            ComponentSize.LARGE -> 0.dp
            ComponentSize.EXTRA_LARGE -> 0.dp
        }

        else -> when (size) {
            ComponentSize.EXTRA_SMALL -> 8.dp
            ComponentSize.SMALL -> 10.dp
            ComponentSize.MEDIUM -> 12.dp
            ComponentSize.LARGE -> 14.dp
            ComponentSize.EXTRA_LARGE -> 16.dp
        }
    }

    when (type) {
        ButtonType.FILLED -> {
            FilledTonalButton(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier.padding(horizontal = horizontalPadding),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                shape = buttonShape
            ) {
                ButtonContent(icon, iconPosition, text, contentColor, size)
            }
        }

        ButtonType.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier.padding(horizontal = horizontalPadding),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                border = BorderStroke(
                    width = when (size) {
                        ComponentSize.EXTRA_SMALL -> 0.5.dp
                        else -> 1.dp
                    },
                    color = if (enabled) contentColor else buttonColors.disabledContentColor
                ),
                shape = buttonShape
            ) {
                ButtonContent(icon, iconPosition, text, contentColor, size)
            }
        }

        ButtonType.TEXT -> {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier.padding(horizontal = horizontalPadding),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                shape = buttonShape
            ) {
                ButtonContent(icon, iconPosition, text, contentColor, size)
            }
        }
    }
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.FILLED,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    size: ComponentSize = ComponentSize.SMALL,
    shape: ButtonShape = ButtonShape.CIRCLE,
    iconColor: Color? = null
) {
    val buttonColors = getButtonColors(style, type)
    val contentColor =
        iconColor ?: if (enabled) buttonColors.contentColor else buttonColors.disabledContentColor
    val containerColor =
        if (enabled) buttonColors.containerColor else buttonColors.disabledContainerColor

    val buttonShape = when (shape) {
        ButtonShape.SQUARE -> when (size) {
            ComponentSize.EXTRA_SMALL -> MaterialTheme.shapes.extraSmall.copy(CornerSize(4.dp))
            ComponentSize.SMALL -> MaterialTheme.shapes.small.copy(CornerSize(4.dp))
            ComponentSize.MEDIUM -> MaterialTheme.shapes.medium.copy(CornerSize(6.dp))
            ComponentSize.LARGE -> MaterialTheme.shapes.large.copy(CornerSize(8.dp))
            ComponentSize.EXTRA_LARGE -> MaterialTheme.shapes.extraLarge.copy(CornerSize(10.dp))
        }

        ButtonShape.CIRCLE -> when (size) {
            ComponentSize.EXTRA_SMALL -> CircleShape
            ComponentSize.SMALL -> CircleShape
            ComponentSize.MEDIUM -> CircleShape
            ComponentSize.LARGE -> CircleShape
            ComponentSize.EXTRA_LARGE -> CircleShape
        }

        ButtonShape.CUT -> when (size) {
            ComponentSize.EXTRA_SMALL -> CutCornerShape(12.dp)
            ComponentSize.SMALL -> CutCornerShape(12.dp)
            ComponentSize.MEDIUM -> CutCornerShape(16.dp)
            ComponentSize.LARGE -> CutCornerShape(20.dp)
            ComponentSize.EXTRA_LARGE -> CutCornerShape(24.dp)
        }
    }

    val buttonModifier = modifier
        .then(
            when (size) {
                ComponentSize.EXTRA_SMALL -> Modifier
                    .size(20.dp)

                ComponentSize.SMALL -> Modifier
                    .size(24.dp)

                ComponentSize.MEDIUM -> Modifier
                    .size(24.dp)

                ComponentSize.LARGE -> Modifier
                    .size(32.dp)

                ComponentSize.EXTRA_LARGE -> Modifier
                    .size(40.dp)
            }
        )

    when (type) {
        ButtonType.FILLED -> {
            FilledIconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                shape = buttonShape
            ) {
                IconButtonContent(icon, contentColor, size)
            }
        }

        ButtonType.OUTLINED -> {
            OutlinedIconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                border = BorderStroke(
                    width = when (size) {
                        ComponentSize.EXTRA_SMALL -> 0.5.dp
                        else -> 1.dp
                    },
                    color = if (enabled) contentColor else buttonColors.disabledContentColor
                ),
                shape = buttonShape
            ) {
                IconButtonContent(icon, contentColor, size)
            }
        }

        ButtonType.TEXT -> {
            OutlinedIconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                border = BorderStroke(
                    width = when (size) {
                        ComponentSize.EXTRA_SMALL -> 0.5.dp
                        else -> 1.dp
                    },
                    color = Color.Transparent
                ),
                shape = buttonShape
            ) {
                IconButtonContent(icon, contentColor, size)
            }
        }
    }
}

@Composable
private fun getButtonColors(
    style: ButtonStyle,
    type: ButtonType
): ButtonColors {
    val colors = MaterialTheme.colorScheme
    return when (style) {
        ButtonStyle.PRIMARY -> when (type) {
            ButtonType.FILLED -> ButtonColors(
                containerColor = colors.primary,
                contentColor = Color.White,
                disabledContainerColor = colors.primaryContainer.copy(alpha = 0.12f),
                disabledContentColor = colors.onPrimary.copy(alpha = 0.38f)
            )

            else -> ButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.primary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.primary.copy(alpha = 0.38f)
            )
        }

        ButtonStyle.ALERT -> when (type) {
            ButtonType.FILLED -> ButtonColors(
                containerColor = colors.error,
                contentColor = colors.onError,
                disabledContainerColor = colors.errorContainer,
                disabledContentColor = colors.onErrorContainer.copy(alpha = 0.38f)
            )

            else -> ButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.error,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.error.copy(alpha = 0.38f)
            )
        }

        ButtonStyle.WARNING -> when (type) {
            ButtonType.FILLED -> ButtonColors(
                containerColor = Color(0xFFFFA000), // Amber 700
                contentColor = Color.White,
                disabledContainerColor = Color(0x33FFA000),
                disabledContentColor = Color(0x99FFA000)
            )

            else -> ButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFFFFA000),
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color(0x99FFA000)
            )
        }

        ButtonStyle.SUCCESS -> when (type) {
            ButtonType.FILLED -> ButtonColors(
                containerColor = colors.tertiary,
                contentColor = colors.onTertiary,
                disabledContainerColor = colors.tertiaryContainer,
                disabledContentColor = colors.onTertiaryContainer.copy(alpha = 0.38f)
            )

            else -> ButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.tertiary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.tertiary.copy(alpha = 0.38f)
            )
        }

        ButtonStyle.INFO -> when (type) {
            ButtonType.FILLED -> ButtonColors(
                containerColor = colors.secondary,
                contentColor = colors.onSecondary,
                disabledContainerColor = colors.secondaryContainer,
                disabledContentColor = colors.onSecondaryContainer.copy(alpha = 0.38f)
            )

            else -> ButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.secondary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.secondary.copy(alpha = 0.38f)
            )
        }

        ButtonStyle.DEFAULT -> when (type) {
            ButtonType.FILLED -> ButtonColors(
                containerColor = colors.surfaceVariant,
                contentColor = colors.onSurfaceVariant,
                disabledContainerColor = colors.surfaceVariant.copy(alpha = 0.12f),
                disabledContentColor = colors.inverseOnSurface.copy(alpha = 0.38f)
            )

            else -> ButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.onSurfaceVariant,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
private fun ButtonContent(
    icon: ImageVector?,
    iconPosition: IconPosition,
    text: String?,
    contentColor: Color,
    size: ComponentSize,
) {
    val iconSize = when (size) {
        ComponentSize.EXTRA_SMALL -> 16.dp
        ComponentSize.SMALL -> 18.dp
        ComponentSize.MEDIUM -> 20.dp
        ComponentSize.LARGE -> 22.dp
        ComponentSize.EXTRA_LARGE -> 24.dp
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (text == null) {
            Icon(
                imageVector = icon ?: Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
        } else {
            if (icon != null && iconPosition == IconPosition.START) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = when (size) {
                    ComponentSize.EXTRA_SMALL -> 12.sp
                    ComponentSize.SMALL -> 13.sp
                    ComponentSize.MEDIUM -> 14.sp
                    ComponentSize.LARGE -> 15.sp
                    ComponentSize.EXTRA_LARGE -> 16.sp
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (icon != null && iconPosition == IconPosition.END) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }

}

@Composable
private fun IconButtonContent(
    icon: ImageVector,
    contentColor: Color,
    size: ComponentSize,
) {
    val iconSize = when (size) {
        ComponentSize.EXTRA_SMALL -> 18.dp
        ComponentSize.SMALL -> 20.dp
        ComponentSize.MEDIUM -> 20.dp
        ComponentSize.LARGE -> 28.dp
        ComponentSize.EXTRA_LARGE -> 32.dp
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(iconSize)
    )
}

@Composable
private fun BuildFab(
    icon: ImageVector,
    text: String? = null,
    size: ComponentSize,
    modifier: Modifier,
    buttonColors: ButtonColors,
    onClick: () -> Unit
) {
    val shape = when (size) {
        ComponentSize.EXTRA_SMALL, ComponentSize.SMALL -> FloatingActionButtonDefaults.smallShape
        ComponentSize.MEDIUM -> FloatingActionButtonDefaults.extendedFabShape
        else -> FloatingActionButtonDefaults.largeShape
    }

    when {
        text == null -> {
            val fab: @Composable () -> Unit = when (size) {
                ComponentSize.EXTRA_SMALL, ComponentSize.SMALL -> {
                    {
                        SmallFloatingActionButton(
                            onClick,
                            modifier,
                            shape,
                            buttonColors.containerColor,
                            buttonColors.contentColor
                        ) {
                            Icon(icon, "FAB icon", tint = buttonColors.contentColor)
                        }
                    }
                }

                ComponentSize.MEDIUM -> {
                    {
                        FloatingActionButton(
                            onClick,
                            modifier,
                            shape,
                            buttonColors.containerColor,
                            buttonColors.contentColor
                        ) {
                            Icon(icon, "FAB icon", tint = buttonColors.contentColor)
                        }
                    }
                }

                else -> {
                    {
                        LargeFloatingActionButton(
                            onClick,
                            modifier,
                            shape,
                            buttonColors.containerColor,
                            buttonColors.contentColor
                        ) {
                            Icon(icon, "FAB icon", tint = buttonColors.contentColor)
                        }
                    }
                }
            }
            fab()
        }

        else -> {
            ExtendedFloatingActionButton(
                onClick = onClick,
                expanded = true,
                shape = shape,
                modifier = modifier,
                containerColor = buttonColors.containerColor,
                contentColor = buttonColors.contentColor,
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = "FAB icon",
                        tint = buttonColors.contentColor
                    )
                },
                text = {
                    BodyText(
                        text = text,
                        textColor = buttonColors.contentColor,
                        size = size
                    )
                }
            )
        }
    }
}

@Composable
private fun BuildFab(
    icon: @Composable () -> Unit,
    text: String? = null,
    size: ComponentSize,
    modifier: Modifier,
    buttonColors: ButtonColors,
    onClick: () -> Unit
) {
    val shape = when (size) {
        ComponentSize.EXTRA_SMALL, ComponentSize.SMALL -> FloatingActionButtonDefaults.smallShape
        ComponentSize.MEDIUM -> FloatingActionButtonDefaults.extendedFabShape
        else -> FloatingActionButtonDefaults.largeShape
    }

    when {
        text == null -> {
            val fab: @Composable () -> Unit = when (size) {
                ComponentSize.EXTRA_SMALL, ComponentSize.SMALL -> {
                    {
                        SmallFloatingActionButton(
                            onClick,
                            modifier,
                            shape,
                            buttonColors.containerColor,
                            buttonColors.contentColor,
                            content = icon
                        )
                    }
                }

                ComponentSize.MEDIUM -> {
                    {
                        FloatingActionButton(
                            onClick,
                            modifier,
                            shape,
                            buttonColors.containerColor,
                            buttonColors.contentColor,
                            content = icon
                        )
                    }
                }

                else -> {
                    {
                        LargeFloatingActionButton(
                            onClick,
                            modifier,
                            shape,
                            buttonColors.containerColor,
                            buttonColors.contentColor,
                            content = icon
                        )
                    }
                }
            }
            fab()
        }

        else -> {
            ExtendedFloatingActionButton(
                onClick = onClick,
                expanded = true,
                shape = shape,
                modifier = modifier,
                containerColor = buttonColors.containerColor,
                contentColor = buttonColors.contentColor,
                icon = icon,
                text = {
                    BodyText(
                        text = text,
                        textColor = buttonColors.contentColor,
                        size = size
                    )
                }
            )
        }
    }
}


@Composable
fun FabButton(
    icon: ImageVector,
    text: String? = null,
    type: ButtonType = ButtonType.FILLED,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    size: ComponentSize = ComponentSize.MEDIUM,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val buttonColors = getButtonColors(style, type)
    BuildFab(icon, text, size, modifier, buttonColors, onClick)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FabDialButton(
    icon: ImageVector,
    text: String? = null,
    dialActions: List<FabDialActions>,
    type: ButtonType = ButtonType.FILLED,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    size: ComponentSize = ComponentSize.MEDIUM,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onClick: () -> Unit
) {
    val buttonColors = getButtonColors(style, type)

    Column(horizontalAlignment = Alignment.End) {
        dialActions.forEachIndexed { index, action ->
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(
                    animationSpec = tween(
                        delayMillis = index * 60,
                        durationMillis = 250
                    )
                ) + slideInVertically(
                    animationSpec = tween(
                        delayMillis = index * 60,
                        durationMillis = 300
                    ),
                    initialOffsetY = { it * 2 }
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        delayMillis = (dialActions.size - index - 1) * 40,
                        durationMillis = 200
                    )
                ) + slideOutVertically(
                    animationSpec = tween(
                        delayMillis = (dialActions.size - index - 1) * 40,
                        durationMillis = 250
                    ),
                    targetOffsetY = { it * 2 }
                )
            ) {
                Column {
                    BuildFab(
                        icon = action.icon,
                        text = action.text,
                        size = size,
                        modifier = modifier,
                        buttonColors = buttonColors,
                        onClick = action.onClick
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        BuildFab(
            icon = {
                AnimatedContent(
                    targetState = expanded,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(150, 150)) with
                                fadeOut(animationSpec = tween(150)) using
                                SizeTransform(clip = false)
                    },
                    label = "fab-icon"
                ) { isExpanded ->
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.Close else icon,
                        contentDescription = "FAB icon",
                        tint = buttonColors.contentColor
                    )
                }
            },
            text = text,
            size = size,
            modifier = modifier,
            buttonColors = buttonColors,
            onClick = onClick
        )
    }
}