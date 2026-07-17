package io.github.lucasfaiska.kmpdf.ui

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import io.github.lucasfaiska.kmpdf.model.PdfSource
import java.io.File

internal class AndroidPdfPlatformActions(
    private val context: android.content.Context,
) : PdfPlatformActions {
    override fun share(source: PdfSource) {
        val sendIntent =
            when (source) {
                is PdfSource.Url -> createTextShareIntent(source.url)
                is PdfSource.Local -> createFileShareIntent(source.identifier)
            }
        startShareActivity(sendIntent)
    }

    private fun createTextShareIntent(text: String): Intent =
        Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

    private fun createFileShareIntent(identifier: String): Intent {
        val file = getFileFromIdentifier(identifier)
        return if (file != null && file.exists()) {
            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            createTextShareIntent(identifier)
        }
    }

    private fun startShareActivity(intent: Intent) {
        val shareIntent = Intent.createChooser(intent, null)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    private fun getFileFromIdentifier(identifier: String): File? =
        try {
            when {
                identifier.startsWith("file:///android_asset/") -> handleAssetFile(identifier)
                identifier.startsWith("content://") -> handleContentUriFile(identifier.toUri())
                else -> handleDirectFile(identifier)
            }
        } catch (_: Exception) {
            null
        }

    private fun handleAssetFile(identifier: String): File {
        val assetPath = identifier.removePrefix("file:///android_asset/")
        val fileName = assetPath.substringAfterLast('/')
        val tempFile = File(context.cacheDir, fileName)
        context.assets.open(assetPath).use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun handleContentUriFile(uri: Uri): File {
        val fileName = getFileNameFromContentUri(uri) ?: "document.pdf"
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun handleDirectFile(identifier: String): File {
        val path = identifier.removePrefix("file://")
        return File(path)
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
        val intent =
            Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }
}

@Composable
internal actual fun rememberPdfPlatformActions(): PdfPlatformActions {
    val context = LocalContext.current
    return remember(context) { AndroidPdfPlatformActions(context) }
}
