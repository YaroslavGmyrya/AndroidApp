import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground() // Запускаем сервис с уведомлением
    }

    private fun startForeground() {
        // 1. Создаем канал уведомлений (для Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for foreground service"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // 2. Создаем уведомление
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Мой сервис")
            .setContentText("Работает в фоне")
            .setSmallIcon(R.drawable.ic_notification) // Добавьте свою иконку!
            .build()

        // 3. Запускаем сервис с уведомлением (ID должен быть > 0)
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Здесь можно выполнять фоновую работу
        return START_STICKY // Сервис перезапустится после убийства системы
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true) // Удаляем уведомление при остановке
    }

    companion object {
        private const val CHANNEL_ID = "foreground_service_channel"
        private const val NOTIFICATION_ID = 123

        fun startService(context: Context) {
            val intent = Intent(context, ForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ForegroundService::class.java)
            context.stopService(intent)
        }
    }
}