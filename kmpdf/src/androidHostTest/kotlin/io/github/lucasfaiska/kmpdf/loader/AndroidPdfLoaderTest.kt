package io.github.lucasfaiska.kmpdf.loader

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import androidx.test.core.app.ApplicationProvider
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidPdfLoaderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val client =
        HttpClient(MockEngine) {
            engine {
                addHandler { _ ->
                    respond(byteArrayOf(1, 2, 3), HttpStatusCode.OK)
                }
            }
        }
    private val loader = AndroidPdfLoader(context, client, Dispatchers.Unconfined)

    @Test
    fun `given valid local source when loading then it should return bytes from content resolver`() =
        runTest {
            val file = getSamplePdfFile()
            val uri = android.net.Uri.parse("file://${file.absolutePath}")
            val source = PdfSource.Local(uri.toString())

            shadowOf(context.contentResolver).registerInputStream(uri, FileInputStream(file))

            val result = loader.load(source)

            assertNotNull(result)
            assertTrue(result.isNotEmpty())
        }

    @Test
    fun `given asset source when loading then it should return bytes from assets`() =
        runTest {
            val assetPath = "sample.pdf"
            val source = PdfSource.Local("file:///android_asset/$assetPath")
            val expectedBytes = byteArrayOf(4, 5, 6)

            val mockAssets = mockk<AssetManager>()
            val mockContext = mockk<Context>()
            every { mockContext.assets } returns mockAssets
            every { mockAssets.open(assetPath) } returns ByteArrayInputStream(expectedBytes)

            val assetLoader = AndroidPdfLoader(mockContext, client, Dispatchers.Unconfined)
            val result = assetLoader.load(source)

            assertNotNull(result)
            assertTrue(result.contentEquals(expectedBytes))
        }

    @Test
    fun `given invalid local source when loading then it should throw exception`() =
        runTest {
            val source = PdfSource.Local("file:///invalid/path.pdf")

            assertFailsWith<IOException> {
                loader.load(source)
            }
        }

    @Test
    fun `given failing assets when loading then it should catch and throw exception`() =
        runTest {
            val assetPath = "missing.pdf"
            val source = PdfSource.Local("file:///android_asset/$assetPath")

            val mockAssets = mockk<AssetManager>()
            val mockContext = mockk<Context>()
            every { mockContext.assets } returns mockAssets
            every { mockAssets.open(assetPath) } throws IOException("Asset not found")

            val assetLoader = AndroidPdfLoader(mockContext, client, Dispatchers.Unconfined)
            assertFailsWith<IOException> {
                assetLoader.load(source)
            }
        }

    @Test
    fun `given failing content resolver when loading then it should catch and throw exception`() =
        runTest {
            val source = PdfSource.Local("file:///some/path.pdf")

            val mockResolver = mockk<ContentResolver>()
            val mockContext = mockk<Context>()
            every { mockContext.contentResolver } returns mockResolver
            every { mockResolver.openInputStream(any()) } throws RuntimeException("Resolver error")

            val resolverLoader = AndroidPdfLoader(mockContext, client, Dispatchers.Unconfined)
            assertFailsWith<RuntimeException> {
                resolverLoader.load(source)
            }
        }

    @Test
    fun `given content resolver returning null when loading then it should throw IllegalArgumentException`() =
        runTest {
            val source = PdfSource.Local("file:///some/path.pdf")

            val mockResolver = mockk<ContentResolver>()
            val mockContext = mockk<Context>()
            every { mockContext.contentResolver } returns mockResolver
            every { mockResolver.openInputStream(any()) } returns null

            val resolverLoader = AndroidPdfLoader(mockContext, client, Dispatchers.Unconfined)
            assertFailsWith<IllegalArgumentException> {
                resolverLoader.load(source)
            }
        }

    @Test
    fun `given url source when loading then it should return bytes from network client`() =
        runTest {
            val source = PdfSource.Url("https://example.com/test.pdf")
            val result = loader.load(source)

            assertNotNull(result)
            assertTrue(result.contentEquals(byteArrayOf(1, 2, 3)))
        }

    @Test
    fun `given default constructor when instantiated then it should not crash`() {
        val defaultLoader = AndroidPdfLoader(context)
        assertNotNull(defaultLoader)
    }

    private fun getSamplePdfFile(): File {
        val inputStream =
            javaClass.classLoader?.getResourceAsStream("sample.pdf")
                ?: throw IllegalStateException("sample.pdf not found in resources")
        val file = File(context.cacheDir, "test_loader_sample.pdf")
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        return file
    }
}
