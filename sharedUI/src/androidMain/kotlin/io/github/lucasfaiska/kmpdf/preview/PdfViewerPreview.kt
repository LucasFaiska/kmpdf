package io.github.lucasfaiska.kmpdf.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.ui.PdfViewer
import io.github.lucasfaiska.kmpdf.ui.rememberPdfViewerState

@Preview(showBackground = true)
@Composable
fun PdfViewerPreview() {
    PdfViewer(
        source = PdfSource.Url("https://example.com/sample.pdf"),
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true)
@Composable
fun PdfViewerLoadingPreview() {
    val state = rememberPdfViewerState()
    // Simulating loading
    LaunchedEffect(Unit) {
        state.load(PdfSource.Url("https://example.com/sample.pdf"))
    }
    PdfViewer(
        source = PdfSource.Url("https://example.com/sample.pdf"),
        state = state,
        modifier = Modifier.fillMaxSize(),
    )
}
