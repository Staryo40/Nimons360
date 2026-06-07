package com.labpro.nimons360.ui.features.map

import android.util.Log
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.notification.SendGreetingRequest
import com.labpro.nimons360.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MemberDetailBottomSheet : BottomSheetDialogFragment() {

    private val targetUserId: Int by lazy { requireArguments().getInt(ARG_USER_ID) }
    private val memberName: String by lazy { requireArguments().getString(ARG_NAME, "") }
    private val memberEmail: String by lazy { requireArguments().getString(ARG_EMAIL, "") }
    private val latitude: Double by lazy { requireArguments().getDouble(ARG_LAT) }
    private val longitude: Double by lazy { requireArguments().getDouble(ARG_LON) }
    private val batteryLevel: Int by lazy { requireArguments().getInt(ARG_BATTERY, -1) }
    private val isCharging: Boolean? by lazy {
        if (requireArguments().containsKey(ARG_CHARGING)) {
            requireArguments().getBoolean(ARG_CHARGING)
        } else null
    }
    private val internetStatus: String by lazy { requireArguments().getString(ARG_NET, "Unknown") }

    private var resolvedFamilyId: Int? = null
    private var senderName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_member_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind circular initial fallback and name/email
        val tvAvatarInitial = view.findViewById<TextView>(R.id.tvAvatarInitial)
        val ivMemberAvatar = view.findViewById<ShapeableImageView>(R.id.ivMemberAvatar)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvActiveFamilyBadge = view.findViewById<TextView>(R.id.tvActiveFamilyBadge)

        tvName.text = memberName
        tvEmail.text = memberEmail
        tvAvatarInitial.text = memberName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        // Grid Status Elements
        val tvBattery = view.findViewById<TextView>(R.id.tvBattery)
        val tvLoc = view.findViewById<TextView>(R.id.tvLoc)
        val tvNet = view.findViewById<TextView>(R.id.tvNet)

        val batteryStr = if (batteryLevel >= 0) "$batteryLevel%" else getString(R.string.map_unknown)
        val chargeStatus = when (isCharging) {
            true -> getString(R.string.map_charging)
            false -> getString(R.string.map_not_charging)
            null -> getString(R.string.map_unknown)
        }
        tvBattery.text = if (batteryLevel >= 0) "$batteryStr ($chargeStatus)" else batteryStr

        tvLoc.text = String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
        tvNet.text = when (internetStatus.lowercase(Locale.US)) {
            "wifi" -> getString(R.string.map_internet_wifi)
            "mobile" -> getString(R.string.map_internet_mobile)
            else -> getString(R.string.map_unknown)
        }

        // Dynamic Time-Based Weather Greeting Setup
        val greetingData = getGreetingData()
        val ivGreetingWeatherIcon = view.findViewById<ImageView>(R.id.ivGreetingWeatherIcon)
        val tvGreetingTimeLabel = view.findViewById<TextView>(R.id.tvGreetingTimeLabel)
        val tvGreetingTitle = view.findViewById<TextView>(R.id.tvGreetingTitle)
        val btnSendGreeting = view.findViewById<MaterialButton>(R.id.btnSendGreeting)
        val btnCloseSheet = view.findViewById<View>(R.id.btnCloseSheet)

        ivGreetingWeatherIcon.setImageResource(greetingData.iconResId)
        tvGreetingTimeLabel.text = greetingData.label
        tvGreetingTitle.text = greetingData.title

        // Bind vector icon to MaterialButton natively
        btnSendGreeting.text = greetingData.buttonText
        btnSendGreeting.icon = ContextCompat.getDrawable(requireContext(), greetingData.iconResId)
        btnSendGreeting.iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

        btnCloseSheet.setOnClickListener { dismiss() }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMe()
                if (response.isSuccessful) {
                    senderName = response.body()?.data?.fullName
                }
            } catch (_: Exception) {}
        }

        // Dynamically fetch user's families to find the shared family ID and name
        val app = requireActivity().application as MainApplication
        val familyRepo = app.familyRepository
        lifecycleScope.launch {
            when (val result = familyRepo.getMyFamilies()) {
                is NetworkResult.Success -> {
                    // Try to find a family containing this target member
                    val sharedFamily = result.data.data.firstOrNull { family ->
                        family.members.any { it.email.lowercase() == memberEmail.lowercase() }
                    }
                    if (sharedFamily != null) {
                        tvActiveFamilyBadge.text = sharedFamily.name
                        resolvedFamilyId = sharedFamily.id

                        // Dynamically try loading custom profile avatar if member info has it
                        val targetMember = sharedFamily.members.firstOrNull { it.email.lowercase() == memberEmail.lowercase() }
                        if (targetMember != null && !targetMember.profileImageUrl.isNullOrBlank()) {
                            ivMemberAvatar.visibility = View.VISIBLE
                            tvAvatarInitial.visibility = View.GONE
                            val resolvedUrl = if (targetMember.profileImageUrl.startsWith("/")) {
                                "${com.labpro.nimons360.BuildConfig.BASE_URL}${targetMember.profileImageUrl}"
                            } else {
                                targetMember.profileImageUrl
                            }
                            ivMemberAvatar.load(resolvedUrl) { crossfade(true) }
                        }
                    } else {
                        tvActiveFamilyBadge.text = "Family Member"
                    }
                }
                else -> {
                    tvActiveFamilyBadge.text = "Family Member"
                }
            }
        }

        btnSendGreeting.setOnClickListener {
            val familyId = resolvedFamilyId
            if (familyId == null) {
                Toast.makeText(requireContext(), "Searching for shared family context...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSendGreeting.isEnabled = false
            lifecycleScope.launch {
                try {
                    val currentSender = senderName ?: "Someone"
                    val formattedMessage = "$currentSender: ${greetingData.title}"

                    val response = RetrofitClient.apiService.sendGreetingToMember(
                        SendGreetingRequest(familyId, targetUserId, formattedMessage)
                    )

                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Greeting Sent!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        btnSendGreeting.isEnabled = true
                        Log.e(TAG, "Greeting request failed with HTTP ${response.code()}")
                        Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    btnSendGreeting.isEnabled = true
                    Log.e(TAG, "Unable to send greeting", e)
                    Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show()
                }
            }
        }

        val etCustomMessage = view.findViewById<TextInputEditText>(R.id.etCustomMessage)
        val btnSendCustomMessage = view.findViewById<MaterialButton>(R.id.btnSendCustomMessage)
        btnSendCustomMessage.setOnClickListener {
            val familyId = resolvedFamilyId
            if (familyId == null) {
                Toast.makeText(requireContext(), "Searching for shared family context...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val messageText = etCustomMessage.text.toString().trim()
            if (messageText.isEmpty()) {
                etCustomMessage.error = "Message cannot be empty"
                return@setOnClickListener
            }

            btnSendCustomMessage.isEnabled = false
            lifecycleScope.launch {
                try {
                    val currentSender = senderName ?: "Someone"
                    val formattedMessage = "$currentSender: $messageText"

                    val response = RetrofitClient.apiService.sendGreetingToMember(
                        SendGreetingRequest(familyId, targetUserId, formattedMessage)
                    )

                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Message Sent!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        btnSendCustomMessage.isEnabled = true
                        Log.e(TAG, "Message request failed with HTTP ${response.code()}")
                        Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    btnSendCustomMessage.isEnabled = true
                    Log.e(TAG, "Unable to send member message", e)
                    Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getGreetingData(): GreetingData {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
        val timeString = timeFormat.format(calendar.time)

        return when (hour) {
            in 5..11 -> GreetingData(
                label = "MORNING · $timeString",
                title = "Good Morning!",
                buttonText = "Send Good Morning!",
                iconResId = R.drawable.ic_sunrise
            )
            in 12..16 -> GreetingData(
                label = "AFTERNOON · $timeString",
                title = "Good Afternoon!",
                buttonText = "Send Good Afternoon!",
                iconResId = R.drawable.ic_wb_sunny
            )
            else -> GreetingData(
                label = "NIGHT · $timeString",
                title = "Good Night!",
                buttonText = "Send Good Night!",
                iconResId = R.drawable.ic_nights_stay
            )
        }
    }

    private data class GreetingData(
        val label: String,
        val title: String,
        val buttonText: String,
        val iconResId: Int
    )

    companion object {
        const val TAG = "MemberDetailBottomSheet"

        private const val ARG_USER_ID = "user_id"
        private const val ARG_NAME = "name"
        private const val ARG_EMAIL = "email"
        private const val ARG_LAT = "lat"
        private const val ARG_LON = "lon"
        private const val ARG_BATTERY = "battery"
        private const val ARG_CHARGING = "charging"
        private const val ARG_NET = "net"

        fun newInstance(
            userId: Int,
            name: String,
            email: String,
            lat: Double,
            lon: Double,
            battery: Int?,
            charging: Boolean?,
            net: String?
        ) = MemberDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putInt(ARG_USER_ID, userId)
                putString(ARG_NAME, name)
                putString(ARG_EMAIL, email)
                putDouble(ARG_LAT, lat)
                putDouble(ARG_LON, lon)
                putInt(ARG_BATTERY, battery ?: -1)
                if (charging != null) {
                    putBoolean(ARG_CHARGING, charging)
                }
                putString(ARG_NET, net ?: "Unknown")
            }
        }
    }
}
