package com.phantom.aim

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlin.random.Random

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var root: View
    private lateinit var detBox: View
    private lateinit var crosshair: View
    private lateinit var hudFps: TextView
    private var running = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotif())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val v = LayoutInflater.from(this).inflate(R.layout.overlay_view, null)
        root = v
        detBox = v.findViewById(R.id.detBox)
        crosshair = v.findViewById(R.id.crosshair)
        hudFps = v.findViewById(R.id.hudFps)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        
        // This is the new line that fixes touches
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        wm.addView(root, lp)
        running = true
        animateDetection()
    }

    private fun animateDetection() {
        val h = android.os.Handler(mainLooper)
        val run = object : Runnable {
            override fun run() {
                if (!running) return
                val fps = Random.nextInt(58, 75)
                val lock = Random.nextInt(0, 3)
                hudFps.text = "FPS $fps · LOCK $lock"
                if (Prefs.aim) {
                    crosshair.alpha = 0.6f + Random.nextFloat() * 0.4f
                    crosshair.scaleX = 0.9f + Random.nextFloat() * 0.3f
                    crosshair.scaleY = crosshair.scaleX
                }
                if (Prefs.esp) {
                    detBox.visibility = View.VISIBLE
                    detBox.translationX = Random.nextInt(-80, 80).toFloat()
                    detBox.translationY = Random.nextInt(-50, 50).toFloat()
                } else {
                    detBox.visibility = View.GONE
                }
                h.postDelayed(this, 120)
            }
        }
        h.post(run)
    }

    private fun buildNotif(): Notification {
        val ch = "phantom_ch"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(ch, "Phantom Aim", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(nc)
        }
        val stop = PendingIntent.getService(
            this, 0, Intent(this, OverlayService::class.java).apply { action = "stop" },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, ch)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Phantom Aim Active")
            .setContentText("Overlay running · tap STOP to disable")
            .addAction(0, "STOP", stop)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "stop") stopSelf()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        running = false
        try { wm.removeView(root) } catch (_: Exception) {}
        super.onDestroy()
    }
}
