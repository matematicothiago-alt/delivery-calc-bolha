package com.meuapp.lucroaovivo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.meuapp.lucroaovivo.R
import android.widget.*

data class Corrida(val valor: Double, val km: Double) // <- adiciona isso

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etValor = findViewById<EditText>(R.id.etValor)
        val btnBolha = findViewById<Button>(R.id.btnAtivarBolha)

        btnBolha.setOnClickListener {
            val valor = etValor.text.toString().toDoubleOrNull() ?: 0.0
            val corrida = Corrida(valor, 0.0) // <- linha 27 ok agora
            
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("valor", corrida.valor.toString()) // <- linha 31 força String
            startActivity(intent)
        }
    }
}
