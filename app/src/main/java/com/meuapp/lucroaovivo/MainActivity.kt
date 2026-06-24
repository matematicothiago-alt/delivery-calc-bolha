package com.meuapp.lucroaovivo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
        }
        
        val tvTitulo = TextView(this).apply { 
            text = "LucroAoVivo"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        
        val etValor = EditText(this).apply { 
            hint = "Valor da corrida R$"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etKmEstabelecimento = EditText(this).apply { 
            hint = "Km: você até o estabelecimento"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etKmAteCliente = EditText(this).apply { 
            hint = "Km: estabelecimento até cliente"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etKmViagem = EditText(this).apply { 
            hint = "Km: viagem com cliente"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val btn = Button(this).apply { text = "CALCULAR E ATIVAR BOLHA" }
        
        layout.addView(tvTitulo)
        layout.addView(etValor)
        layout.addView(etKmEstabelecimento)
        layout.addView(etKmAteCliente)
        layout.addView(etKmViagem)
        layout.addView(btn)
        setContentView(layout)
        
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        
        btn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Libera permissão de overlay primeiro", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                return@setOnClickListener
            }
            
            val valor = etValor.text.toString().toDoubleOrNull() ?: 0.0
            val kmEstabelecimento = etKmEstabelecimento.text.toString().toDoubleOrNull() ?: 0.0
            val kmAteCliente = etKmAteCliente.text.toString().toDoubleOrNull() ?: 0.0
            val kmViagem = etKmViagem.text.toString().toDoubleOrNull() ?: 0.0
            
            if (valor == 0.0) {
                Toast.makeText(this, "Preenche o valor da corrida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val custoKm = 0.35 // teu custo por km - muda aqui
            val kmTotal = kmEstabelecimento + kmAteCliente + kmViagem
            val lucro = valor - (kmTotal * custoKm)
            val deuLucro = lucro >= 0
            
            val intent = Intent(this, OverlayService::class.java)
            intent.putExtra("lucro", "${"%.2f".format(lucro)}")
            intent.putExtra("deuLucro", deuLucro)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            
            Toast.makeText(this, "Bolha ativada!", Toast.LENGTH_SHORT).show()
        }
    }
}
