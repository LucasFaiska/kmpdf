package io.github.lucasfaiska.kmpdf.model

/**
 * Represents the status of a PDF loading operation.
 */
sealed class PdfLoadStatus {
    /**
     * Successfully loaded the PDF document.
     */
    data class Success(
        val document: PdfDocument,
    ) : PdfLoadStatus()

    /**
     * The document is password-protected and requires a password to open.
     */
    object PasswordRequired : PdfLoadStatus()

    /**
     * The provided password was incorrect.
     */
    object InvalidPassword : PdfLoadStatus()

    /**
     * An error occurred while loading the PDF.
     *
     * @property type The specific category of the error.
     */
    data class Error(
        val type: PdfErrorType,
    ) : PdfLoadStatus()
}

/**
 * Types of errors that can occur during PDF loading.
 */
enum class PdfErrorType {
    /**
     * Error related to Input/Output operations (e.g., file not found, network failure).
     */
    IO_ERROR,

    /**
     * The file data is corrupted or not a valid PDF.
     */
    CORRUPTED,

    /**
     * The PDF format or feature is not supported by the current engine.
     */
    UNSUPPORTED,

    /**
     * Any other unexpected error.
     */
    GENERIC,
}
