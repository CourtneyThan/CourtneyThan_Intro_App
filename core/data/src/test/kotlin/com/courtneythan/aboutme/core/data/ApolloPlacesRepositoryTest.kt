package com.courtneythan.aboutme.core.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.courtneythan.aboutme.core.model.Place
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Runs the real Apollo client against a local MockWebServer, so the generated query,
 * response parsing, normalized cache and mapping are all exercised together.
 */
class ApolloPlacesRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: ApolloPlacesRepository

    private val response = """
        {"data":{"countries":[
          {"__typename":"Country","code":"IT","name":"Italy","emoji":"🇮🇹","capital":"Rome","currency":"EUR",
           "continent":{"__typename":"Continent","name":"Europe"},
           "languages":[{"__typename":"Language","name":"Italian"}]},
          {"__typename":"Country","code":"JP","name":"Japan","emoji":"🇯🇵","capital":"Tokyo","currency":"JPY",
           "continent":{"__typename":"Continent","name":"Asia"},
           "languages":[{"__typename":"Language","name":"Japanese"}]}
        ]}}
    """.trimIndent()

    private fun ok() = MockResponse().setHeader("Content-Type", "application/json").setBody(response)

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val apollo = ApolloClient.Builder()
            .serverUrl(server.url("/graphql").toString())
            .normalizedCache(MemoryCacheFactory())
            .build()
        repository = ApolloPlacesRepository(apollo)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `maps countries and keeps the order from the JSON`() = runTest {
        server.enqueue(ok())

        val places = repository.places(listOf("JP", "IT")).toList().last()

        assertEquals(listOf("JP", "IT"), places.map { it.code })
        assertEquals(Place("JP", "Japan", "🇯🇵", "Tokyo", "JPY", "Asia", listOf("Japanese")), places.first())
    }

    @Test
    fun `sends the country codes as GraphQL variables`() = runTest {
        server.enqueue(ok())

        repository.places(listOf("JP", "IT")).toList()

        val body = server.takeRequest().body.readUtf8()
        assertTrue(body, body.contains("query Countries"))
        assertTrue(body, body.contains("\"codes\":[\"JP\",\"IT\"]"))
    }

    @Test
    fun `cached places still show when the network fails`() = runTest {
        server.enqueue(ok())
        repository.places(listOf("JP", "IT")).toList() // fills the normalized cache

        server.enqueue(MockResponse().setResponseCode(500))
        val emissions = repository.places(listOf("JP", "IT")).toList()

        assertEquals(1, emissions.size) // the cached copy; the failed refresh is not an error
        assertEquals(listOf("JP", "IT"), emissions.single().map { it.code })
    }

    @Test(expected = IOException::class)
    fun `no cache and no network is an error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        repository.places(listOf("JP")).toList()
    }
}
