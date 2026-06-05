package com.labpro.nimons360.ui.features.families

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.notification.BroadcastNotificationRequest
import com.labpro.nimons360.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SendMessageBottomSheet : BottomSheetDialogFragment() {

    private val familyId: Int by lazy {
        requireArguments().getInt(ARG_FAMILY_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_send_message, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Dynamic Time-Based Weather Greeting Setup
        val greetingData = getGreetingData()
        val ivGreetingWeatherIcon = view.findViewById<ImageView>(R.id.ivGreetingWeatherIcon)
        val tvGreetingTimeLabel = view.findViewById<TextView>(R.id.tvGreetingTimeLabel)
        val tvGreetingTitle = view.findViewById<TextView>(R.id.tvGreetingTitle)
        val btnSendGreeting = view.findViewById<MaterialButton>(R.id.btnSendGreeting)

        ivGreetingWeatherIcon.setImageResource(greetingData.iconResId)
        tvGreetingTimeLabel.text = greetingData.label
        tvGreetingTitle.text = greetingData.title

        // Bind vector icon to MaterialButton natively
        btnSendGreeting.text = greetingData.buttonText
        btnSendGreeting.icon = ContextCompat.getDrawable(requireContext(), greetingData.iconResId)
        btnSendGreeting.iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

        btnSendGreeting.setOnClickListener {
            btnSendGreeting.isEnabled = false
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.sendBroadcastNotification(
                        BroadcastNotificationRequest(familyId, greetingData.title)
                    )
                    if (response.isSuccessful && response.body()?.data?.sent == true) {
                        Toast.makeText(requireContext(), "Family Notification Sent!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        btnSendGreeting.isEnabled = true
                        Toast.makeText(requireContext(), "Failed to send greeting: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    btnSendGreeting.isEnabled = true
                    Toast.makeText(requireContext(), "Error sending greeting: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val etMessage = view.findViewById<TextInputEditText>(R.id.etMessage)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSend = view.findViewById<Button>(R.id.btnSend)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString().trim()
            if (messageText.isBlank()) {
                etMessage.error = "Message cannot be empty"
                return@setOnClickListener
            }

            btnSend.isEnabled = false
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.sendBroadcastNotification(
                        BroadcastNotificationRequest(familyId, messageText)
                    )
                    if (response.isSuccessful && response.body()?.data?.sent == true) {
                        Toast.makeText(requireContext(), "Family Notification Sent!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        btnSend.isEnabled = true
                        Toast.makeText(requireContext(), "Failed to send message: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    btnSend.isEnabled = true
                    Toast.makeText(requireContext(), "Error sending message: ${e.message}", Toast.LENGTH_SHORT).show()
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
        const val TAG = "SendMessageBottomSheet"
        private const val ARG_FAMILY_ID = "family_id"

        fun newInstance(familyId: Int) = SendMessageBottomSheet().apply {
            arguments = Bundle().apply {
                putInt(ARG_FAMILY_ID, familyId)
            }
        }
    }
}
