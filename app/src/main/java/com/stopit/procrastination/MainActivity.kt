package com.stopit.procrastination

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val startButton = Button(this).apply {
            text = "Включить анти-прокрастинацию"
            setOnClickListener {
                if (hasUsageStatsPermission()) {
                    Toast.makeText(this@MainActivity, "Мониторинг запущен!", Toast.LENGTH_SHORT).show()
                    val serviceIntent = Intent(this@MainActivity, AppMonitorService::class.java)
                    startService(serviceIntent)
                } else {
                    Toast.makeText(this@MainActivity, "Нужно разрешение на статистику!", Toast.LENGTH_LONG).show()
                    requestUsageStatsPermission()
                }
            }
        }
        
        setContentView(startButton)
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.noteOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }
}
