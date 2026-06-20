
package com.meuapp.lucroaovivo
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
class AppAccessibilityService:AccessibilityService(){
 private val apps=setOf("com.beedelivery.driver","com.uber.driver","com.taxi99.driver","com.inride.app")
 override fun onAccessibilityEvent(e:AccessibilityEvent){
  if(!apps.contains(e.packageName?.toString())) return
  sendBroadcast(Intent("LUCRO_UPDATE").putExtra("dados","Dados detectados"))
 }
 override fun onInterrupt(){}
}
