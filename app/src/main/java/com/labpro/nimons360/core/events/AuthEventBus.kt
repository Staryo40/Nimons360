package com.labpro.nimons360.core.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * App-wide event bus for authentication lifecycle.
 *
 * Usage:
 *  - Emit from [AuthInterceptor] when the server returns 409 (token expired).
 *  - Emit from [AuthRepository.logout] on explicit sign-out.
 *  - Collect in [MainActivity] to redirect to [LoginActivity].
 *
 * [MutableSharedFlow] with replay = 0 ensures each event is only consumed once
 * and is not re-delivered to late subscribers (e.g. after screen rotation).
 */
object AuthEventBus {

    private val _events = MutableSharedFlow<AuthEvent>(
        extraBufferCapacity = 1, // prevents suspension when no collector is ready
    )

    val events = _events.asSharedFlow()

    fun tryEmitSessionExpired() {
        _events.tryEmit(AuthEvent.SessionExpired)
    }

    fun tryEmitLoggedOut() {
        _events.tryEmit(AuthEvent.LoggedOut)
    }

    /**
     * Emit a [AuthEvent.SessionExpired] event (called from interceptor).
     * Safe to call from any coroutine or from [kotlinx.coroutines.runBlocking].
     */
    suspend fun emitSessionExpired() {
        _events.emit(AuthEvent.SessionExpired)
    }


    /**
     * Emit a [AuthEvent.LoggedOut] event (called on explicit sign-out).
     */
    suspend fun emitLoggedOut() {
        _events.emit(AuthEvent.LoggedOut)
    }
}

sealed class AuthEvent {
    /** The server returned 409 / 401 — token is no longer valid. */
    object SessionExpired : AuthEvent()

    /** The user tapped "Sign Out" explicitly. */
    object LoggedOut : AuthEvent()
}