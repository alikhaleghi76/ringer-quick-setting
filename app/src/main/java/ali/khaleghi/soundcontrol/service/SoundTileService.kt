package ali.khaleghi.soundcontrol.service

import ali.khaleghi.soundcontrol.R
import android.graphics.drawable.Icon
import android.service.quicksettings.TileService
import android.media.AudioManager
import android.content.IntentFilter
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.app.NotificationManager
import android.service.quicksettings.Tile


class SoundTileService : TileService() {

    private val STATE_SILENT = 1
    private val STATE_VIBRATE = 2
    private val STATE_VOLUME = 3

    var state = STATE_SILENT

    private lateinit var receiver: BroadcastReceiver

    override fun onTileAdded() {
        super.onTileAdded()

        qsTile.state = Tile.STATE_ACTIVE

        getPermissions()

        getStateFromAudioManager()
        updateState()

        registerRingerModeChangeListener()
    }

    private fun updateState() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when (state) {
            STATE_SILENT -> {
                qsTile.icon =
                    Icon.createWithResource(baseContext, R.drawable.ic_do_not_disturb)
                am.ringerMode = AudioManager.RINGER_MODE_SILENT
                qsTile.label = getString(R.string.silent)
            }
            STATE_VIBRATE -> {
                qsTile.icon = Icon.createWithResource(baseContext, R.drawable.ic_vibration)
                am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                qsTile.label = getString(R.string.vibration)
            }
            STATE_VOLUME -> {
                qsTile.icon = Icon.createWithResource(baseContext, R.drawable.ic_volume_up)
                am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                qsTile.label = getString(R.string.volume)
            }
        }

        qsTile.updateTile()

    }

    override fun onClick() {
        super.onClick()

        when (state) {
            STATE_SILENT -> state = STATE_VIBRATE
            STATE_VIBRATE -> state = STATE_VOLUME
            STATE_VOLUME -> state = STATE_SILENT
        }

        updateState()
    }

    private fun getStateFromAudioManager() {

        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> state = STATE_SILENT
            AudioManager.RINGER_MODE_VIBRATE -> state = STATE_VIBRATE
            AudioManager.RINGER_MODE_NORMAL -> state = STATE_VOLUME
        }

    }

    private fun registerRingerModeChangeListener() {

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                getStateFromAudioManager()
                updateState()
            }
        }
        val filter = IntentFilter(
            AudioManager.RINGER_MODE_CHANGED_ACTION
        )
        registerReceiver(receiver, filter)

    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        if (receiver != null)
            unregisterReceiver(receiver)
    }

    private fun getPermissions() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) {

            val intent = Intent(
                android.provider.Settings
                    .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            )

            startActivity(intent)
            val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            sendBroadcast(closeIntent)
        }
    }

}