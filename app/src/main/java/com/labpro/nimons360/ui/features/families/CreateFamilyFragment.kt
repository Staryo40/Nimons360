package com.labpro.nimons360.ui.features.families

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.ui_state.CreateFamilyUiState
import com.labpro.nimons360.viewmodel.FAMILY_ICON_URLS
import com.labpro.nimons360.viewmodel.CreateFamilyViewModel
import com.labpro.nimons360.viewmodel.CreateFamilyViewModelFactory
import kotlinx.coroutines.launch

/**
 * Fullscreen DialogFragment for creating a new family.
 * Inflates [R.layout.fragment_create_family] (XML).
 *
 * Show from any Fragment or Composable that has access to a [FragmentManager]:
 *   CreateFamilyFragment().show(parentFragmentManager, CreateFamilyFragment.TAG)
 *
 * After successful creation this fragment dismisses itself and immediately
 * shows [FamilyDetailFragment] for the newly created family.
 */
class CreateFamilyFragment : DialogFragment() {

    private val viewModel: CreateFamilyViewModel by viewModels {
        CreateFamilyViewModelFactory(
            (requireActivity().application as MainApplication).familyRepository
        )
    }


    private lateinit var btnCancel: MaterialButton
    private lateinit var btnCreate: MaterialButton
    private lateinit var ivSelectedIcon: ImageView
    private lateinit var tilFamilyName: TextInputLayout
    private lateinit var etFamilyName: TextInputEditText
    private lateinit var tvError: TextView
    private lateinit var loadingOverlay: FrameLayout

    private lateinit var iconContainers: List<FrameLayout>
    private lateinit var iconImageViews: List<ImageView>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Nimons360)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_create_family, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBar = view.findViewById<View>(R.id.appBarLayout)
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(
                v.paddingLeft,
                statusBarInsets.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        bindViews(view)
        makeIconsSquare()
        loadIconImages()
        setupListeners()
        observeState()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
    }

    // === onViewCreated methods ===

    private fun bindViews(root: View) {
        btnCancel      = root.findViewById(R.id.btnCancel)
        btnCreate      = root.findViewById(R.id.btnCreate)
        ivSelectedIcon = root.findViewById(R.id.ivSelectedIcon)
        tilFamilyName  = root.findViewById(R.id.tilFamilyName)
        etFamilyName   = root.findViewById(R.id.etFamilyName)
        tvError        = root.findViewById(R.id.tvError)
        loadingOverlay = root.findViewById(R.id.loadingOverlay)

        iconContainers = listOf(
            root.findViewById(R.id.iconContainer1),
            root.findViewById(R.id.iconContainer2),
            root.findViewById(R.id.iconContainer3),
            root.findViewById(R.id.iconContainer4),
            root.findViewById(R.id.iconContainer5),
            root.findViewById(R.id.iconContainer6),
            root.findViewById(R.id.iconContainer7),
            root.findViewById(R.id.iconContainer8),
        )
        iconImageViews = listOf(
            root.findViewById(R.id.ivIcon1),
            root.findViewById(R.id.ivIcon2),
            root.findViewById(R.id.ivIcon3),
            root.findViewById(R.id.ivIcon4),
            root.findViewById(R.id.ivIcon5),
            root.findViewById(R.id.ivIcon6),
            root.findViewById(R.id.ivIcon7),
            root.findViewById(R.id.ivIcon8),
        )
    }

    private fun makeIconsSquare() {
        iconContainers.forEach { container ->
            container.post {
                val params = container.layoutParams
                params.height = container.width
                container.layoutParams = params
            }
        }
    }

    private fun loadIconImages() {
        iconImageViews.forEachIndexed { index, imageView ->
            imageView.load(FAMILY_ICON_URLS[index]) {
                crossfade(true)
            }
        }
        ivSelectedIcon.load(FAMILY_ICON_URLS[0]) { crossfade(true) }
    }

    private fun setupListeners() {
        btnCancel.setOnClickListener { dismiss() }
        btnCreate.setOnClickListener {
            viewModel.setFamilyName(etFamilyName.text?.toString().orEmpty())
            viewModel.create()
        }

        etFamilyName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tilFamilyName.error = null
                viewModel.setFamilyName(s?.toString().orEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        iconContainers.forEachIndexed { index, container ->
            container.setOnClickListener { viewModel.selectIcon(index) }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    // Rendering

    private fun render(state: CreateFamilyUiState) {
        loadingOverlay.isVisible = state.isLoading
        btnCreate.isEnabled = !state.isLoading

        iconContainers.forEachIndexed { index, container ->
            val isSelected = index == state.selectedIconIndex
            container.isSelected = isSelected
            container.background = container.background?.apply { jumpToCurrentState() }
                ?: container.background
        }

        if (state.selectedIconIndex in FAMILY_ICON_URLS.indices) {
            ivSelectedIcon.load(FAMILY_ICON_URLS[state.selectedIconIndex]) { crossfade(false) }
            ivSelectedIcon.isSelected = true
        }

        if (state.error != null && state.familyName.isBlank()) {
            tilFamilyName.error = state.error
            tvError.isVisible = false
        } else {
            tilFamilyName.error = null
            tvError.isVisible = state.error != null
            tvError.text = state.error
        }

        if (state.navigateToFamilyId != null) {
            viewModel.onNavigated()
            navigateToDetail(state.navigateToFamilyId)
        }
    }

    private fun navigateToDetail(familyId: Int) {
        dismiss()
        FamilyDetailFragment
            .newInstance(familyId)
            .show(parentFragmentManager, FamilyDetailFragment.TAG)
    }

    companion object {
        const val TAG = "CreateFamilyFragment"
    }
}
