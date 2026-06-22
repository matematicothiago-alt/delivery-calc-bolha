package com.meuapp.lucroaovivo

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat

class OverlayService: Service() {
    lateinit var wm: WindowManager
    lateinit var view: View
    
    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) { 
            view.findViewById<TextView>(R.id.tvInfo).text = i?.getStringExtra("dados") ?: "Aguardando detalhes..."
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // ANDROID 14 FIX: Notificação ANTES de tudo
        if (Build.VERSION.SDK_INT
