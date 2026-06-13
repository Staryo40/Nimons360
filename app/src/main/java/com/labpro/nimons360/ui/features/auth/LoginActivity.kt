package com.labpro.nimons360.ui.features.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.labpro.nimons360.MainActivity
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.core.navigation.FamilyDeepLink
import com.labpro.nimons360.viewmodel.AuthViewModel
import com.labpro.nimons360.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

/**
 * Login screen — one of the 4 mandatory XML-based pages.
 *
 * Responsibilities:
 *  - Render email / password form.
 *  - Delegate login to [AuthViewModel].
 *  - Auto-redirect to [MainActivity] if already logged in.
 *  - Navigate to [MainActivity] on successful login.
 */
class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((application as MainApplication).authRepository)
    }

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignIn: Button
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvError: TextView

    private var pendingFamilyDeepLink: FamilyDeepLink? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingFamilyDeepLink = FamilyDeepLink.fromIntent(intent)

        setContentView(R.layout.activity_login)
        bindViews()
        setupListeners()
        observeState()
    }

    private fun bindViews() {
        tilEmail   = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail    = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn  = findViewById(R.id.btnSignIn)
        pbLoading  = findViewById(R.id.pbLoading)
        tvError    = findViewById(R.id.tvError)
    }

    private fun setupListeners() {
        btnSignIn.setOnClickListener { attemptLogin() }

        etEmail.setOnFocusChangeListener { _, _ ->
            tilEmail.error = null
        }

        etPassword.setOnFocusChangeListener { _, _ ->
            tilPassword.error = null
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->

                // Loading state
                pbLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                btnSignIn.isEnabled  = !state.isLoading

                // Navigation (ONLY from ViewModel trigger)
                if (state.navigateToMain) {
                    navigateToMain()
                    viewModel.onNavigated()
                    return@collect
                }

                // Error handling
                if (state.errorMessage != null) {
                    tvError.text = state.errorMessage
                    tvError.visibility = View.VISIBLE
                } else {
                    tvError.visibility = View.GONE
                }
            }
        }
    }

    private fun attemptLogin() {
        val email    = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()

        var isValid = true

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.error_empty_fields)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        }

        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_empty_fields)
            isValid = false
        }

        if (!isValid) return

        viewModel.clearError()
        viewModel.login(email, password)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            pendingFamilyDeepLink?.let {
                putExtra(FamilyDeepLink.EXTRA_URI, it.toUriString())
            }
        }
        startActivity(intent)
        finish()
    }
}
