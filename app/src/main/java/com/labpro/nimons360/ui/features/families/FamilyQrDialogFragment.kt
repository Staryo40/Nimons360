package com.labpro.nimons360.ui.features.families

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.labpro.nimons360.R
import com.labpro.nimons360.core.navigation.FamilyDeepLink
import com.labpro.nimons360.core.utils.FamilyQrCodeGenerator
import java.io.File
import java.io.FileOutputStream

class FamilyQrDialogFragment : DialogFragment() {
    private lateinit var qrBitmap: Bitmap
    private lateinit var deepLink: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_Nimons360)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.dialog_family_qr, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val familyId = requireArguments().getInt(ARG_FAMILY_ID)
        val familyName = requireArguments().getString(ARG_FAMILY_NAME).orEmpty()
        val familyCode = requireArguments().getString(ARG_FAMILY_CODE).orEmpty()

        deepLink = FamilyDeepLink(familyId, familyCode).toUriString()
        qrBitmap = FamilyQrCodeGenerator.generate(deepLink)

        view.findViewById<TextView>(R.id.tvQrFamilyName).text = familyName
        view.findViewById<TextView>(R.id.tvQrInviteCode).text = familyCode
        view.findViewById<TextView>(R.id.tvQrLink).text = deepLink
        view.findViewById<ImageView>(R.id.ivQrCode).setImageBitmap(qrBitmap)
        view.findViewById<MaterialButton>(R.id.btnCloseQr).setOnClickListener { dismiss() }
        view.findViewById<MaterialButton>(R.id.btnShareQr).setOnClickListener {
            shareQrPng(familyId, familyName)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val horizontalMargin = 24.dpToPx()
            val verticalMargin = 24.dpToPx()
            val availableWidth = resources.displayMetrics.widthPixels - (horizontalMargin * 2)
            val availableHeight = resources.displayMetrics.heightPixels - (verticalMargin * 2)
            val dialogWidth = minOf(availableWidth, 560.dpToPx())

            setLayout(dialogWidth, WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes.apply { dimAmount = 0.55f }

            decorView.post {
                val measuredHeight = view?.measuredHeight ?: availableHeight
                setLayout(dialogWidth, minOf(measuredHeight, availableHeight))
            }
        }
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    private fun shareQrPng(familyId: Int, familyName: String) {
        runCatching {
            val file = writeQrToCache(familyId)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "com.labpro.nimons360.fileprovider",
                file,
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_family_message, familyName, deepLink))
                clipData = ClipData.newUri(requireContext().contentResolver, file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(sendIntent, getString(R.string.qr_share_chooser)))
        }.onFailure {
            Toast.makeText(requireContext(), R.string.qr_share_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeQrToCache(familyId: Int): File {
        val directory = File(requireContext().cacheDir, "family_qr").apply {
            if (!exists()) mkdirs()
        }
        val file = File(directory, "family_$familyId.png")
        FileOutputStream(file).use { output ->
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        return file
    }

    companion object {
        const val TAG = "FamilyQrDialogFragment"

        private const val ARG_FAMILY_ID = "family_id"
        private const val ARG_FAMILY_NAME = "family_name"
        private const val ARG_FAMILY_CODE = "family_code"

        fun newInstance(
            familyId: Int,
            familyName: String,
            familyCode: String,
        ) = FamilyQrDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_FAMILY_ID, familyId)
                putString(ARG_FAMILY_NAME, familyName)
                putString(ARG_FAMILY_CODE, familyCode)
            }
        }
    }
}
