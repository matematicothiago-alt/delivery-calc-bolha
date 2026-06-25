package com.meuapp.lucroaovivo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    
    val OVERLAY_REQUEST = 1234
    val LOCATION_REQUEST = 5678
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Pede Overlay
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Libera o Overlay", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, OVERLAY_REQUEST)
            return
        }
        
        // 2. Pede GPS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST)
            return
        }
        
        // 3. Se tudo OK, inicia
        startService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "LucroAoVivo ativo. Abre Uber/Bee/99", Toast.LENGTH_LONG).show()
        finish()
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(Intent(this, OverlayService::class.java))
                Toast.makeText(this, "GPS OK. Abre Uber/Bee/99", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Sem GPS não calcula a rota", Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OVERLAY_REQUEST) {
            if (Settings.canDrawOverlays(this)) {
                recreate() // volta pra pedir GPS
            } else {
                Toast.makeText(this, "Sem overlay não funciona", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
