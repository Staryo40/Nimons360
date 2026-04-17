package com.labpro.nimons360.ui.features.live

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.labpro.nimons360.BuildConfig
import io.agora.agorauikit_android.AgoraConnectionData
import io.agora.agorauikit_android.AgoraVideoViewer
import io.agora.rtc2.Constants

class LiveActivity : AppCompatActivity() {

    private var agView: AgoraVideoViewer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appID = BuildConfig.AGORA_APP_ID

        val roomID = intent.getStringExtra(EXTRA_ROOM_ID) ?: return
        val isHost = intent.getBooleanExtra(EXTRA_IS_HOST, false)

        agView = AgoraVideoViewer(
            this,
            AgoraConnectionData(appId = appID)
        )

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setContentView(agView, params)

        val role = if (isHost) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        agView?.join(channel = roomID, fetchToken = false, role = role)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                agView?.leaveChannel()
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        agView?.leaveChannel()
        agView = null
    }

    companion object {
        private const val EXTRA_ROOM_ID = "room_id"
        private const val EXTRA_IS_HOST = "is_host"

        fun newIntent(
            context: Context,
            roomID: String,
            isHost: Boolean
        ): Intent {
            return Intent(context, LiveActivity::class.java).apply {
                putExtra(EXTRA_ROOM_ID, roomID)
                putExtra(EXTRA_IS_HOST, isHost)
            }
        }
    }
}