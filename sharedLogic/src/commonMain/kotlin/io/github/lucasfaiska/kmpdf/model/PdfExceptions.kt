package io.github.lucasfaiska.kmpdf.model

/**
 * Base class for all PDF password-related exceptions.
 */
abstract class PdfPasswordException(message: String) : Exception(message)

/**
 * Thrown when a PDF document is password-protected and no password was provided.
 */
class PdfPasswordRequiredException : PdfPasswordException("The PDF document is password-protected.")

/**
 * Thrown when the provided password for a PDF document is incorrect.
 */
class PdfInvalidPasswordException : PdfPasswordException("The provided password is incorrect.")
