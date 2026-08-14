package io.github.lucasfaiska.kmpdf.ui.material3

import androidx.compose.runtime.Immutable

/**
 * Labels for the password dialog component.
 *
 * @property title The title of the dialog.
 * @property message The message asking for the password.
 * @property errorMessage The error message shown when the password is incorrect.
 * @property textFieldLabel The label for the password text field.
 * @property confirmButton The text for the confirm button.
 * @property cancelButton The text for the cancel button.
 */
@Immutable
data class Material3PasswordDialogLabels(
    val title: String = "Password Required",
    val message: String = "This document is protected. Please enter the password.",
    val errorMessage: String = "Incorrect password. Please try again.",
    val textFieldLabel: String = "Password",
    val confirmButton: String = "Open",
    val cancelButton: String = "Cancel",
)
