package com.meuapp.lucroaovivo

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AppAccessibilityService : AccessibilityService() {

    private val PACOTES_ALVO = listOf(
        "com.ubercab.driver",           // Uber Driver
        "br.com.99.motorista",          // 99 Motorista
        "com.internationalpassenger"     // inDrive
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName !in PACOTES_ALVO) return

        val rootNode = rootInActiveWindow ?: return
        val valor = extrairValor(rootNode)
        val km = extrairKm(rootNode)

        if (valor > 0 && km > 0) {
            val intent = Intent(this, BolhaService::class.java)
            intent.putExtra("valor", valor.toString())
            intent.putExtra("km", km.toString())
            startService(intent)
        }
    }

    private fun extrairValor(node: AccessibilityNodeInfo): Double {
        val textoCompleto = buscarTexto(node)
        val regex = Regex("""R\$\s*(\d+[,.]?\d*)""")
        val match = regex.find(textoCompleto)
        return match?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    }

    private fun extrairKm(node: AccessibilityNodeInfo): Double {
        val textoCompleto = buscarTexto(node)
        val regex = Regex("""(\d+[,.]?\d*)\s*km""", RegexOption.IGNORE_CASE)
        val match = regex.find(textoCompleto)
        return match?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    }

    private fun buscarTexto(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        if (node.text != null) sb.append(node.text).append(" ")
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { sb.append(buscarTexto(it)) }
        }
        return sb.toString()
    }

    override fun onInterrupt() {}
}
