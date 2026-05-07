package com.kronos.multiplatform.weatherapp.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class DropdownItem(
    var text: String,
    var value: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownView(
    options: List<DropdownItem>,
    defaultText: StringResource,
    selectedOption: String?,
    onOptionSelected: (DropdownItem) -> Unit,
    showError: Boolean = false,
    errorText: String? = null,
    modifier: Modifier = Modifier
) {
    // Opciones para el Dropdown
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // DropdownMenu para seleccionar el tipo de especialista
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption ?: stringResource(defaultText),
                onValueChange = { },
                readOnly = true,
                label = {
                    if (selectedOption.isNullOrEmpty())
                        BodyText(stringResource(defaultText), size = ComponentSize.LARGE)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { BodyText(option.text, size = ComponentSize.LARGE) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Si el error esta activo
        if (showError) {
            // Mostrar errores si el campo está vacío
            if (!errorText.isNullOrEmpty()) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
