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
    private val OVERLAY_PERMISSION_REQUEST_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etValor = findViewById(R.id.etValor)
        etKm = findViewById(R.id.etKm)
        btnCalcular = findViewById(R.id.btnCalcular)

        btnCalcular.setOnClickListener {
            if (checkOverlayPermission()) {
                mostrarBolha()
            } else {
                requestOverlayPermission()
            }
        }
    }

    private fun mostrarBolha() {
        val valorStr = etValor.text.toString()
        val kmStr = etKm.text.toString()

        if (valorStr.isEmpty() || kmStr.isEmpty()) {
            Toast.makeText(this, "Preencha valor e km", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, BolhaService::class.java)
        intent.putExtra("valor", valorStr)
        intent.putExtra("km", kmStr)
        startService(intent)
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (checkOverlayPermission()) {
                mostrarBolha()
            } else {
                Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
