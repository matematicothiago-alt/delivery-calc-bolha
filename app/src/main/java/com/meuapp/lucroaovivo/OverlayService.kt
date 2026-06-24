package com.meuapp.lucroaovivo

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*

class OverlayService: Service() {
    lateinit var wm: WindowManager
    lateinit var view: View
    lateinit var tvLucro: TextView
    
    override fun onCreate() {
        super.onCreate()
        
        val ch = NotificationChannel("lucro", "LucroAoVivo", NotificationManager.IMPORTANCE_LOW)
        ch.setShowBadge(false)
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        
        val notification = Notification.Builder(this, "lucro")
            .setContentTitle("LucroAoVivo ativo")
            .setContentText("Bolha rodando")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
        
        startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Libera 'Exibir sobre outros apps' nas configs", Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }
        
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        
        view = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF00AA00.toInt())
            setPadding(24, 16, 24, 16)
            
            tvLucro = TextView(context).apply {
                text = "Lucro: R$ 0.00"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 18f
            }
            addView(tvLucro)
            
            val btn = Button(context).apply {
                text = "X"
                setOnClickListener { stopSelf() }
            }
            addView(btn)
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 50
        params.y = 200
        
        view.setOnTouchListener(object: View.OnTouchListener {
            var x = 0; var y = 0; var tx = 0f; var ty = 0f
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when(e.action) {
                    MotionEvent.ACTION_DOWN -> { x = params.x; y = params.y; tx = e.rawX; ty = e.rawY }
                    MotionEvent.ACTION_MOVE -> { 
                        params.x = x + (e.rawX - tx).toInt()
                        params.y = y + (e.rawY - ty).toInt()
                        wm.updateViewLayout(view, params)
                    }
                }
                return true
            }
        })
        
        wm.addView(view, params)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val lucro = intent?.getStringExtra("lucro") ?: "Lucro: R$ 0.00"
        tvLucro.text = lucro
        return START_STICKY
    }
    
    override fun onDestroy() {
        try { wm.removeView(view) } catch(e: Exception) {}
        super.onDestroy()
    }
    
    override fun onBind(i: Intent?) = null
}
