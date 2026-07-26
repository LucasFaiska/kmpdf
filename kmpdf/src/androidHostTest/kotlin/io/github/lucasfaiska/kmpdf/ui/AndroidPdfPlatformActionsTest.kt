package io.github.lucasfaiska.kmpdf.ui

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidPdfPlatformActionsTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val actions = AndroidPdfPlatformActions(app)
    private val mockUri = Uri.parse("content://mock/test.pdf")

    @Before
    fun setup() {
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns mockUri
    }

    @After
    fun tearDown() {
        unmockkStatic(FileProvider::class)
    }

    @Test
    fun `given url source when sharing then it should start send intent with text`() {
        val source = PdfSource.Url("https://example.com/test.pdf")

        actions.share(source)

        val nextActivity = shadowOf(app).nextStartedActivity
        assertNotNull(nextActivity)
        assertEquals(Intent.ACTION_CHOOSER, nextActivity.action)

        val targetIntent = nextActivity.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        assertNotNull(targetIntent)
        assertEquals(Intent.ACTION_SEND, targetIntent?.action)
        assertEquals("https://example.com/test.pdf", targetIntent?.getStringExtra(Intent.EXTRA_TEXT))
        assertEquals("text/plain", targetIntent?.type)
    }

    @Test
    fun `given local direct file source when sharing then it should start send intent with stream`() {
        val file = File(app.cacheDir, "test.pdf").apply { writeText("pdf content") }
        val source = PdfSource.Local("file://${file.absolutePath}")

        actions.share(source)

        val nextActivity = shadowOf(app).nextStartedActivity
        val targetIntent = nextActivity.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)

        assertEquals(Intent.ACTION_SEND, targetIntent?.action)
        assertEquals(mockUri, targetIntent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
        assertEquals("application/pdf", targetIntent?.type)
        val flags = targetIntent?.flags ?: 0
        assertTrue((flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
    }

    @Test
    fun `given asset source when sharing then it should copy to cache and share`() {
        val assetPath = "test_share.pdf"
        val source = PdfSource.Local("file:///android_asset/$assetPath")
        val content = "asset content"

        val mockAssets = mockk<android.content.res.AssetManager>()
        val mockContext = mockk<Context>(relaxed = true)
        every { mockContext.assets } returns mockAssets
        every { mockContext.cacheDir } returns app.cacheDir
        every { mockAssets.open(assetPath) } returns ByteArrayInputStream(content.toByteArray())

        val assetActions = AndroidPdfPlatformActions(mockContext)
        assetActions.share(source)

        val cacheFile = File(app.cacheDir, "test_share.pdf")
        assertTrue(cacheFile.exists())
        assertEquals(content, cacheFile.readText())
    }

    @Test
    fun `given content uri source when sharing then it should query name and copy to cache`() {
        val authority = "authority"
        val uri = Uri.parse("content://$authority/document.pdf")
        val source = PdfSource.Local(uri.toString())
        val content = "content uri data"

        TestContentProvider.instance = TestContentProvider("real_name.pdf", content.toByteArray(), app.cacheDir)
        Robolectric.setupContentProvider(TestContentProvider::class.java, authority)

        actions.share(source)

        val cacheFile = File(app.cacheDir, "real_name.pdf")
        assertTrue(cacheFile.exists())
        assertEquals(content, cacheFile.readText())
    }

    @Test
    fun `given download url when downloading then it should start view intent`() {
        val url = "https://example.com/doc.pdf"

        actions.download(url)

        val nextActivity = shadowOf(app).nextStartedActivity
        assertNotNull(nextActivity)
        assertEquals(Intent.ACTION_VIEW, nextActivity.action)
        assertEquals(Uri.parse(url), nextActivity.data)
        val flags = nextActivity.flags
        assertTrue((flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun `given invalid identifier when sharing then it should fallback to text share`() {
        val source = PdfSource.Local("invalid-path")

        actions.share(source)

        val nextActivity = shadowOf(app).nextStartedActivity
        val targetIntent = nextActivity.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)

        assertEquals(Intent.ACTION_SEND, targetIntent?.action)
        assertEquals("invalid-path", targetIntent?.getStringExtra(Intent.EXTRA_TEXT))
    }

    class TestContentProvider() : ContentProvider() {
        companion object {
            var instance: TestContentProvider? = null
        }

        var fileName: String = "document.pdf"
        var data: ByteArray = byteArrayOf()
        var cacheDir: File? = null

        constructor(fileName: String, data: ByteArray, cacheDir: File) : this() {
            this.fileName = fileName
            this.data = data
            this.cacheDir = cacheDir
        }

        override fun onCreate(): Boolean = true

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?,
        ): Cursor {
            val cursor = MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME))
            cursor.addRow(arrayOf(instance?.fileName ?: fileName))
            return cursor
        }

        override fun openFile(
            uri: Uri,
            mode: String,
        ): ParcelFileDescriptor? {
            val file = File(instance?.cacheDir ?: cacheDir, "temp_provider_file.pdf")
            FileOutputStream(file).use { it.write(instance?.data ?: data) }
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }

        override fun getType(uri: Uri): String? = null

        override fun insert(
            uri: Uri,
            values: ContentValues?,
        ): Uri? = null

        override fun delete(
            uri: Uri,
            selection: String?,
            selectionArgs: Array<out String>?,
        ): Int = 0

        override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?,
        ): Int = 0
    }
}
