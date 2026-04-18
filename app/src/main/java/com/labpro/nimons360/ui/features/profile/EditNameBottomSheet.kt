package com.labpro.nimons360.ui.features.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.viewmodel.ProfileViewModel
import com.labpro.nimons360.viewmodel.ProfileViewModelFactory

class EditNameBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: ProfileViewModel by activityViewModels {
        ProfileViewModelFactory(
            (requireActivity().application as MainApplication).userRepository,
            (requireActivity().application as MainApplication).authRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_edit_name, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<TextInputEditText>(R.id.etDisplayName)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        etName.setText(viewModel.uiState.value.name)

        btnCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val newName = etName.text?.toString().orEmpty()
            if (newName.isNotBlank()) {
                viewModel.updateName(newName)
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "EditNameBottomSheet"
    }
}