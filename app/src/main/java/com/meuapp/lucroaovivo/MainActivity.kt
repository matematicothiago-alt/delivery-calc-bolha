
package com.meuapp.lucroaovivo
import android.content.*
import android.os.*
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.meuapp.lucroaovivo.databinding.ActivityMainBinding
class MainActivity:AppCompatActivity(){
 lateinit var b:ActivityMainBinding
 override fun onCreate(s:Bundle?){super.onCreate(s)
 b=ActivityMainBinding.inflate(layoutInflater);setContentView(b.root)
 val sp=getSharedPreferences("cfg",MODE_PRIVATE)
 b.etCusto.setText(sp.getString("custo","1.20"))
 b.etVelocidade.setText(sp.getString("vel","60"))
 b.btnAtivar.setOnClickListener{
 sp.edit().putString("custo",b.etCusto.text.toString()).putString("vel",b.etVelocidade.text.toString()).apply()
 startService(Intent(this,OverlayService::class.java))
 }
 b.btnAcessibilidade.setOnClickListener{
 startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
 }
 }
}
