package com.labpro.nimons360.ui.features.families

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.notification.BroadcastNotificationRequest
import com.labpro.nimons360.data.remote.RetrofitClient
import kotlinx.coroutines.launch

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
