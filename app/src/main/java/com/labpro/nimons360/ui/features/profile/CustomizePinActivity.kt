package com.labpro.nimons360.ui.features.profile

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.core.utils.TokenManager
import com.labpro.nimons360.core.utils.applyStatusBarHeaderInset
import com.labpro.nimons360.data.model.map.CustomPin
import com.labpro.nimons360.data.repository.CustomPinRepository
import com.labpro.nimons360.ui.features.map.MapPinMaker

class CustomizePinActivity : AppCompatActivity() {
    private val app: MainApplication
        get() = application as MainApplication

    private lateinit var repository: CustomPinRepository
    private lateinit var pinContainer: LinearLayout
    private val downloading = mutableSetOf<String>()
    private var pendingDownload: CustomPin? = null

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingDownload?.let(::startDownload)
        } else {
            Toast.makeText(this, R.string.pin_notification_required, Toast.LENGTH_LONG).show()
        }
        pendingDownload = null
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getStringExtra(CustomPinDownloadService.EXTRA_PIN_ID) ?: return
            downloading.remove(id)
            val successful = intent.getBooleanExtra(CustomPinDownloadService.EXTRA_SUCCESS, false)
            renderPins()
            Toast.makeText(
                this@CustomizePinActivity,
                if (successful) R.string.pin_download_ready else R.string.pin_download_error,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customize_pin)
        findViewById<View>(R.id.customizePinHeader).applyStatusBarHeaderInset(extraTopDp = 18)
        repository = CustomPinRepository(applicationContext)
        pinContainer = findViewById(R.id.pinGridContainer)

        findViewById<View>(R.id.btnCustomizePinBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnColorPins).setOnClickListener { showColorDialog() }
        render()
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            downloadReceiver,
            IntentFilter(CustomPinDownloadService.ACTION_DOWNLOAD_FINISHED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onStop() {
        unregisterReceiver(downloadReceiver)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        renderCurrentPin()
        renderPins()
        renderColorSelection()
    }

    private fun renderCurrentPin() {
        val image = findViewById<ImageView>(R.id.ivCurrentPin)
        val initial = findViewById<TextView>(R.id.tvCurrentPinInitial)
        val selected = CustomPin.find(app.tokenManager.getPinSkin())
        val file = selected?.takeIf(repository::isDownloaded)?.let(repository::file)

        if (file != null) {
            image.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            image.visibility = View.VISIBLE
            initial.visibility = View.GONE
        } else {
            image.visibility = View.GONE
            initial.visibility = View.VISIBLE
            initial.background = MapPinMaker.self(
                this,
                app.tokenManager.getPresenceName().firstOrNull()?.uppercase() ?: "Y",
                app.tokenManager.getPresenceName(),
                0f,
                selectedColor(),
            )
            initial.text = ""
        }
    }

    private fun renderPins() {
        pinContainer.removeAllViews()
        CustomPin.all.chunked(3).forEach { rowPins ->
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).also { it.topMargin = dp(10) }
                orientation = LinearLayout.HORIZONTAL
                weightSum = 3f
            }
            if (rowPins.size < 3) {
                row.addView(createGridSpacer((3f - rowPins.size) / 2f))
            }
            rowPins.forEach { pin ->
                row.addView(createPinCard(pin))
            }
            if (rowPins.size < 3) {
                row.addView(createGridSpacer((3f - rowPins.size) / 2f))
            }
            pinContainer.addView(row)
        }
    }

    private fun createGridSpacer(weight: Float): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(0, 1, weight)
    }

    private fun createPinCard(pin: CustomPin): View {
        val card = LayoutInflater.from(this)
            .inflate(R.layout.item_custom_pin, pinContainer, false) as MaterialCardView
        card.layoutParams = LinearLayout.LayoutParams(0, dp(116), 1f).also {
            it.marginStart = dp(5)
            it.marginEnd = dp(5)
        }

        val downloaded = repository.isDownloaded(pin)
        if (repository.isDownloading(pin)) downloading += pin.id
        val selected = app.tokenManager.getPinSkin() == pin.id
        val image = card.findViewById<ImageView>(R.id.ivPinPreview)
        val status = card.findViewById<ImageView>(R.id.ivPinStatus)
        card.findViewById<TextView>(R.id.tvPinName).text = pin.label

        if (downloaded) {
            image.setImageBitmap(BitmapFactory.decodeFile(repository.file(pin).absolutePath))
            image.alpha = 1f
        } else {
            image.load(pin.url) {
                placeholder(R.drawable.ic_pin)
                error(R.drawable.ic_pin)
                crossfade(true)
            }
            image.alpha = 0.48f
        }
        status.setImageResource(
            when {
                selected -> R.drawable.ic_pin_selected
                pin.id in downloading -> R.drawable.ic_pin_downloading
                downloaded -> R.drawable.ic_pin_ready
                else -> R.drawable.ic_download
            }
        )
        card.strokeWidth = if (selected) dp(2) else dp(1)
        card.strokeColor = ContextCompat.getColor(
            this,
            if (selected) R.color.analytics_blue else R.color.divider,
        )
        card.setOnClickListener {
            when {
                downloaded -> selectPin(pin)
                pin.id !in downloading -> requestDownload(pin)
            }
        }
        return card
    }

    private fun requestDownload(pin: CustomPin) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            pendingDownload = pin
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startDownload(pin)
        }
    }

    private fun startDownload(pin: CustomPin) {
        downloading += pin.id
        renderPins()
        ContextCompat.startForegroundService(
            this,
            Intent(this, CustomPinDownloadService::class.java)
                .putExtra(CustomPinDownloadService.EXTRA_PIN_ID, pin.id),
        )
    }

    private fun selectPin(pin: CustomPin) {
        app.tokenManager.setPinSkin(pin.id)
        app.analytics.pinCustomized(pin.id)
        render()
        Toast.makeText(this, R.string.pin_selected, Toast.LENGTH_SHORT).show()
    }

    private fun showColorDialog() {
        val styles = arrayOf(
            TokenManager.PIN_TEAL,
            TokenManager.PIN_CORAL,
            TokenManager.PIN_BLUE,
            TokenManager.PIN_PURPLE,
            TokenManager.PIN_ORANGE,
        )
        val labels = arrayOf("Teal", "Coral", "Blue", "Purple", "Orange")
        val current = styles.indexOf(app.tokenManager.getPinStyle()).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.pin_choose_color)
            .setSingleChoiceItems(labels, current) { dialog, which ->
                app.tokenManager.setPinStyle(styles[which])
                app.analytics.pinCustomized(styles[which])
                dialog.dismiss()
                render()
                Toast.makeText(this, R.string.pin_selected, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_close, null)
            .show()
    }

    private fun renderColorSelection() {
        findViewById<TextView>(R.id.tvSelectedColor).text = getString(
            R.string.pin_color_selected,
            app.tokenManager.getPinStyle().replaceFirstChar(Char::uppercase),
        )
    }

    private fun selectedColor(): Int {
        val color = when (app.tokenManager.getPinStyle()) {
            TokenManager.PIN_CORAL -> R.color.secondary_coral
            TokenManager.PIN_BLUE -> R.color.pin_blue
            TokenManager.PIN_PURPLE -> R.color.pin_purple
            TokenManager.PIN_ORANGE -> R.color.pin_orange
            else -> R.color.primary_teal
        }
        return ContextCompat.getColor(this, color)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
