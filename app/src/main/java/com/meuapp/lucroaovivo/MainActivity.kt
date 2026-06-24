package com.meuapp.lucroaovivo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.meuapp.lucroaovivo.R

data class Corrida(
    val valor: Double,
    val kmAteCliente: Double
)

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etValor = findViewById<EditText>(R.id.etValor)
        val etKmAteCliente = findViewById<EditText>(R.id.etKmAteCliente)
        val tvLucroKm = findViewById<TextView>(R.id.tvLucroKm)
        val btnAtivarBolha = findViewById<Button>(R.id.btnAtivarBolha)

        btnAtivarBolha.setOnClickListener {
            // Mostra um Toast pra provar que o botão funciona
            Toast.makeText(this, "Botão clicado!", Toast.LENGTH_SHORT).show()
            
            val valorStr = etValor.text.toString()
            val kmStr = etKmAteCliente.text.toString()
            
            // Se tiver vazio, avisa
            if (valorStr.isEmpty() || kmStr.isEmpty()) {
                tvLucroKm.text = "Preenche valor e km"
                return@setOnClickListener
            }
            
            val valor = valorStr.toDoubleOrNull() ?: 0.0
            val km = kmStr.toDoubleOrNull() ?: 0.0
            
            if (km == 0.0) {
                tvLucroKm.text = "Km não pode ser zero"
                return@setOnClickListener
            }
            
            val lucroKm = valor / km
            val corrida = Corrida(valor, km)
            
            // MOSTRA O RESULTADO NA TELA
            tvLucroKm.text = "R$ %.2f /km".format(lucroKm)
            
            // Muda cor: verde se > 2.00, vermelho se < 1.00
            if (lucroKm >= 2.0) {
                tvLucroKm.setTextColor(android.graphics.Color.GREEN)
            } else {
                tvLucroKm.setTextColor(android.graphics.Color.RED)
            }
        }
    }
}
