package com.meuapp.lucroaovivo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.provider.Settings

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val etValor = findViewById<EditText>(R.id.etValor)
        val etKmAteCliente = findViewById<EditText>(R.id.etKmAteCliente)
        val btnAtivar = findViewById<Button>(R.id.btnAtivarBolha)
        val btnAcessibilidade = findViewById<Button>(R.id.btnAcessibilidade)
        
        // Botão 1: Ativar a bolha com cálculo
        btnAtivar.setOnClickListener {
            val valor = etValor.text.toString().toDoubleOrNull() ?: 0.0
            val kmAteCliente = etKmAteCliente.text.toString().toDoubleOrNull() ?: 0.0
            
            // Km da corrida vai vir do AppAccessibilityService
            // Por enquanto manda 0, o service atualiza depois
            val corrida = Corrida(valor, 0.0, kmAteCliente)
            val lucro = corrida.calcularLucro()
            
            val intent = Intent(this, OverlayService::class.java)
            intent.putExtra("lucro", lucro)
            startForegroundService(intent)
        }
        
        // Botão 2: Abrir tela de acessibilidade
        btnAcessibilidade.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}
