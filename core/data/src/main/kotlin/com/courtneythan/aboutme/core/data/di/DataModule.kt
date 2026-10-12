package com.courtneythan.aboutme.core.data.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.network.okHttpClient
import com.courtneythan.aboutme.core.data.ApolloPlacesRepository
import com.courtneythan.aboutme.core.data.AssetProfileSource
import com.courtneythan.aboutme.core.data.BundledProfileSource
import com.courtneythan.aboutme.core.data.FileProfileCache
import com.courtneythan.aboutme.core.data.IoDispatcher
import com.courtneythan.aboutme.core.data.OfflineFirstProfileRepository
import com.courtneythan.aboutme.core.data.OkHttpRemoteProfileSource
import com.courtneythan.aboutme.core.data.PlacesRepository
import com.courtneythan.aboutme.core.data.ProfileCache
import com.courtneythan.aboutme.core.data.ProfileRepository
import com.courtneythan.aboutme.core.data.RemoteProfileSource
import com.courtneythan.aboutme.core.model.ProfileParser
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindRepository(impl: OfflineFirstProfileRepository): ProfileRepository

    @Binds
    abstract fun bindPlaces(impl: ApolloPlacesRepository): PlacesRepository

    @Binds
    abstract fun bindBundled(impl: AssetProfileSource): BundledProfileSource

    @Binds
    @Singleton
    abstract fun bindCache(impl: FileProfileCache): ProfileCache

    @Binds
    abstract fun bindRemote(impl: OkHttpRemoteProfileSource): RemoteProfileSource

    companion object {
        private const val COUNTRIES_URL = "https://countries.trevorblades.com/graphql"

        @Provides
        @Singleton
        fun provideParser(): ProfileParser = ProfileParser()

        @Provides
        @Singleton
        fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        /**
         * One Apollo client for the app. Two cache layers: memory for speed, SQLite so
         * places still show after the app restarts offline. Shares the app's OkHttpClient
         * (one connection pool, one place to add interceptors).
         */
        @Provides
        @Singleton
        fun provideApollo(@ApplicationContext context: Context, okHttp: OkHttpClient): ApolloClient =
            ApolloClient.Builder()
                .serverUrl(COUNTRIES_URL)
                .okHttpClient(okHttp)
                .normalizedCache(
                    MemoryCacheFactory(maxSizeBytes = 5 * 1024 * 1024)
                        .chain(SqlNormalizedCacheFactory(context, "places.db")),
                )
                .build()

        @Provides
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }
}
