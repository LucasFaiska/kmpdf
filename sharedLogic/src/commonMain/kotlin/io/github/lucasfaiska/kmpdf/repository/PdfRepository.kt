package io.github.lucasfaiska.kmpdf.repository

import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfSource

/**
 * Repository for managing PDF documents.
 */
interface PdfRepository {
    /**
     * Loads a [PdfDocument] from the given [source].
     *
     * @param source The source of the PDF.
     * @return The loaded [PdfDocument].
     * @throws Exception if the document cannot be loaded.
     */
    suspend fun loadDocument(source: PdfSource): PdfDocument
}
