package io.github.lucasfaiska.kmpdf.loader

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidPdfLoaderTest {
    private lateinit var context: Context
    private lateinit var loader: AndroidPdfLoader

    @BeforeTest
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        loader =
            AndroidPdfLoader(
                context = context,
                httpClient = HttpClient(MockEngine { respond("") }),
                dispatcher = Dispatchers.Unconfined,
            )
    }

    @Test
    fun `given a local file URI when loading then it should return file bytes`() =
        runTest {
            // Given
            val tempFile = File.createTempFile("test", ".pdf")
            val expectedBytes = byteArrayOf(10, 20, 30)
            tempFile.writeBytes(expectedBytes)
            val uriString = Uri.fromFile(tempFile).toString()

            // When
            val result = (loader as SharedPdfLoader).load(PdfSource.Local(uriString))

            // Then
            assertContentEquals(expectedBytes, result)
            tempFile.delete()
        }
}
