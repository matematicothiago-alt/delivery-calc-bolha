package com.meuapp.lucroaovivo

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AppAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val source = event?.source ?: return
        if (event.packageName != "com.ubercab.eats.driver") return
        
        var valor = 0.0
        var enderecoEstabelecimento = ""
        var enderecoCliente = ""
        
        buscarDados(source, 
            { v -> valor = v }, 
            { e -> enderecoEstabelecimento = e }, 
            { c -> enderecoCliente = c }
        )
        
        if (valor > 0 && enderecoEstabelecimento.isNotEmpty()) {
            val intent = Intent(this, OverlayService::class.java)
            intent.putExtra("valor", valor)
            intent.putExtra("endEstab", enderecoEstabelecimento)
            intent.putExtra("endCliente", enderecoCliente)
            startForegroundService(intent)
        }
    }
    
    fun buscarDados(node: AccessibilityNodeInfo, v: (Double) -> Unit, e: (String) -> Unit, c: (String) -> Unit) {
        node.text?.toString()?.let { texto ->
            // Valor: "R$ 12,50" ou "R$12.50"
            if (texto.contains("R$") && !texto.contains("total") && !texto.contains("Total")) {
                val valorLimpo = texto.replace("R$", "").replace(".", "").replace(",", ".").replace(" ", "").trim()
                v(valorLimpo.toDoubleOrNull() ?: 0.0)
            }
            // Estabelecimento
            if (texto.contains("Retirada") || texto.contains("Restaurante") || texto.contains("Loja") || texto.contains("Pickup")) {
                e(texto.replace("Retirada:", "").replace("Restaurante:", "").replace("Loja:", "").replace("Pickup:", "").trim())
            }
            // Cliente
            if (texto.contains("Entrega") || texto.contains("Cliente") || texto.contains("Destino") || texto.contains("Dropoff")) {
                c(texto.replace("Entrega:", "").replace("Cliente:", "").replace("Destino:", "").replace("Dropoff:", "").trim())
            }
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { buscarDados(it, v, e, c) }
        }
    }
    
    override fun onInterrupt() {}
}
