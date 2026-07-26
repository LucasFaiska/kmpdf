package io.github.lucasfaiska.kmpdf.androidApp.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.lucasfaiska.kmpdf.ui.PdfViewer
import io.github.lucasfaiska.kmpdf.ui.rememberPdfViewerState

@Preview(showBackground = true)
@Composable
fun PdfViewerPreview() {
    val state = rememberPdfViewerState()
    PdfViewer(
        state = state,
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true)
@Composable
fun PdfViewerWithToolbarPreview() {
    val state = rememberPdfViewerState()
    PdfViewer(
        state = state,
        modifier = Modifier.fillMaxSize(),
    )
}
