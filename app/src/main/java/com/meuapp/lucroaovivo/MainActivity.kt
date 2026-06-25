package com.meuapp.lucroaovivo

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var etValor: EditText
    private lateinit var etKm: EditText
    private lateinit var btnCalcular: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etValor = findViewById(R.id.etValor)
        etKm = findViewById(R.id.etKm)
        btnCalcular = findViewById(R.id.btnCalcular)

        btnCalcular.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 123)
                Toast.makeText(this, "Ative a permissão e clique de novo", Toast.LENGTH_LONG).show()
            } else {
                calcularEChamarBolha()
            }
        }
    }

    private fun calcularEChamarBolha() {
        val valorStr = etValor.text.toString()
        val kmStr = etKm.text.toString()

        if (valorStr.isEmpty() || kmStr.isEmpty()) {
            Toast.makeText(this, "Preenche valor e KM", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, BolhaService::class.java)
        intent.putExtra("valor", valorStr)
        intent.putExtra("km", kmStr)
        startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                calcularEChamarBolha()
            }
        }
    }
}
