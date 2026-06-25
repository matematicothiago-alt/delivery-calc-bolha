package com.meuapp.lucroaovivo

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat

class BolhaService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var bubbleView: View
    private val handler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val valorStr = intent?.getStringExtra("valor")?.replace(",", ".")
        val kmStr = intent?.getStringExtra("km")?.replace(",", ".")

        val valor = valorStr?.toDoubleOrNull()
        val km = kmStr?.toDoubleOrNull()

        if (valor!= null && km!= null && km > 0) {
            val resultado = valor / km
            mostrarBolha(resultado)
        }
        return START_NOT_STICKY
    }

    private fun mostrarBolha(resultado: Double) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        bubbleView = inflater.inflate(R.layout.overlay_layout, null)

        val tvResultado = bubbleView.findViewById<TextView>(R.id.tvResultado)
        tvResultado.text = String.format("R$ %.2f /km", resultado)

        val cor = when {
            resultado >= 2.0 -> R.color.verde
            resultado >= 1.0 -> R.color.amarelo
            else -> R.color.vermelho
        }
        bubbleView.backgroundTintList = ContextCompat.getColorStateList(this, cor)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 100

        bubbleView.setOnClickListener { esconderBolha() }
        windowManager.addView(bubbleView, params)

        hideRunnable?.let { handler.removeCallbacks(it) }
        hideRunnable = Runnable { esconderBolha() }
        handler.postDelayed(hideRunnable!!, 15000)
    }

    private fun esconderBolha() {
        if (::bubbleView.isInitialized) {
            windowManager.removeView(bubbleView)
            stopSelf()
        }
    }
}
