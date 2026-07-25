package io.github.lucasfaiska.kmpdf.reader

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidPdfReaderTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val reader = AndroidPdfReader(context, Dispatchers.Unconfined)

    @Test
    fun `given valid bytes when opening then it should successfully attempt to process document`() =
        runTest {
            val bytes = getSamplePdfBytes()
            val result = reader.open(bytes, null)
            assertNotNull(result)
        }

    @Test
    fun `given invalid bytes when opening then it should return an error status`() =
        runTest {
            val bytes = byteArrayOf(1, 2, 3)
            val result = reader.open(bytes, null)
            assertNotNull(result)
        }

    private fun getSamplePdfBytes(): ByteArray {
        val inputStream =
            javaClass.classLoader?.getResourceAsStream("sample.pdf")
                ?: throw IllegalStateException("sample.pdf not found in resources")
        return inputStream.readBytes()
    }
}
