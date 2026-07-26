package io.github.lucasfaiska.kmpdf.ui.material3

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Material 3 implementation of the loading content using a [CircularProgressIndicator].
 */
@Composable
fun Material3LoadingContent(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier)
}
