package io.github.lucasfaiska.kmpdf.loader

import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SharedPdfLoaderTest {
    @Test
    fun `given a URL source when loading then it should return bytes from HttpClient`() =
        runTest {
            // Given
            val expectedBytes = byteArrayOf(1, 2, 3)
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = expectedBytes,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/pdf"),
                    )
                }
            val httpClient = HttpClient(mockEngine)
            val loader =
                object : SharedPdfLoader(httpClient) {
                    override suspend fun loadLocal(identifier: String): ByteArray = byteArrayOf()
                }
            val source = PdfSource.Url("https://example.com/sample.pdf")

            // When
            val result = loader.load(source)

            // Then
            assertContentEquals(expectedBytes, result)
        }

    @Test
    fun `given a local source when loading then it should delegate to loadLocal`() =
        runTest {
            // Given
            val expectedBytes = byteArrayOf(4, 5, 6)
            val httpClient = HttpClient(MockEngine { respond("") })
            val loader =
                object : SharedPdfLoader(httpClient) {
                    override suspend fun loadLocal(identifier: String): ByteArray {
                        assertEquals("my-local-file", identifier)
                        return expectedBytes
                    }
                }
            val source = PdfSource.Local("my-local-file")

            // When
            val result = loader.load(source)

            // Then
            assertContentEquals(expectedBytes, result)
        }
}
