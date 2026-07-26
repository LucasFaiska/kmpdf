package io.github.lucasfaiska.kmpdf.model

/**
 * Represents a source from which a PDF document can be loaded.
 */
sealed class PdfSource {
    /**
     * A remote PDF source accessible via a URL.
     *
     * @property url The full URL string of the PDF document.
     */
    data class Url(
        val url: String,
    ) : PdfSource()

    /**
     * A local PDF source accessible via a platform-specific identifier.
     * On Android, this can be a content URI (content://), a file URI (file://),
     * or an absolute file path.
     *
     * @property identifier The platform-specific string identifying the local resource.
     */
    data class Local(
        val identifier: String,
    ) : PdfSource()
}
