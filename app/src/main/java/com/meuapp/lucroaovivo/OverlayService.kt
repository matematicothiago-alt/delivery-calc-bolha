override fun onCreate() {
    super.onCreate()
    
    // ANDROID 14 FIX: Notificação primeiro nos 5s iniciais
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "lucro",
            "LucroAoVivo",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.setShowBadge(false)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    
    val notification = Notification.Builder(this, "lucro")
        .setContentTitle("LucroAoVivo ativo")
        .setContentText("Calculando lucro em tempo real")
        .setSmallIcon(android.R.drawable.ic_menu_info_details)
        .setOngoing(true)
        .build()
    
    startForeground(1, notification) // Agora é a 1ª coisa
    
    // Daqui pra baixo é teu código original igual
    registerReceiver(receiver, IntentFilter("LUCRO_UPDATE"), RECEIVER_NOT_EXPORTED)
    wm = getSystemService(WINDOW_SERVICE) as WindowManager
    view = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
    
    val p = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    )
    p.gravity = Gravity.TOP
    
    view.setOnTouchListener(object : View.OnTouchListener {
        var x = 0
        var y = 0
        var tx = 0f
        var ty = 0f
        
        override fun onTouch(v: View, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = p.x
                    y = p.y
                    tx = e.rawX
                    ty = e.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    p.x = x + (e.rawX - tx).toInt()
                    p.y = y + (e.rawY - ty).toInt()
                    wm.updateViewLayout(view, p)
                }
            }
            return true
        }
    })
    
    view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener { stopSelf() }
    wm.addView(view, p)
}
