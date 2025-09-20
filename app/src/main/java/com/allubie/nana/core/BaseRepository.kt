package com.allubie.nana.core

/** Base class to share common providers across repositories (test friendly). */
open class BaseRepository(
    protected val timeProvider: TimeProvider = SystemTimeProvider,
    protected val idProvider: IdProvider = UuidProvider,
)
