package com.allubie.nana.core

import kotlinx.datetime.Clock
import java.util.UUID

/** Simple abstraction layer for generating timestamps and IDs to ease testing. */
interface TimeProvider { fun now() = Clock.System.now() }
object SystemTimeProvider : TimeProvider

interface IdProvider { fun newId(): String = UUID.randomUUID().toString() }
object UuidProvider : IdProvider
