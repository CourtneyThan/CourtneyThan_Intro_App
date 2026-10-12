package com.courtneythan.aboutme.di

import com.courtneythan.aboutme.BuildConfig
import com.courtneythan.aboutme.core.data.PEXELS_API_KEY
import com.courtneythan.aboutme.core.data.PROFILE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /** Remote content URL, set in app/build.gradle.kts. Blank = bundled content only. */
    @Provides
    @Named(PROFILE_URL)
    fun provideProfileUrl(): String = BuildConfig.PROFILE_URL

    /** Pexels key from local.properties (blank = photos section shows "add a key"). */
    @Provides
    @Named(PEXELS_API_KEY)
    fun providePexelsKey(): String = BuildConfig.PEXELS_API_KEY
}
