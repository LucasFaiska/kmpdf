package io.github.lucasfaiska.kmpdf.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lucasfaiska.kmpdf.model.PdfSource

internal class AndroidPdfPlatformHelper(
    private val context: android.content.Context,
) : PdfPlatformHelper {
    override fun share(source: PdfSource) {
        val sendIntent: Intent =
            Intent().apply {
                action = Intent.ACTION_SEND
                when (source) {
                    is PdfSource.Url -> {
                        putExtra(Intent.EXTRA_TEXT, source.url)
                        type = "text/plain"
                    }
                    is PdfSource.Local -> {
                        putExtra(Intent.EXTRA_TEXT, source.identifier)
                        type = "text/plain"
                    }
                }
            }
        val shareIntent = Intent.createChooser(sendIntent, null)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    override fun download(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

@Composable
internal actual fun rememberPdfPlatformHelper(): PdfPlatformHelper {
    val context = LocalContext.current
    return remember(context) { AndroidPdfPlatformHelper(context) }
}
