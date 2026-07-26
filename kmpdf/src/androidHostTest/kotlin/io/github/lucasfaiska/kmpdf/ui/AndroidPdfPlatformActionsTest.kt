package io.github.lucasfaiska.kmpdf.ui

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboCursor
import java.io.ByteArrayInputStream
import java.io.File

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

        val targetIntent = nextActivity.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
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
        val targetIntent = nextActivity.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

        assertEquals(Intent.ACTION_SEND, targetIntent?.action)
        assertEquals(mockUri, targetIntent?.getParcelableExtra(Intent.EXTRA_STREAM))
        assertEquals("application/pdf", targetIntent?.type)
        val flags = targetIntent?.flags ?: 0
        assertTrue((flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
    }

    @Test
    fun `given asset source when sharing then it should copy to cache and share`() {
        val assetPath = "test_share.pdf"
        val source = PdfSource.Local("file:///android_asset/$assetPath")
        val content = "asset content"

        val mockAssets = mockk<AssetManager>()
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
        val uri = Uri.parse("content://authority/document.pdf")
        val source = PdfSource.Local(uri.toString())
        val content = "content uri data"

        val mockResolver = mockk<ContentResolver>()
        val mockContext = mockk<Context>(relaxed = true)
        every { mockContext.contentResolver } returns mockResolver
        every { mockContext.cacheDir } returns app.cacheDir

        @Suppress("DEPRECATION")
        val cursor = RoboCursor()
        cursor.setColumnNames(listOf(OpenableColumns.DISPLAY_NAME))
        cursor.setResults(arrayOf(arrayOf("real_name.pdf")))

        @Suppress("DEPRECATION")
        shadowOf(app.contentResolver).setCursor(uri, cursor)
        every { mockResolver.query(uri, any(), any(), any(), any()) } returns cursor
        every { mockResolver.openInputStream(uri) } returns ByteArrayInputStream(content.toByteArray())

        val contentActions = AndroidPdfPlatformActions(mockContext)
        contentActions.share(source)

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
        val targetIntent = nextActivity.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

        assertEquals(Intent.ACTION_SEND, targetIntent?.action)
        assertEquals("invalid-path", targetIntent?.getStringExtra(Intent.EXTRA_TEXT))
    }
}
