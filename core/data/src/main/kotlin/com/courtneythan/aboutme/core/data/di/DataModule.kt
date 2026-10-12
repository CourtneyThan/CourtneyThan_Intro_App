package com.courtneythan.aboutme.core.data.di

import com.courtneythan.aboutme.core.data.AssetProfileSource
import com.courtneythan.aboutme.core.data.BundledProfileSource
import com.courtneythan.aboutme.core.data.FileProfileCache
import com.courtneythan.aboutme.core.data.IoDispatcher
import com.courtneythan.aboutme.core.data.OfflineFirstProfileRepository
import com.courtneythan.aboutme.core.data.OkHttpRemoteProfileSource
import com.courtneythan.aboutme.core.data.ProfileCache
import com.courtneythan.aboutme.core.data.ProfileRepository
import com.courtneythan.aboutme.core.data.RemoteProfileSource
import com.courtneythan.aboutme.core.model.ProfileParser
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    abstract fun bindBundled(impl: AssetProfileSource): BundledProfileSource

    @Binds
    @Singleton
    abstract fun bindCache(impl: FileProfileCache): ProfileCache

    @Binds
    abstract fun bindRemote(impl: OkHttpRemoteProfileSource): RemoteProfileSource

    companion object {
        @Provides
        @Singleton
        fun provideParser(): ProfileParser = ProfileParser()

        @Provides
        @Singleton
        fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        @Provides
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }
}
