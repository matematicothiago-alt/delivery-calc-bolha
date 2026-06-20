
package com.meuapp.lucroaovivo
import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.*
class OverlayService:Service(){
 lateinit var wm:WindowManager; lateinit var view:View
 private val receiver=object:BroadcastReceiver(){
  override fun onReceive(c:Context?,i:Intent?){ view.findViewById<TextView>(R.id.tvInfo).text=i?.getStringExtra("dados")?:"Aguardando detalhes..."}
 }
 override fun onCreate(){
  super.onCreate()
  registerReceiver(receiver,IntentFilter("LUCRO_UPDATE"),RECEIVER_NOT_EXPORTED)
  val ch=NotificationChannel("lucro","LucroAoVivo",NotificationManager.IMPORTANCE_LOW)
  getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
  startForeground(1,Notification.Builder(this,"lucro").setContentTitle("LucroAoVivo").setSmallIcon(android.R.drawable.ic_menu_info_details).build())
  wm=getSystemService(WINDOW_SERVICE) as WindowManager
  view=LayoutInflater.from(this).inflate(R.layout.overlay_layout,null)
  val p=WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,
  WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT)
  p.gravity=Gravity.TOP
  view.setOnTouchListener(object:View.OnTouchListener{
   var x=0;var y=0;var tx=0f;var ty=0f
   override fun onTouch(v:View,e:MotionEvent):Boolean{
    when(e.action){
      MotionEvent.ACTION_DOWN->{x=p.x;y=p.y;tx=e.rawX;ty=e.rawY}
      MotionEvent.ACTION_MOVE->{p.x=x+(e.rawX-tx).toInt();p.y=y+(e.rawY-ty).toInt();wm.updateViewLayout(view,p)}
    };return true}
  })
  view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener{stopSelf()}
  wm.addView(view,p)
 }
 override fun onDestroy(){unregisterReceiver(receiver);wm.removeView(view);super.onDestroy()}
 override fun onBind(i:Intent?)=null
}
