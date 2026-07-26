package io.github.lucasfaiska.kmpdf.model

/**
 * Represents a PDF document.
 *
 * @property pageCount The total number of pages in the document.
 */
interface PdfDocument : AutoCloseable {
    val pageCount: Int

    /**
     * Retrieves a specific page from the document.
     *
     * @param index The 0-based index of the page to retrieve.
     * @return A [PdfPage] instance representing the requested page.
     * @throws IllegalArgumentException if the index is out of bounds.
     */
    fun getPage(index: Int): PdfPage

    /**
     * Closes the document and releases any associated resources.
     */
    override fun close()
}
