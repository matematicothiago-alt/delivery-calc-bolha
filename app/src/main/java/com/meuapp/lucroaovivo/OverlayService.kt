package com.meuapp.lucroaovivo

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*
import com.google.android.gms.location.LocationServices
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class OverlayService: Service() {
    lateinit var wm: WindowManager
    lateinit var view: LinearLayout
    lateinit var tvMin: TextView
    lateinit var tvHora: TextView  
    lateinit var tvKm: TextView
    lateinit var tvLucro: TextView
    lateinit var tvKmTotal: TextView
    val client = OkHttpClient()
    val custoKm = 0.35
    var params: WindowManager.LayoutParams? = null
    
    override fun onCreate() {
        super.onCreate()
        
        val ch = NotificationChannel("lucro", "LucroAoVivo", NotificationManager.IMPORTANCE_LOW)
        ch.setShowBadge(false)
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        
        val notification = Notification.Builder(this, "lucro")
            .setContentTitle("LucroAoVivo ativo")
            .setContentText("Monitorando Uber Eats")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
        
        startForeground(1, notification)
        
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        
        view = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = 32f
                setColor(0xFF1A1A1A.toInt())
            }
            setPadding(28, 18, 28, 18)
            
            val linha1 = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                val paramsColuna = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                val divider = { View(context).apply { 
                    layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT)
                    setBackgroundColor(0xFF444444.toInt()) 
                }}
                
                fun coluna(tv: TextView, label: String): LinearLayout {
                    return LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = paramsColuna
                        gravity = Gravity.CENTER
                        tv.apply {
                            textSize = 26f
                            setTextColor(0xFF39FF14.toInt())
                            typeface = Typeface.DEFAULT_BOLD
                            gravity = Gravity.CENTER
                            text = "--"
                        }
                        addView(tv)
                        addView(TextView(context).apply {
                            text = label
                            textSize = 12f
                            setTextColor(0xFF39FF14.toInt())
                            gravity = Gravity.CENTER
                            setPadding(0, 4, 0, 0)
                        })
                    }
                }
                
                tvMin = TextView(context)
                tvHora = TextView(context)
                tvKm = TextView(context)
                tvLucro = TextView(context)
                
                addView(coluna(tvMin, "/min"))
                addView(divider())
                addView(coluna(tvHora, "/hr"))
                addView(divider())
                addView(coluna(tvKm, "/km"))
                addView(divider())
                addView(coluna(tvLucro, "Lucro (R$)"))
            }
            
            tvKmTotal = TextView(context).apply {
                textSize = 12f
                setTextColor(0xFFAAAAAA.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
                text = "Aguardando entrega..."
            }
            
            addView(linha1)
            addView(tvKmTotal)
        }
        
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { 
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 150 
        }
        
        view.setOnTouchListener(object: View.OnTouchListener {
            var initialX = 0; var initialY = 0; var touchX = 0f; var touchY = 0f
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when(e.action) {
                    MotionEvent.ACTION_DOWN -> { 
                        initialX = params!!.x; initialY = params!!.y
                        touchX = e.rawX; touchY = e.rawY 
                    }
                    MotionEvent.ACTION_MOVE -> { 
                        params!!.x = initialX + (e.rawX - touchX).toInt()
                        params!!.y = initialY + (e.rawY - touchY).toInt()
                        wm.updateViewLayout(view, params)
                    }
                }
                return true
            }
        })
        
        wm.addView(view, params)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val valor = intent?.getDoubleExtra("valor", 0.0) ?: 0.0
        val endEstab = intent?.getStringExtra("endEstab") ?: ""
        val endCliente = intent?.getStringExtra("endCliente") ?: ""
        
        if (valor > 0 && endEstab.isNotEmpty()) {
            calcularRotaCompleta(valor, endEstab, endCliente)
        }
        return START_STICKY
    }
    
    fun calcularRotaCompleta(valor: Double, endEstab: String, endCliente: String) {
        tvMin.text = "..."; tvHora.text = "..."; tvKm.text = "..."; tvLucro.text = "..."
        tvKmTotal.text = "Calculando rota..."
        
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                tvKmTotal.text = "Ativa o GPS"
                setarCor(0xFFFF3B30.toInt())
                return@addOnSuccessListener
            }
            
            val origem = "${location.latitude},${location.longitude}"
            val destino = if (endCliente.isNotEmpty()) endCliente else endEstab
            val waypoints = if (endCliente.isNotEmpty()) "&waypoints=${endEstab.replace(" ", "+")}" else ""
            
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=$origem&destination=${destino.replace(" ", "+")}$waypoints&key=SUA_CHAVE_AQUI"
            
            client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        tvKmTotal.text = "Erro ao calcular"
                        setarCor(0xFFFF3B30.toInt())
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val legs = routes.getJSONObject(0).getJSONArray("legs")
                        
                        var kmTotal = 0.0
                        var tempoSegTotal = 0
                        
                        for (i in 0 until legs.length()) {
                            val leg = legs.getJSONObject(i)
                            kmTotal += leg.getJSONObject("distance").getInt("value") / 1000.0
                            tempoSegTotal += leg.getJSONObject("duration").getInt("value")
                        }
                        
                        val tempoMin = tempoSegTotal / 60.0
                        val lucro = valor - (kmTotal * custoKm)
                        val porMin = if (tempoMin > 0) valor / tempoMin else 0.0
                        val porHora = porMin * 60
                        val porKm = if (kmTotal > 0) valor / kmTotal else 0.0
                        val cor = if (lucro >= 0) 0xFF39FF14.toInt() else 0xFFFF3B30.toInt()
                        
                        Handler(Looper.getMainLooper()).post {
                            tvMin.text = "%.2f".format(porMin).replace(".", ",")
                            tvHora.text = "%.0f".format(porHora)
                            tvKm.text = "%.2f".format(porKm).replace(".", ",")
                            tvLucro.text = "%.2f".format(lucro).replace(".", ",")
                            tvKmTotal.text = "Total: %.1f km | %.0f min".format(kmTotal, tempoMin)
                            setarCor(cor)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            tvKmTotal.text = "Rota não encontrada"
                            setarCor(0xFFFF3B30.toInt())
                        }
                    }
                }
            })
        }
    }
    
    fun setarCor(cor: Int) {
        tvMin.setTextColor(cor)
        tvHora.setTextColor(cor)
        tvKm.setTextColor(cor)
        tvLucro.setTextColor(cor)
    }
    
    override fun onDestroy() {
        try { 
            if (::view.isInitialized) wm.removeView(view) 
        } catch(e: Exception) {}
        super.onDestroy()
    }
    
    override fun onBind(i: Intent?) = null
}
