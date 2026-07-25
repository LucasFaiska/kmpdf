package io.github.lucasfaiska.kmpdf.loader

import androidx.test.core.app.ApplicationProvider
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidPdfLoaderTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
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
    fun `given url source when loading then it should return bytes from network client`() =
        runTest {
            val source = PdfSource.Url("https://example.com/test.pdf")
            val result = loader.load(source)

            assertNotNull(result)
            assertTrue(result.contentEquals(byteArrayOf(1, 2, 3)))
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
