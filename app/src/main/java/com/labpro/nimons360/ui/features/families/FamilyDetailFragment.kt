package com.labpro.nimons360.ui.features.families

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.core.utils.applyStatusBarHeaderInset
import com.labpro.nimons360.core.navigation.FamilyDeepLink
import com.labpro.nimons360.data.model.family.FamilyDetail
import com.labpro.nimons360.data.model.family.FamilyMember
import com.labpro.nimons360.data.model.ui_state.FamilyDetailUiState
import com.labpro.nimons360.ui.features.live.LiveActivity
import com.labpro.nimons360.viewmodel.FamilyDetailViewModel
import com.labpro.nimons360.viewmodel.FamilyDetailViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Fullscreen DialogFragment showing family detail.
 * Inflates [R.layout.fragment_family_detail] (XML).
 *
 * Navigation from Compose / another Fragment:
 *   FamilyDetailFragment.newInstance(familyId)
 *       .show(supportFragmentManager, FamilyDetailFragment.TAG)
 */
class FamilyDetailFragment : DialogFragment() {

    private val familyId: Int by lazy {
        requireArguments().getInt(ARG_FAMILY_ID)
    }
    private val currentUserEmail: String by lazy {
        requireArguments().getString(ARG_CURRENT_USER_EMAIL, "")
    }
    private val prefillCode: String? by lazy {
        requireArguments().getString(ARG_PREFILL_CODE)
    }

    private val viewModel: FamilyDetailViewModel by viewModels {
        FamilyDetailViewModelFactory(
            familyId   = familyId,
            repository = (requireActivity().application as MainApplication).familyRepository,
        )
    }

    private lateinit var toolbar: Toolbar
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var ivFamilyIcon: android.widget.ImageView
    private lateinit var tvFamilyName: TextView
    private lateinit var tvFamilyMeta: TextView
    private lateinit var tvNotMemberBadge: TextView
    private lateinit var familyCodeSection: LinearLayout
    private lateinit var tvFamilyCode: TextView
    private lateinit var btnCopyCode: ImageButton
    private lateinit var actionsSection: LinearLayout
    private lateinit var btnSendMessage: LinearLayout
    private lateinit var btnShareFamily: LinearLayout
    private lateinit var btnShareFamilyQr: LinearLayout
    private lateinit var dividerShareFamilyQr: View
    private lateinit var membersCard: MaterialCardView
    private lateinit var membersContainer: LinearLayout
    private lateinit var joinHintSection: LinearLayout
    private lateinit var btnAction: MaterialButton
    private lateinit var pbAction: ProgressBar
    private lateinit var tvActionError: TextView
    private lateinit var tilMemberSearch: com.google.android.material.textfield.TextInputLayout
    private lateinit var etMemberSearch: com.google.android.material.textfield.TextInputEditText
    private lateinit var membersScrollView: androidx.core.widget.NestedScrollView

    private var allMembers: List<FamilyMember> = emptyList()
    private var isCensored: Boolean = false
    private var currentSearchQuery: String = ""

    private var btnLive: MaterialButton? = null

    private val avatarColors: List<Int> by lazy {
        listOf(
            ContextCompat.getColor(requireContext(), R.color.primary_teal),
            ContextCompat.getColor(requireContext(), R.color.pin_red),
            ContextCompat.getColor(requireContext(), R.color.pin_green),
            ContextCompat.getColor(requireContext(), R.color.pin_blue),
            ContextCompat.getColor(requireContext(), R.color.pin_orange),
            ContextCompat.getColor(requireContext(), R.color.pin_purple),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Nimons360)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_family_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        val appBar = view.findViewById<View>(R.id.appBarLayout)
        appBar.applyStatusBarHeaderInset(extraTopDp = 0)

        setupToolbar()
        setupJoinDialogResultListener()
        setupSearchInput()
        observeState()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
    }

    private fun bindViews(root: View) {
        toolbar           = root.findViewById(R.id.toolbar)
        loadingOverlay    = root.findViewById(R.id.loadingOverlay)
        ivFamilyIcon      = root.findViewById(R.id.ivFamilyIcon)
        tvFamilyName      = root.findViewById(R.id.tvFamilyName)
        tvFamilyMeta      = root.findViewById(R.id.tvFamilyMeta)
        tvNotMemberBadge  = root.findViewById(R.id.tvNotMemberBadge)
        familyCodeSection = root.findViewById(R.id.familyCodeSection)
        tvFamilyCode      = root.findViewById(R.id.tvFamilyCode)
        btnCopyCode       = root.findViewById(R.id.btnCopyCode)
        actionsSection    = root.findViewById(R.id.actionsSection)
        btnSendMessage    = root.findViewById(R.id.btnSendMessage)
        btnShareFamily    = root.findViewById(R.id.btnShareFamily)
        btnShareFamilyQr  = root.findViewById(R.id.btnShareFamilyQr)
        dividerShareFamilyQr = root.findViewById(R.id.dividerShareFamilyQr)
        membersCard       = root.findViewById(R.id.membersCard)
        membersContainer  = root.findViewById(R.id.membersContainer)
        joinHintSection   = root.findViewById(R.id.joinHintSection)
        btnAction         = root.findViewById(R.id.btnAction)
        pbAction          = root.findViewById(R.id.pbAction)
        tvActionError     = root.findViewById(R.id.tvActionError)
        tilMemberSearch   = root.findViewById(R.id.tilMemberSearch)
        etMemberSearch    = root.findViewById(R.id.etMemberSearch)
        membersScrollView = root.findViewById(R.id.membersScrollView)
    }

    private fun setupToolbar() {
        toolbar.navigationContentDescription = getString(R.string.cd_back)
        toolbar.setNavigationOnClickListener { dismiss() }
    }

    /**
     * Listen for the result from [JoinFamilyDialog] shown via [childFragmentManager].
     * When the user confirms a valid code, delegate to the ViewModel.
     */
    private fun setupJoinDialogResultListener() {
        childFragmentManager.setFragmentResultListener(
            JoinFamilyDialog.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val code = bundle.getString(JoinFamilyDialog.KEY_CODE) ?: return@setFragmentResultListener
            viewModel.joinFamily(code)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: FamilyDetailUiState) {
        loadingOverlay.isVisible = state.isLoading && state.family == null

        if (state.error != null && state.family == null) {
            Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
            return
        }

        val family = state.family ?: return
        renderFamily(family)

        pbAction.isVisible      = state.isActionLoading
        btnAction.isEnabled     = !state.isActionLoading
        tvActionError.isVisible = state.actionError != null
        tvActionError.text      = state.actionError

        if (state.navigateBack) {
            viewModel.onNavigatedBack()
            dismiss()
            return
        }

        if (state.snackbarMessage != null) {
            Toast.makeText(requireContext(), state.snackbarMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearSnackbar()
        }
    }

    private fun renderFamily(family: FamilyDetail) {
        toolbar.title = family.name

        ivFamilyIcon.load(family.iconUrl) { crossfade(true) }
        ivFamilyIcon.contentDescription = getString(R.string.cd_discover_family_icon, family.name)
        tvFamilyName.text = family.name
        tvFamilyMeta.text = buildMeta(family)

        if (family.isMember) {
            tvNotMemberBadge.isVisible = false

            familyCodeSection.isVisible = true
            tvFamilyCode.text = family.familyCode ?: "------"
            btnCopyCode.setOnClickListener { copyCodeToClipboard(family.familyCode) }
            updateShareActions(family)

            injectLiveButton(family)

            membersCard.isVisible    = true
            joinHintSection.isVisible = false

            tilMemberSearch.isVisible = true
            allMembers = family.members
            isCensored = false
            filterAndRebuildMembers()

            btnAction.text = getString(R.string.leave_family)
            btnAction.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.danger_crimson))
            btnAction.setOnClickListener { confirmLeave(family.name) }
        } else {
            tvNotMemberBadge.isVisible = true

            familyCodeSection.isVisible = false
            actionsSection.isVisible = false
            toolbar.menu.clear()
            btnLive?.isVisible = false
            membersCard.isVisible       = false
            joinHintSection.isVisible   = true

            tilMemberSearch.isVisible = false
            allMembers = family.members
            isCensored = true
            filterAndRebuildMembers()

            btnAction.text = getString(R.string.join_family)
            btnAction.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_teal))
            btnAction.setOnClickListener { showJoinDialog() }
        }
    }

    private fun updateShareActions(family: FamilyDetail) {
        val canShare = family.familyCode != null
        actionsSection.isVisible = true
        btnSendMessage.setOnClickListener {
            openSendMessageBottomSheet(family)
        }
        btnShareFamily.isEnabled = canShare
        btnShareFamily.alpha = if (canShare) 1f else 0.5f
        btnShareFamily.setOnClickListener {
            if (canShare) shareFamilyLink(family)
        }
        btnShareFamilyQr.isVisible = canShare
        dividerShareFamilyQr.isVisible = canShare
        btnShareFamilyQr.setOnClickListener {
            if (canShare) showFamilyQrDialog(family)
        }

        toolbar.menu.clear()
        if (canShare) {
            toolbar.menu.add(R.string.share_family)
                .setIcon(R.drawable.ic_share)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            toolbar.setOnMenuItemClickListener {
                shareFamilyLink(family)
                true
            }
        }
    }

    private fun injectLiveButton(family: FamilyDetail) {
        if (btnLive == null) {
            btnLive = MaterialButton(requireContext()).apply {
                text = getString(R.string.live_room)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary_coral_dark))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                val params = LinearLayout.LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 0)
                layoutParams = params

                setOnClickListener {
                    showLiveOptionsDialog(family.id.toString())
                }
            }
            familyCodeSection.addView(btnLive)
        }
        btnLive?.isVisible = true
    }

    private fun showLiveOptionsDialog(roomId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.live_room_title)
            .setMessage(R.string.live_room_message)
            .setPositiveButton(R.string.live_room_start) { _, _ ->
                startActivity(LiveActivity.newIntent(requireContext(), roomId, true))
            }
            .setNegativeButton(R.string.live_room_watch) { _, _ ->
                startActivity(LiveActivity.newIntent(requireContext(), roomId, false))
            }
            .setNeutralButton(R.string.btn_cancel, null)
            .show()
    }

    private fun buildMemberRows(members: List<FamilyMember>) {
        membersContainer.removeAllViews()
        members.forEachIndexed { index, member ->
            val row = layoutInflater.inflate(
                R.layout.item_family_member,
                membersContainer,
                false,
            )
            bindMemberRow(row, member, index, isCurrentUser = member.email == currentUserEmail)
            membersContainer.addView(row)

            if (index < members.lastIndex) {
                membersContainer.addView(makeDivider())
            }
        }
        adjustMembersCardHeight()
    }

    private fun buildCensoredMemberRows(members: List<FamilyMember>) {
        membersContainer.removeAllViews()
        adjustMembersCardHeight()
    }

    private fun setupSearchInput() {
        etMemberSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString() ?: ""
                filterAndRebuildMembers()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        membersScrollView.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun filterAndRebuildMembers() {
        val query = currentSearchQuery.trim()
        val filtered = if (query.isEmpty()) {
            allMembers
        } else {
            allMembers.filter { member ->
                member.fullName.contains(query, ignoreCase = true) ||
                        member.email.contains(query, ignoreCase = true)
            }
        }

        if (isCensored) {
            buildCensoredMemberRows(filtered)
        } else {
            buildMemberRows(filtered)
        }
    }

    private fun adjustMembersCardHeight() {
        membersContainer.post {
            if (!isAdded) return@post
            val maxPx = resources.getDimensionPixelSize(R.dimen.family_members_max_height)
            val params = membersScrollView.layoutParams
            if (membersContainer.measuredHeight > maxPx) {
                params.height = maxPx
            } else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            membersScrollView.layoutParams = params
        }
    }

    private fun bindMemberRow(
        row: View,
        member: FamilyMember,
        colorIndex: Int,
        isCurrentUser: Boolean,
    ) {
        val initial = member.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        val color   = avatarColors[colorIndex % avatarColors.size]

        val avatarBg = row.findViewById<View>(R.id.avatarBg)
        avatarBg.background?.mutate()?.setTint(color)

        row.findViewById<TextView>(R.id.tvAvatarInitial).text  = initial
        row.findViewById<TextView>(R.id.tvMemberName).text     = member.fullName
        row.findViewById<TextView>(R.id.tvMemberEmail).text    = member.email
        row.findViewById<TextView>(R.id.tvYouBadge).isVisible  = isCurrentUser

        val ivMemberAvatar = row.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivMemberAvatar)
        if (!member.profileImageUrl.isNullOrBlank()) {
            ivMemberAvatar.visibility = View.VISIBLE
            val resolvedUrl = if (member.profileImageUrl.startsWith("/")) {
                "${com.labpro.nimons360.BuildConfig.BASE_URL}${member.profileImageUrl}"
            } else {
                member.profileImageUrl
            }
            ivMemberAvatar.load(resolvedUrl) {
                crossfade(true)
            }
        } else {
            ivMemberAvatar.visibility = View.GONE
        }

        row.contentDescription = getString(
            R.string.member_row_description,
            member.fullName,
            member.email,
            if (isCurrentUser) ", ${getString(R.string.member_you)}" else "",
        )
    }

    private fun makeDivider(): View {
        val divider = View(requireContext())
        divider.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 1).apply {
            setMargins(
                resources.getDimensionPixelSize(R.dimen.spacing_xl),
                0, 0, 0,
            )
        }
        divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider))
        return divider
    }

    private fun showJoinDialog() {
        JoinFamilyDialog.newInstance(prefillCode)
            .show(childFragmentManager, JoinFamilyDialog.TAG)
    }

    private fun shareFamilyLink(family: FamilyDetail) {
        val code = family.familyCode ?: return
        val link = FamilyDeepLink(family.id, code).toUriString()
        val message = getString(R.string.share_family_message, family.name, link)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_family_chooser)))
    }

    private fun showFamilyQrDialog(family: FamilyDetail) {
        val code = family.familyCode ?: return
        if (childFragmentManager.findFragmentByTag(FamilyQrDialogFragment.TAG) != null) return

        FamilyQrDialogFragment
            .newInstance(
                familyId = family.id,
                familyName = family.name,
                familyCode = code,
            )
            .show(childFragmentManager, FamilyQrDialogFragment.TAG)
    }

    private fun confirmLeave(familyName: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_not_member)
            .setTitle(R.string.leave_family)
            .setMessage(getString(R.string.leave_confirmation, familyName))
            .setPositiveButton(R.string.btn_leave) { _, _ -> viewModel.leaveFamily() }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                        ?.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.danger_crimson)
                        )
                }
            }
            .show()
    }

    private fun openSendMessageBottomSheet(family: FamilyDetail) {
        SendMessageBottomSheet.newInstance(family.id, family.name)
            .show(childFragmentManager, SendMessageBottomSheet.TAG)
    }

    private fun copyCodeToClipboard(code: String?) {
        if (code == null) return
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.family_code_clipboard_label), code))
        Toast.makeText(requireContext(), getString(R.string.family_code_copied), Toast.LENGTH_SHORT).show()
    }

    private fun buildMeta(family: FamilyDetail): String {
        val pluralSuffix = if (family.members.size != 1) "s" else ""
        val date  = family.createdAt?.let { parseDate(it) } ?: ""
        return if (date.isNotEmpty()) {
            getString(R.string.family_meta_with_date, family.members.size, pluralSuffix, date)
        } else {
            getString(R.string.family_meta_without_date, family.members.size, pluralSuffix)
        }
    }

    private fun parseDate(iso: String): String = try {
        val inFmt  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val outFmt = SimpleDateFormat("MMM yyyy", Locale.US)
        outFmt.format(inFmt.parse(iso)!!)
    } catch (_: Exception) { "" }

    companion object {
        const val TAG                     = "FamilyDetailFragment"
        private const val ARG_FAMILY_ID          = "family_id"
        private const val ARG_CURRENT_USER_EMAIL = "current_user_email"
        private const val ARG_PREFILL_CODE       = "prefill_code"

        /**
         * @param familyId         the family to display.
         * @param currentUserEmail used to render the "You" badge on the current user's row.
         */
        fun newInstance(
            familyId: Int,
            currentUserEmail: String = "",
            prefillCode: String? = null,
        ) = FamilyDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_FAMILY_ID, familyId)
                putString(ARG_CURRENT_USER_EMAIL, currentUserEmail)
                putString(ARG_PREFILL_CODE, prefillCode)
            }
        }
    }
}
