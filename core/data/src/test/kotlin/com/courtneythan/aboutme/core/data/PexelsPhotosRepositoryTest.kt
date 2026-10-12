package com.courtneythan.aboutme.core.data

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

/** Runs the real repository (OkHttp + JSON parsing) against a local MockWebServer. */
class PexelsPhotosRepositoryTest {

    private lateinit var server: MockWebServer

    private val body = """
        {"page":1,"per_page":2,"photos":[
          {"id":101,"width":4000,"height":3000,"url":"https://www.pexels.com/photo/ha-long-101/",
           "photographer":"Linh Tran","photographer_url":"https://www.pexels.com/@linh","photographer_id":7,
           "avg_color":"#6E8B87","src":{"original":"o","large":"https://images.pexels.com/101-large.jpg","small":"s"},
           "liked":false,"alt":"Boats in Ha Long Bay"},
          {"id":102,"url":"https://www.pexels.com/photo/hoi-an-102/",
           "photographer":"Minh Nguyen","photographer_url":"https://www.pexels.com/@minh",
           "src":{"large":"https://images.pexels.com/102-large.jpg"},"alt":""}
        ],"total_results":2}
    """.trimIndent()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() = server.shutdown()

    private fun TestScope.repository(key: String = "test-key") = PexelsPhotosRepository(
        client = OkHttpClient(),
        apiKey = key,
        baseUrl = server.url("/").toString(),
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun `maps Pexels photos to the app model`() = runTest {
        server.enqueue(MockResponse().setBody(body))

        val photos = repository().photos("Vietnam", 2)

        assertEquals(listOf(101L, 102L), photos.map { it.id })
        val first = photos.first()
        assertEquals("https://images.pexels.com/101-large.jpg", first.imageUrl)
        assertEquals("Linh Tran", first.photographer)
        assertEquals("#6E8B87", first.averageColor)
        assertEquals("Boats in Ha Long Bay", first.alt)
        assertEquals("Photo by Minh Nguyen", photos[1].alt) // blank alt gets a fallback
    }

    @Test
    fun `sends the key and search parameters`() = runTest {
        server.enqueue(MockResponse().setBody(body))

        repository().photos("Vietnam", 6)

        val request = server.takeRequest()
        assertEquals("test-key", request.getHeader("Authorization"))
        assertEquals("/v1/search", request.requestUrl?.encodedPath)
        assertEquals("Vietnam", request.requestUrl?.queryParameter("query"))
        assertEquals("6", request.requestUrl?.queryParameter("per_page"))
        assertEquals("landscape", request.requestUrl?.queryParameter("orientation"))
    }

    @Test
    fun `second call for the same query uses the memory cache`() = runTest {
        server.enqueue(MockResponse().setBody(body))
        val repository = repository()

        repository.photos("Vietnam", 2)
        repository.photos("Vietnam", 2)

        assertEquals(1, server.requestCount)
    }

    @Test(expected = MissingApiKeyException::class)
    fun `blank key fails fast without a network call`() = runTest {
        repository(key = "").photos("Vietnam", 2)
    }

    @Test(expected = IOException::class)
    fun `HTTP errors become IOException`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429))
        repository().photos("Vietnam", 2)
    }

    private fun single(id: Long, photographer: String) = """
        {"id":$id,"url":"https://www.pexels.com/photo/$id/","photographer":"$photographer",
         "photographer_url":"https://www.pexels.com/@p$id","src":{"large":"https://images.pexels.com/$id.jpg"},"alt":"Photo $id"}
    """.trimIndent()

    /** Answers /v1/photos/{id} by path, so parallel requests can arrive in any order. */
    private fun serveById(vararg found: Pair<Long, String>) {
        val byPath = found.associate { (id, name) -> "/v1/photos/$id" to single(id, name) }
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                byPath[request.path]?.let { MockResponse().setBody(it) } ?: MockResponse().setResponseCode(404)
        }
    }

    @Test
    fun `photos by id keep the requested order`() = runTest {
        serveById(1L to "A", 2L to "B", 3L to "C")

        val photos = repository().photosById(listOf(3L, 1L, 2L))

        assertEquals(listOf(3L, 1L, 2L), photos.map { it.id })
        assertEquals("https://images.pexels.com/3.jpg", photos.first().imageUrl)
    }

    @Test
    fun `a missing id is skipped, the rest still show`() = runTest {
        serveById(1L to "A", 3L to "C")

        val photos = repository().photosById(listOf(1L, 2L, 3L))

        assertEquals(listOf(1L, 3L), photos.map { it.id })
    }

    @Test(expected = IOException::class)
    fun `all ids failing is an error`() = runTest {
        serveById()
        repository().photosById(listOf(1L, 2L))
    }
}
