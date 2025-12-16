package com.example.smartalarm

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import android.os.Vibrator
import android.os.VibrationEffect
import android.app.NotificationManager
import android.app.NotificationChannel
import androidx.core.app.NotificationCompat
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import android.content.SharedPreferences
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.core.content.edit


class MainActivity : AppCompatActivity() {

    private lateinit var currentTimeTextView: TextView
    private lateinit var btnSetAlarm: Button
    private lateinit var btnCancelAlarm: Button
    private lateinit var btnSnooze: Button
    private lateinit var tvStatus: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var alarmHour = 0
    private var alarmMinute = 0
    private var isAlarmEnabled = false
    private lateinit var vibrator: Vibrator
    private lateinit var notificationManager: NotificationManager
    private val channelId = "alarm_channel"
    private val notificationId = 1
    private val permissionRequestCode = 100
    private var isSnoozeActive = false
    private lateinit var sharedPreferences: SharedPreferences
    private companion object {
        const val PREFS_NAME = "AlarmPrefs"
        const val KEY_ALARM_HOUR = "alarmHour"
        const val KEY_ALARM_MINUTE = "alarmMinute"
        const val KEY_IS_ALARM_ENABLED = "isAlarmEnabled"
    }

    private lateinit var alarmTTS: AlarmTTS

    private lateinit var weatherManager: WeatherManager
    private lateinit var nameDayManager: NameDayManager



    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, "Alarm", importance).apply {
            description = "Channel for Alarm"
            enableVibration(true)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        loadAlarmData()

        alarmTTS = AlarmTTS(this, AppSettings(this).language)

        weatherManager = WeatherManager(BuildConfig.OPENWEATHER_API_KEY, this)
        nameDayManager = NameDayManager(this)

        currentTimeTextView = findViewById(R.id.currentTimeTextView)
        btnSetAlarm = findViewById(R.id.btnSetAlarm)
        btnCancelAlarm = findViewById(R.id.btnCancelAlarm)
        tvStatus = findViewById(R.id.tvStatus)
        btnSnooze = findViewById(R.id.btnSnooze)

        vibrator = getSystemService(Vibrator::class.java)

        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    permissionRequestCode
                )
            }
        }

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

                    val alarmTime = String.format(Locale.getDefault(),"%02d:%02d", alarmHour, alarmMinute)
                    tvStatus.text = getString(R.string.alarm_set, alarmTime)
                    saveAlarmData()
                },
                hour,
                minute,
                true
            )

            timePickerDialog.show()
        }


        btnCancelAlarm.setOnClickListener {
            notificationManager.cancel(notificationId)

            vibrator.cancel()

            alarmTTS.stop()

            isAlarmEnabled = false
            tvStatus.text = getString(R.string.alarm_cancelled)

            saveAlarmData()
        }

        btnSnooze.setOnClickListener {
            vibrator.cancel()
            alarmTTS.stop()
            notificationManager.cancel(notificationId)

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 5)

            alarmHour = calendar.get(Calendar.HOUR_OF_DAY)
            alarmMinute = calendar.get(Calendar.MINUTE)

            isSnoozeActive = true
            isAlarmEnabled = true

            val alarmTime = String.format(Locale.getDefault(),"%02d:%02d", alarmHour, alarmMinute)
            tvStatus.text = getString(R.string.alarm_snoozed, alarmTime)

            saveAlarmData()
        }

        val btnSettings = findViewById<Button>(R.id.btnSettings)

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onDestroy() {
        alarmTTS.shutdown()
        super.onDestroy()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.notification_permission_granted), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun attachBaseContext(newBase: Context) {
        val lang = AppSettings(newBase).language
        val wrapped = LocaleManager.wrapContext(newBase, lang)
        super.attachBaseContext(wrapped)
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
        tvStatus.text = getString(R.string.alarm_ringing)
        isSnoozeActive = false

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)

        val pattern = longArrayOf(0, 500, 500, 500, 500, 500)
        val vibrationEffect = VibrationEffect.createWaveform(pattern, -1)
        vibrator.vibrate(vibrationEffect)

        val settings = AppSettings(this)

        lifecycleScope.launch {
            val weatherMessage = weatherManager.getWeather(
                settings.city,
                settings.language
            )

            val nameDayNames = nameDayManager.getTodaysNames()

            val alarmMessage = LocaleManager.getStringResource(
                this@MainActivity,
                R.string.tts_alarm_message,
                settings.language
            )

            val fullMessage = buildString {
                append(alarmMessage)
                append(" ")
                append(weatherMessage)
                if (settings.speakNameDay) {
                    val nameDayMessage = nameDayManager.getNameDayMessage(nameDayNames, settings.language, this@MainActivity)
                    if (nameDayMessage.isNotEmpty()) {
                        append(". ")
                        append(nameDayMessage)
                    }
                }
            }

            alarmTTS.setLanguage(settings.language)
            alarmTTS.speak(fullMessage)
        }
    }


    private fun loadAlarmData() {
        alarmHour = sharedPreferences.getInt(KEY_ALARM_HOUR, 7)
        alarmMinute = sharedPreferences.getInt(KEY_ALARM_MINUTE, 0)
        isAlarmEnabled = sharedPreferences.getBoolean(KEY_IS_ALARM_ENABLED, false)
        tvStatus = findViewById(R.id.tvStatus)

        if (isAlarmEnabled) {
            val alarmTime = String.format(Locale.getDefault(),"%02d:%02d", alarmHour, alarmMinute)
            tvStatus.text = getString(R.string.alarm_set, alarmTime)
        } else {
            tvStatus.text = getString(R.string.alarm_not_set)
        }
    }


    private fun saveAlarmData() {
        sharedPreferences.edit {
            putInt(KEY_ALARM_HOUR, alarmHour)
            putInt(KEY_ALARM_MINUTE, alarmMinute)
            putBoolean(KEY_IS_ALARM_ENABLED, isAlarmEnabled)
        }
    }
}
