package com.labpro.nimons360

import android.app.Application
import com.labpro.nimons360.core.utils.TokenManager
import com.labpro.nimons360.data.remote.RetrofitClient
import com.labpro.nimons360.data.repository.AuthRepository
import com.labpro.nimons360.data.repository.UserRepository

/**
 * Application class — acts as a lightweight dependency container.
 *
 * Responsibilities:
 *  - Initialize singleton-like app-wide dependencies
 *  - Provide AuthRepository to ViewModels
 */
class MainApplication : Application() {

    lateinit var tokenManager: TokenManager
        private set

    val authRepository: AuthRepository by lazy {
        AuthRepository(tokenManager)
    }

    val userRepository: UserRepository by lazy {
        UserRepository()
    }

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(applicationContext)
        RetrofitClient.initialize(tokenManager)
    }
}