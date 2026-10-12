package com.courtneythan.aboutme.core.data

import javax.inject.Qualifier

/** Injected so tests can swap in a test dispatcher instead of Dispatchers.IO. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
