package io.github.lucasfaiska.kmpdf.ui.material3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Material 3 implementation of the password dialog for protected PDF documents.
 *
 * @param isInvalid Whether the last password attempt was invalid.
 * @param onConfirm Callback when the password is confirmed.
 * @param labels The labels to be used for text in the dialog.
 */
@Composable
fun Material3PasswordDialog(
    isInvalid: Boolean,
    onConfirm: (String) -> Unit,
    labels: Material3PasswordDialogLabels = Material3PasswordDialogLabels(),
) {
    var password by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text(labels.title) },
        text = {
            Column {
                val message = if (isInvalid) labels.errorMessage else labels.message
                Text(
                    message,
                    color = if (isInvalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(labels.textFieldLabel) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = isInvalid,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank(),
            ) {
                Text(labels.confirmButton)
            }
        },
    )
}
