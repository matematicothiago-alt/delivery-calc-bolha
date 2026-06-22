package com.meuapp.lucroaovivo
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.meuapp.lucroaovivo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnNovaCorrida.setOnClickListener {
            startActivity(Intent(this, NovaCorridaActivity::class.java))
        }
        binding.btnConfig.setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
        }
        binding.btnResumo.setOnClickListener {
            startActivity(Intent(this, ResumoDiaActivity::class.java))
        }
    }
}
