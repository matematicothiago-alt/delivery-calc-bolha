package com.meuapp.lucroaovivo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        val etValor = EditText(this).apply { hint = "Valor: R$ 15.50" }
        val etKmAte = EditText(this).apply { hint = "KM até cliente: 2.5" }
        val etKmTotal = EditText(this).apply { hint = "KM viagem: 8.0" }
        val btn = Button(this).apply { text = "CALCULAR E MOSTRAR BOLHA" }
        
        layout.addView(etValor); layout.addView(etKmAte); layout.addView(etKmTotal); layout.addView(btn)
        setContentView(layout)
        
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        
        btn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Libera overlay primeiro", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                return@setOnClickListener
            }
            
            val valor = etValor.text.toString().toDoubleOrNull() ?: 0.0
            val kmAte = etKmAte.text.toString().toDoubleOrNull() ?: 0.0
            val kmTotal = etKmTotal.text.toString().toDoubleOrNull() ?: 0.0
            
            val custoKm = 0.35 // teu custo por km
            val lucro = valor - ((kmTotal + kmAte) * custoKm)
            
            val intent = Intent(this, OverlayService::class.java)
            intent.putExtra("lucro", "Lucro: R$ ${"%.2f".format(lucro)}")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }
}
