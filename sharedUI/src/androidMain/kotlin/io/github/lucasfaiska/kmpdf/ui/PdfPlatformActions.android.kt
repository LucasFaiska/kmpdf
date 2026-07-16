package io.github.lucasfaiska.kmpdf.ui

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import io.github.lucasfaiska.kmpdf.model.PdfSource
import java.io.File

internal class AndroidPdfPlatformActions(
    private val context: android.content.Context,
) : PdfPlatformActions {
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
                        val file = getFileFromIdentifier(source.identifier)
                        if (file != null && file.exists()) {
                            val uri =
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file,
                                )
                            putExtra(Intent.EXTRA_STREAM, uri)
                            type = "application/pdf"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        } else {
                            putExtra(Intent.EXTRA_TEXT, source.identifier)
                            type = "text/plain"
                        }
                    }
                }
            }
        val shareIntent = Intent.createChooser(sendIntent, null)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    private fun getFileFromIdentifier(identifier: String): File? =
        try {
            if (identifier.startsWith("file:///android_asset/")) {
                val assetPath = identifier.removePrefix("file:///android_asset/")
                val fileName = assetPath.substringAfterLast('/')
                val tempFile = File(context.cacheDir, fileName)
                context.assets.open(assetPath).use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } else {
                val uri = Uri.parse(identifier)
                if (uri.scheme == "content") {
                    val fileName = getFileNameFromContentUri(uri) ?: "document.pdf"
                    val tempFile = File(context.cacheDir, fileName)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile
                } else {
                    val path = identifier.removePrefix("file://")
                    File(path)
                }
            }
        } catch (_: Exception) {
            null
        }

    private fun getFileNameFromContentUri(uri: Uri): String? =
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }

    override fun download(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

@Composable
internal actual fun rememberPdfPlatformActions(): PdfPlatformActions {
    val context = LocalContext.current
    return remember(context) { AndroidPdfPlatformActions(context) }
}
