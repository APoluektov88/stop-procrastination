package com.stopit.procrastination

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.util.*

class AppMonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var monitorRunnable: Runnable? = null
    
    private val forbiddenApps = listOf(
        "com.vkontakte.android", // ВКонтакте
        "com.instagram.android", // Instagram
        "org.telegram.messenger", // Telegram
        "com.zhiliaoapp.musically" // TikTok
    )

    private val punchlines = listOf(
        "Эй! Опять залипаешь? Закрывай соцсеть и иди работай!",
        "Минута в соцсети — минус минута твоей жизни. Выходи давай!",
        "Хватит листать ленту, тебя ждут великие дела!",
        "Твой внутренний прокрастинатор ликует. А ну-ка закрой приложение!"
    )

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startMonitoring()
    }

    private fun startForegroundService() {
        val channelId = "procrastination_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Анти-Прокрастинация",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Анти-прокрастинация работает")
            .setContentText("Мы следим за твоей продуктивностью...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)
    }

    private fun startMonitoring() {
        monitorRunnable = object : Runnable {
            override fun run() {
                checkActiveApp()
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(monitorRunnable!!)
    }

    private fun checkActiveApp() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, time - 10000, time
        )

        if (!stats.isNullOrEmpty()) {
            val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
            val currentApp = sortedStats[0].packageName

            if (forbiddenApps.contains(currentApp)) {
                sendAlertNotification()
            }
        }
    }

    private fun sendAlertNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "procrastination_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Дерзкие напоминания",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val randomText = punchlines[Random().nextInt(punchlines.size)]

        val alertNotification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Поймался!")
            .setContentText(randomText)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), alertNotification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        monitorRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }
}
