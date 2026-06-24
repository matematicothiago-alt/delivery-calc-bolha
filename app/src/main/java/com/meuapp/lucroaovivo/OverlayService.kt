package com.meuapp.lucroaovivo

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class BolhaService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var bubbleView: View
    private val handler = Handler(Looper.getMainLooper())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val valorStr = intent?.getStringExtra("valor") ?: "0"
        val kmStr = intent?.getStringExtra("km") ?: "0"
        
        val valor = valorStr.toDoubleOrNull() ?: 0.0
        val km = kmStr.toDoubleOrNull() ?: 0.0
        val lucroKm = if (km > 0) valor / km else 0.0
        
        mostrarBolha(lucroKm)
        return START_NOT_STICKY
    }

    private fun mostrarBolha(lucroKm: Double) {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        bubbleView = inflater.inflate(R.layout.overlay_layout, null)
        
        val tvResultado = bubbleView.findViewById<TextView>(R.id.tvResultadoBolha)
        tvResultado.text = "R$ %.2f /km".format(lucroKm)
        
        // Verde se >= 2.00, Vermelho se < 1.00, Amarelo no meio
        when {
            lucroKm >= 2.0 -> tvResultado.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
            lucroKm < 1.0 -> tvResultado.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
            else -> tvResultado.setBackgroundColor(android.graphics.Color.parseColor("#FFC107"))
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 20
        params.y = 100
        
        windowManager.addView(bubbleView, params)
        
        // Fecha sozinho depois de 5 segundos
        handler.postDelayed({
            stopSelf()
        }, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::bubbleView.isInitialized) {
            windowManager.removeView(bubbleView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
