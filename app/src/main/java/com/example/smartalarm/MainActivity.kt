package com.example.smartalarm

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import android.media.MediaPlayer


class MainActivity : AppCompatActivity() {

    private lateinit var currentTimeTextView: TextView
    private lateinit var btnSetAlarm: Button
    private lateinit var btnCancelAlarm: Button
    private lateinit var tvStatus: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null
    private var alarmHour = 0
    private var alarmMinute = 0
    private var isAlarmEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentTimeTextView = findViewById(R.id.currentTimeTextView)
        btnSetAlarm = findViewById(R.id.btnSetAlarm)
        btnCancelAlarm = findViewById(R.id.btnCancelAlarm)
        tvStatus = findViewById(R.id.tvStatus)

        updateTime()

        btnSetAlarm.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = android.app.TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    alarmHour = selectedHour
                    alarmMinute = selectedMinute
                    isAlarmEnabled = true

                    val alarmTime = String.format("%02d:%02d", alarmHour, alarmMinute)
                    tvStatus.text = "⏰ Alarm set for $alarmTime"
                },
                hour,
                minute,
                true
            )

            timePickerDialog.show()
        }


        btnCancelAlarm.setOnClickListener {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }

            isAlarmEnabled = false

            tvStatus.text = "❌ Alarm cancelled"
        }

    }

    private fun updateTime() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(calendar.time)
        currentTimeTextView.text = currentTime

        if (isAlarmEnabled && currentHour == alarmHour && currentMinute == alarmMinute) {
            triggerAlarm()
            isAlarmEnabled = false
        }

        handler.postDelayed({
            updateTime()
        }, 1000)
    }

    private fun triggerAlarm() {
        tvStatus.text = "🔔 ALARM!!!"

        mediaPlayer = MediaPlayer.create(
            this,
            android.media.RingtoneManager.getDefaultUri(
                android.media.RingtoneManager.TYPE_ALARM
            )
        )

        mediaPlayer?.setVolume(1.0f, 1.0f)

        mediaPlayer?.start()
    }

}
