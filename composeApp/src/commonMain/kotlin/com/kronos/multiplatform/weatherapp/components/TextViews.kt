package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun IconComponent(
    drawable: DrawableResource? = null,
    vector: ImageVector? = null,
    iconTint: Color = Color.Unspecified,
    isDarkTheme: Boolean,
    iconModifier: Modifier = Modifier.size(24.dp)
) {
    if (drawable != null) {
        Icon(
            painterResource(drawable),
            contentDescription = null,
            tint = if (iconTint.isUnspecified) {
                if (isDarkTheme){
                    Color.White
                }else{
                    Color.Black
                }
            }else
                iconTint,
            modifier = iconModifier
        )
    } else if (vector != null)
        Icon(
            vector,
            tint = iconTint,
            contentDescription = null,
            modifier = iconModifier
        )
}

@Composable
private fun BaseTextComponent(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    textColor: Color? = null,
    iconTint: Color = Color.Unspecified,
    textOverflow: TextOverflow = TextOverflow.Visible,
    drawable: DrawableResource? = null,
    vector: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START,
    iconModifier: Modifier = Modifier.size(24.dp),
    iconSpacing: Dp = 8.dp,
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    fontStyle: FontStyle = FontStyle.Normal,
    softWrap: Boolean = true,
    obfuscate: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    isDarkTheme: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (iconPosition == IconPosition.START && (drawable != null || vector != null)) {
            IconComponent(drawable, vector, iconTint,isDarkTheme, iconModifier)
            Spacer(Modifier.width(iconSpacing))
        }
        Text(
            text = if (obfuscate) "*".repeat(text.length) else text,
            textAlign = textAlign,
            style = textStyle.copy(
                fontWeight = fontWeight,
                letterSpacing = letterSpacing,
                lineHeight = lineHeight
            ),
            color = textColor ?: MaterialTheme.colorScheme.onSurface,
            overflow = textOverflow,
            maxLines = maxLines,
            fontStyle = fontStyle,
            textDecoration = textDecoration,
            softWrap = softWrap
        )

        if (iconPosition == IconPosition.END && (drawable != null || vector != null)) {
            Spacer(Modifier.width(iconSpacing))
            IconComponent(drawable, vector, iconTint, isDarkTheme, iconModifier)
        }
    }
}

@Composable
fun DisplayText(
    text: String,
    modifier: Modifier = Modifier,
    size: ComponentSize = ComponentSize.SMALL,
    textColor: Color? = null,
    iconTint: Color = Color.Unspecified,
    textOverflow: TextOverflow = TextOverflow.Visible,
    drawable: DrawableResource? = null,
    vector: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START,
    iconModifier: Modifier = Modifier.size(24.dp),
    fontWeight: FontWeight = FontWeight.Bold,
    maxLines: Int = Int.MAX_VALUE,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    fontStyle: FontStyle = FontStyle.Normal,
    softWrap: Boolean = true,
    obfuscate: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    isDarkTheme: Boolean = false
) = BaseTextComponent(
    text = text,
    textStyle =
        when (size) {
            ComponentSize.MEDIUM -> MaterialTheme.typography.displayMedium
            ComponentSize.LARGE -> MaterialTheme.typography.displayLarge
            else -> MaterialTheme.typography.displaySmall
        },
    modifier = modifier,
    textColor = textColor,
    iconTint = iconTint,
    textOverflow = textOverflow,
    drawable = drawable,
    vector = vector,
    iconPosition = iconPosition,
    iconModifier = iconModifier,
    fontWeight = fontWeight,
    maxLines = maxLines,
    letterSpacing = letterSpacing,
    lineHeight = lineHeight,
    textDecoration = textDecoration,
    fontStyle = fontStyle,
    softWrap = softWrap,
    obfuscate = obfuscate,
    textAlign = textAlign,
    isDarkTheme = isDarkTheme
)

@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    size: ComponentSize = ComponentSize.SMALL,
    textColor: Color? = null,
    iconTint: Color = Color.Unspecified,
    textOverflow: TextOverflow = TextOverflow.Visible,
    drawable: DrawableResource? = null,
    vector: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START,
    iconModifier: Modifier = Modifier.size(24.dp),
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    fontStyle: FontStyle = FontStyle.Normal,
    softWrap: Boolean = true,
    obfuscate: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    isDarkTheme: Boolean = false
) = BaseTextComponent(
    text = text,
    textStyle =
        when (size) {
            ComponentSize.MEDIUM -> MaterialTheme.typography.titleMedium
            ComponentSize.LARGE -> MaterialTheme.typography.titleLarge
            else -> MaterialTheme.typography.titleSmall
        },
    modifier = modifier,
    textColor = textColor,
    iconTint = iconTint,
    textOverflow = textOverflow,
    drawable = drawable,
    vector = vector,
    iconPosition = iconPosition,
    iconModifier = iconModifier,
    fontWeight = fontWeight,
    maxLines = maxLines,
    letterSpacing = letterSpacing,
    lineHeight = lineHeight,
    textDecoration = textDecoration,
    fontStyle = fontStyle,
    softWrap = softWrap,
    obfuscate = obfuscate,
    textAlign = textAlign,
    isDarkTheme = isDarkTheme
)

@Composable
fun HeaderText(
    text: String,
    modifier: Modifier = Modifier,
    size: ComponentSize = ComponentSize.SMALL,
    textColor: Color? = null,
    iconTint: Color = Color.Unspecified,
    textOverflow: TextOverflow = TextOverflow.Visible,
    drawable: DrawableResource? = null,
    vector: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START,
    iconModifier: Modifier = Modifier.size(24.dp),
    fontWeight: FontWeight = FontWeight.Bold,
    maxLines: Int = Int.MAX_VALUE,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    fontStyle: FontStyle = FontStyle.Normal,
    softWrap: Boolean = true,
    obfuscate: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    isDarkTheme: Boolean = false
) = BaseTextComponent(
    text = text,
    textStyle =
        when (size) {
            ComponentSize.MEDIUM -> MaterialTheme.typography.headlineMedium
            ComponentSize.LARGE -> MaterialTheme.typography.headlineLarge
            else -> MaterialTheme.typography.headlineSmall
        },
    modifier = modifier,
    textColor = textColor,
    iconTint = iconTint,
    textOverflow = textOverflow,
    drawable = drawable,
    vector = vector,
    iconPosition = iconPosition,
    iconModifier = iconModifier,
    fontWeight = fontWeight,
    maxLines = maxLines,
    letterSpacing = letterSpacing,
    lineHeight = lineHeight,
    textDecoration = textDecoration,
    fontStyle = fontStyle,
    softWrap = softWrap,
    obfuscate = obfuscate,
    textAlign = textAlign,
    isDarkTheme = isDarkTheme
)

@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    size: ComponentSize = ComponentSize.SMALL,
    textColor: Color? = null,
    iconTint: Color = Color.Unspecified,
    textOverflow: TextOverflow = TextOverflow.Visible,
    drawable: DrawableResource? = null,
    vector: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START,
    iconModifier: Modifier = Modifier.size(24.dp),
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    fontStyle: FontStyle = FontStyle.Normal,
    softWrap: Boolean = true,
    obfuscate: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    isDarkTheme: Boolean = false
) = BaseTextComponent(
    text = text,
    textStyle =
        when (size) {
            ComponentSize.MEDIUM -> MaterialTheme.typography.bodyMedium
            ComponentSize.LARGE -> MaterialTheme.typography.bodyLarge
            else -> MaterialTheme.typography.bodySmall
        },
    modifier = modifier,
    textColor = textColor,
    iconTint = iconTint,
    textOverflow = textOverflow,
    drawable = drawable,
    vector = vector,
    iconPosition = iconPosition,
    iconModifier = iconModifier,
    fontWeight = fontWeight,
    maxLines = maxLines,
    letterSpacing = letterSpacing,
    lineHeight = lineHeight,
    textDecoration = textDecoration,
    fontStyle = fontStyle,
    softWrap = softWrap,
    obfuscate = obfuscate,
    textAlign = textAlign,
    isDarkTheme = isDarkTheme
)

@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    size: ComponentSize = ComponentSize.SMALL,
    textColor: Color? = null,
    iconTint: Color = Color.Unspecified,
    textOverflow: TextOverflow = TextOverflow.Visible,
    drawable: DrawableResource? = null,
    vector: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START,
    iconModifier: Modifier = Modifier.size(24.dp),
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    fontStyle: FontStyle = FontStyle.Normal,
    softWrap: Boolean = true,
    obfuscate: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    isDarkTheme: Boolean = false
) = BaseTextComponent(
    text = text,
    textStyle = when (size) {
        ComponentSize.MEDIUM -> MaterialTheme.typography.labelMedium
        ComponentSize.LARGE -> MaterialTheme.typography.labelLarge
        else -> MaterialTheme.typography.labelSmall
    },
    modifier = modifier,
    textColor = textColor,
    iconTint = iconTint,
    textOverflow = textOverflow,
    drawable = drawable,
    vector = vector,
    iconPosition = iconPosition,
    iconModifier = iconModifier,
    fontWeight = fontWeight,
    maxLines = maxLines,
    letterSpacing = letterSpacing,
    lineHeight = lineHeight,
    textDecoration = textDecoration,
    fontStyle = fontStyle,
    softWrap = softWrap,
    obfuscate = obfuscate,
    textAlign = textAlign,
    isDarkTheme = isDarkTheme
)