package com.kronos.multiplatform.weatherapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


enum class TextInputType {
    OUTLINED, FILLED
}

@Composable
fun getFontSize(size: ComponentSize): TextUnit {
    return when (size) {
        ComponentSize.MEDIUM -> MaterialTheme.typography.bodyLarge.fontSize
        ComponentSize.LARGE -> MaterialTheme.typography.headlineSmall.fontSize
        else -> MaterialTheme.typography.bodyMedium.fontSize
    }
}

@Composable
fun getShape(size: ComponentSize): CornerBasedShape {
    return when (size) {
        ComponentSize.MEDIUM -> MaterialTheme.shapes.medium
        ComponentSize.LARGE -> MaterialTheme.shapes.large
        else -> MaterialTheme.shapes.small
    }
}

@Composable
fun TextInputView(
    type: TextInputType,
    modifier: Modifier,
    size: ComponentSize = ComponentSize.MEDIUM,
    fontColor: Color = MaterialTheme.colorScheme.onBackground,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    isError: String? = null,
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardActions: KeyboardActions? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    val fontSize = getFontSize(size)

    val textStyle = TextStyle(
        fontSize = fontSize,
        color = fontColor
    )

    val shape = getShape(size)

    when (type) {
        TextInputType.OUTLINED -> {
            OutlinedTextField(
                textStyle = textStyle,
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = keyboardOptions,
                modifier = modifier.fillMaxWidth(),
                isError = isError != null,
                supportingText = supportingText,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                keyboardActions = keyboardActions ?: KeyboardActions.Default,
                readOnly = readOnly,
                placeholder = placeholder,
                visualTransformation = visualTransformation,
                enabled = enabled,
                singleLine = singleLine,
                shape = shape
            )
        }

        TextInputType.FILLED -> {
            TextField(
                textStyle = textStyle,
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = keyboardOptions,
                modifier = modifier.fillMaxWidth(),
                isError = isError != null,
                supportingText = supportingText,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                keyboardActions = keyboardActions ?: KeyboardActions.Default,
                readOnly = readOnly,
                placeholder = placeholder,
                visualTransformation = visualTransformation,
                enabled = enabled,
                singleLine = singleLine,
                shape = shape
            )
        }
    }
}

@Composable
fun TextInputView(
    type: TextInputType,
    modifier: Modifier,
    size: ComponentSize = ComponentSize.MEDIUM,
    fontColor: Color = MaterialTheme.colorScheme.onBackground,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    isError: String? = null,
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardActions: KeyboardActions? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    val fontSize = getFontSize(size)

    val textStyle = TextStyle(
        fontSize = fontSize,
        color = fontColor
    )

    val shape = getShape(size)

    when (type) {
        TextInputType.OUTLINED -> {
            OutlinedTextField(
                textStyle = textStyle,
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = keyboardOptions,
                modifier = modifier.fillMaxWidth(),
                isError = isError != null,
                supportingText = supportingText,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                keyboardActions = keyboardActions ?: KeyboardActions.Default,
                readOnly = readOnly,
                placeholder = placeholder,
                visualTransformation = visualTransformation,
                enabled = enabled,
                singleLine = singleLine,
                shape = shape
            )
        }

        TextInputType.FILLED -> {
            TextField(
                textStyle = textStyle,
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = keyboardOptions,
                modifier = modifier.fillMaxWidth(),
                isError = isError != null,
                supportingText = supportingText,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                keyboardActions = keyboardActions ?: KeyboardActions.Default,
                readOnly = readOnly,
                placeholder = placeholder,
                visualTransformation = visualTransformation,
                enabled = enabled,
                singleLine = singleLine,
            )
        }
    }
}


@Composable
fun VerificationCodeInput(
    codeLength: Int = 6,
    modifier: Modifier = Modifier,
    onCodeComplete: (String) -> Unit = {},
    onCodeChanged: (String) -> Unit = {}, // Nuevo callback para cambios
    isError: Boolean = false,
    errorText: String? = null
) {
    var code by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { List(codeLength) { FocusRequester() } }

    // Notificar cambios tanto para completado como para modificaciones
    LaunchedEffect(code) {
        onCodeChanged(code) // Notificar cualquier cambio
        if (code.length == codeLength) {
            onCodeComplete(code)
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(codeLength) { index ->
                    val char = code.getOrNull(index)?.toString() ?: ""
                    val isFocused = code.length == index

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = 1.dp,
                                color = when {
                                    isError -> MaterialTheme.colorScheme.error
                                    isFocused -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { focusRequesters[index].requestFocus() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            BasicTextField(
                                value = char,
                                onValueChange = { newChar ->
                                    when {
                                        // Caso borrado
                                        newChar.isEmpty() -> {
                                            if (code.isNotEmpty()) {
                                                code = code.dropLast(1)
                                                // Mover foco al campo anterior
                                                if (code.length > 0) {
                                                    focusRequesters[code.length - 1].requestFocus()
                                                }
                                            }
                                        }
                                        // Pegado de código
                                        newChar.length > 1 -> {
                                            code = newChar.take(codeLength)
                                        }
                                        // Nuevo dígito
                                        newChar.last().isDigit() && code.length < codeLength -> {
                                            code += newChar.last()
                                            // Mover foco al siguiente campo si no es el último
                                            if (code.length < codeLength) {
                                                focusRequesters[code.length].requestFocus()
                                            }
                                        }
                                    }
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequesters[index])
                                    .padding(0.dp)
                            )
                        }
                    }
                }
            }
        }

        if (!errorText.isNullOrEmpty()) {
            LabelText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                text = errorText,
                textColor = MaterialTheme.colorScheme.error,
                size = ComponentSize.MEDIUM
            )
        }
    }

    // Auto-enfocar el primer campo al aparecer
    DisposableEffect(Unit) {
        focusRequesters[0].requestFocus()
        onDispose {}
    }
}