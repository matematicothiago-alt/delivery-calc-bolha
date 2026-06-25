package com.meuapp.lucroaovivo

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.location.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: LinearLayout
    private lateinit var txtMin: TextView
    private lateinit var txtHr: TextView  
    private lateinit var txtKm: TextView
    private lateinit var txtLucro: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    
    var valorCorrida = 0.0
    var enderecoEstab = ""
    var enderecoCliente = ""
    var minhaLat = 0.0
    var minhaLon = 0.0
    
    val GOOGLE_API_KEY = "SUA_CHAVE_AQUI" // TROCA PELA TUA CHAVE
    val CUSTO_POR_KM = 0.35
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        criarBolhaEstiloPrint()
        iniciarGPS()
    }
    
    fun criarBolhaEstiloPrint() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Container principal - fundo preto arredondado
        overlayView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#FF000000"))
            setPadding(8, 8, 8, 8)
        }
        
        // Função pra criar cada bloco: 0,75 /min
        fun criarBloco(): LinearLayout {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(16, 4, 16, 4)
            }
        }
        
        // Função pra criar número grande
        fun criarNumero(): TextView {
            return TextView(this).apply {
                text = "0,00"
                setTextColor(Color.parseColor("#FF00FF00")) // Verde
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
        }
        
        // Função pra criar label pequeno
        fun criarLabel(texto: String): TextView {
            return TextView(this).apply {
                text = texto
                setTextColor(Color.parseColor("#FF00FF00"))
                textSize = 12f
                gravity = Gravity.CENTER
            }
        }
        
        // BLOCO 1: /min
        val blocoMin = criarBloco()
        txtMin = criarNumero()
        blocoMin.addView(txtMin)
        blocoMin.addView(criarLabel("/min"))
        overlayView.addView(blocoMin)
        
        // Separador
        overlayView.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor("#FF333333"))
        })
        
        // BLOCO 2: /hr
        val blocoHr = criarBloco()
        txtHr = criarNumero()
        blocoHr.addView(txtHr)
        blocoHr.addView(criarLabel("/hr"))
        overlayView.addView(blocoHr)
        
        // Separador
        overlayView.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor("#FF333333"))
        })
        
        // BLOCO 3: /km
        val blocoKm = criarBloco()
        txtKm = criarNumero()
        blocoKm.addView(txtKm)
        blocoKm.addView(criarLabel("/km"))
        overlayView.addView(blocoKm)
        
        // Separador
        overlayView.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor("#FF333333"))
        })
        
        // BLOCO 4: Lucro
        val blocoLucro = criarBloco()
        txtLucro = criarNumero()
        blocoLucro.addView(txtLucro)
        blocoLucro.addView(criarLabel("Lucro (R$)"))
        overlayView.addView(blocoLucro)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = 100 }
        
        // Arrastar a bolha
        var initialX = 0; var initialY = 0; var initialTouchX = 0f; var initialTouchY = 0f
        overlayView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                else -> false
            }
        }
        windowManager.addView(overlayView, params)
    }
    
    @SuppressLint("MissingPermission")
    fun iniciarGPS() {
        val request = LocationRequest.create().apply {
            interval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    minhaLat = it.latitude
                    minhaLon = it.longitude
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            valorCorrida = it.getDoubleExtra("valor", 0.0)
            enderecoEstab = it.getStringExtra("endEstab") ?: ""
            enderecoCliente = it.getStringExtra("endCliente") ?: ""
            if (valorCorrida > 0 && enderecoEstab.isNotEmpty()) calcularRotaCompleta()
        }
        return START_STICKY
    }
    
    fun calcularRotaCompleta() {
        if (minhaLat == 0.0) {
            txtMin.text = "GPS"
            return
        }
        if (GOOGLE_API_KEY == "SUA_CHAVE_AQUI") {
            txtMin.text = "API"
            return
        }
        
        val origem1 = "$minhaLat,$minhaLon"
        val client = OkHttpClient()
        
        // TRECHO 1: Vc -> Estabelecimento
        val url1 = "https://maps.googleapis.com/maps/api/directions/json?origin=$origem1&destination=${enderecoEstab}&key=$GOOGLE_API_KEY"
        
        client.newCall(Request.Builder().url(url1).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = JSONObject(response.body?.string() ?: "")
                    val rota1 = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                    val dist1 = rota1.getJSONObject("distance").getInt("value") / 1000.0
                    val temp1 = rota1.getJSONObject("duration").getInt("value") / 60.0
                    
                    // TRECHO 2: Estabelecimento -> Cliente
                    val url2 = "https://maps.googleapis.com/maps/api/directions/json?origin=${enderecoEstab}&destination=${enderecoCliente}&key=$GOOGLE_API_KEY"
                    client.newCall(Request.Builder().url(url2).build()).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {}
                        override fun onResponse(call: Call, response: Response) {
                            try {
                                val json2 = JSONObject(response.body?.string() ?: "")
                                val rota2 = json2.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                                val dist2 = rota2.getJSONObject("distance").getInt("value") / 1000.0
                                val temp2 = rota2.getJSONObject("duration").getInt("value") / 60.0
                                
                                val kmTotal = dist1 + dist2
                                val minTotal = temp1 + temp2
                                val lucro = valorCorrida - (kmTotal * CUSTO_POR_KM)
                                val porMin = valorCorrida / minTotal
                                val porHora = porMin * 60
                                val porKm = valorCorrida / kmTotal
                                
                                val corFundo = if (lucro > 0) "#FF004400" else "#FF440000"
                                val corTexto = if (lucro > 0) "#FF00FF00" else "#FFFF0000"
                                
                                runOnUiThread {
                                    txtMin.text = String.format("%.2f", porMin).replace(".", ",")
                                    txtHr.text = String.format("%.0f", porHora)
                                    txtKm.text = String.format("%.2f", porKm).replace(".", ",")
                                    txtLucro.text = String.format("%.2f", lucro).replace(".", ",")
                                    
                                    // Muda cor de tudo
                                    overlayView.setBackgroundColor(Color.parseColor(corFundo))
                                    txtMin.setTextColor(Color.parseColor(corTexto))
                                    txtHr.setTextColor(Color.parseColor(corTexto))
                                    txtKm.setTextColor(Color.parseColor(corTexto))
                                    txtLucro.setTextColor(Color.parseColor(corTexto))
                                }
                            } catch (e: Exception) {}
                        }
                    })
                } catch (e: Exception) {}
            }
        })
    }
    
    fun runOnUiThread(action: () -> Unit) {
        android.os.Handler(mainLooper).post(action)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) windowManager.removeView(overlayView)
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
