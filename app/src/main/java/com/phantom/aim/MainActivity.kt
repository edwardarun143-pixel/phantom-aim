package com.phantom.aim

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.phantom.aim.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.swAim.isChecked = Prefs.aim
        b.swEsp.isChecked = Prefs.esp
        b.swAnti.isChecked = Prefs.anti

        b.swAim.setOnCheckedChangeListener { _, v -> Prefs.aim = v }
        b.swEsp.setOnCheckedChangeListener { _, v -> Prefs.esp = v }
        b.swAnti.setOnCheckedChangeListener { _, v -> Prefs.anti = v }

        b.btnLaunch.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            } else {
                startOverlay()
            }
        }
        updateStatus()
    }

    private fun requestOverlayPermission() {
        b.tvSubStatus.text = "Granting overlay permission…"
        startActivity(Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        ))
    }

    private fun startOverlay() {
        b.tvStatus.text = "INJECTING"
        b.tvSubStatus.text = "Loading detection model…"

        var p = 0
        val run = object : Runnable {
            override fun run() {
                p += 7
                b.progressBar.progress = p.coerceAtMost(100)
                if (p < 100) {
                    b.progressBar.postDelayed(this, 60)
                } else {
                    b.tvStatus.text = "ACTIVE"
                    b.tvSubStatus.text = "Overlay running · tap notification to stop"
                    startService(Intent(this@MainActivity, OverlayService::class.java))
                }
            }
        }
        b.progressBar.post(run)
    }

    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this) && b.tvStatus.text == "IDLE") {
            b.tvSubStatus.text = "Overlay permission granted"
        }
    }

    private fun updateStatus() {
        if (!Settings.canDrawOverlays(this)) {
            b.tvStatus.text = "IDLE"
            b.tvSubStatus.text = "Awaiting overlay permission"
        }
    }
}
