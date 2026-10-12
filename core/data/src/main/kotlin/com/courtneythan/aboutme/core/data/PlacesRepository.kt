package com.courtneythan.aboutme.core.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.courtneythan.aboutme.core.data.graphql.CountriesQuery
import com.courtneythan.aboutme.core.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

interface PlacesRepository {
    /**
     * Emits the countries for [codes], in the same order as [codes].
     * Cached data comes first (if any), then fresh data from the network.
     * Fails only when there is nothing to show: no cache and no network.
     */
    fun places(codes: List<String>): Flow<List<Place>>
}

/**
 * Loads countries from the Countries GraphQL API with Apollo.
 *
 * `CacheAndNetwork` gives the same stale-while-revalidate behavior as the profile
 * content: the normalized cache answers instantly (even offline), then the network
 * refreshes it. See docs/adr/0004-layout-json-data-graphql.md.
 */
internal class ApolloPlacesRepository @Inject constructor(
    private val apollo: ApolloClient,
) : PlacesRepository {

    override fun places(codes: List<String>): Flow<List<Place>> = flow {
        var emittedAny = false
        apollo.query(CountriesQuery(codes = codes))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .toFlow()
            .collect { response ->
                // Apollo 4 reports errors on the response instead of throwing:
                // a cache miss or a network failure arrives with data == null.
                val countries = response.data?.countries ?: return@collect
                emit(countries.toPlaces(order = codes))
                emittedAny = true
            }
        if (!emittedAny) throw IOException("Couldn't load places: no cached data and no network")
    }
}

/** Maps Apollo's generated models to [Place], keeping the order the JSON asked for. */
internal fun List<CountriesQuery.Country>.toPlaces(order: List<String>): List<Place> =
    map { country ->
        Place(
            code = country.code,
            name = country.name,
            flag = country.emoji,
            capital = country.capital,
            currency = country.currency,
            continent = country.continent.name,
            languages = country.languages.map { it.name },
        )
    }.sortedBy { place -> order.indexOf(place.code).let { if (it == -1) Int.MAX_VALUE else it } }
