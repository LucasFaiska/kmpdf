package io.github.lucasfaiska.kmpdf.model

data class PdfError(
    val type: PdfErrorType,
    val message: String? = null,
    val throwable: Throwable? = null,
)
