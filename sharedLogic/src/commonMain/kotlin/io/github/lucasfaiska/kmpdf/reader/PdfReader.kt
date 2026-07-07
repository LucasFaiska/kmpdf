package io.github.lucasfaiska.kmpdf.reader

import io.github.lucasfaiska.kmpdf.model.PdfDocument

interface PdfReader {
    suspend fun open(bytes: ByteArray): PdfDocument
}
