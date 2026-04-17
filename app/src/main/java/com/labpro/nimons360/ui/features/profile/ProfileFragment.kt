package com.labpro.nimons360.ui.features.profile

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.viewmodel.ProfileViewModel
import com.labpro.nimons360.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch

class ProfileFragment : DialogFragment() {

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ProfileViewModelFactory(
                (requireActivity().application as MainApplication).userRepository,
                (requireActivity().application as MainApplication).authRepository
            )
        )[ProfileViewModel::class.java]
    }

    private var tvFullName: TextView? = null
    private var tvEmail: TextView? = null
    private var tvAvatarInitials: TextView? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Nimons360_ProfileDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val appBar = view.findViewById<View>(R.id.appBarLayout)
//        ViewCompat.setOnApplyWindowInsetsListener(appBar) { v, insets ->
//            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
//            v.setPadding(
//                v.paddingLeft,
//                statusBarInsets.top,
//                v.paddingRight,
//                v.paddingBottom
//            )
//            insets
//        }

        tvFullName = view.findViewById(R.id.tvFullName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }

        setupListeners(view)
        observeState()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnSignOut).setOnClickListener {
            viewModel.logout()
        }

        view.findViewById<View>(R.id.btnEditName).setOnClickListener {
            showEditNameBottomSheet()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->

                tvFullName?.text = state.name
                tvEmail?.text = state.email

                tvAvatarInitials?.text =
                    state.name.firstOrNull()?.uppercase() ?: ""

                if (state.isLoggedOut) {
                    viewModel.logout()
                }
            }
        }
    }

    private fun showEditNameBottomSheet() {
        EditNameBottomSheet().show(childFragmentManager, EditNameBottomSheet.TAG)
    }

    companion object {
        const val TAG = "ProfileFragment"
    }
}