package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Delivery

class LevoNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_DELIVERIES = "levo_deliveries_channel"
        const val CHANNEL_SYNC = "levo_sync_channel"
        const val EXTRA_DELIVERY_ID = "extra_delivery_id"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val deliveriesChannel = NotificationChannel(
                CHANNEL_DELIVERIES,
                "Entregas e Despacho",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de novas entregas e alterações de rota vindas do painel web"
                enableVibration(true)
                setShowBadge(true)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Sincronização Offline",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Alertas de sincronização em segundo plano com o servidor Levo"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(deliveriesChannel)
            notificationManager.createNotificationChannel(syncChannel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showNewDeliveryAlert(delivery: Delivery) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DELIVERY_ID, delivery.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            delivery.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val feeFormatted = delivery.formattedFee
        val totalFormatted = delivery.formattedTotal
        val contentText = "#${delivery.displayId} · ${delivery.source.label} · ${delivery.customerName}"

        val notification = NotificationCompat.Builder(context, CHANNEL_DELIVERIES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🛵 Pedido #${delivery.displayId} [${delivery.source.label}]")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Cliente: ${delivery.customerName}\n" +
                        "Endereço: ${delivery.address}\n" +
                        "Itens: ${delivery.itemsSummary}\n" +
                        "Total: $totalFormatted (Taxa: $feeFormatted)\n" +
                        "Pagamento: ${delivery.paymentMethod.label}" +
                        if (delivery.deliveryCode != null) "\nCódigo do cliente: ${delivery.deliveryCode}" else ""
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(delivery.id.hashCode(), notification)
        } catch (_: SecurityException) {
            // Handled safely
        }
    }

    fun showStatusUpdateAlert(deliveryId: String, statusText: String, clientName: String) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_DELIVERIES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔄 Atualização: $deliveryId")
            .setContentText("Status atualizado para '$statusText' ($clientName)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(deliveryId.hashCode() + 1, notification)
        } catch (_: SecurityException) {
            // Handled safely
        }
    }

    fun showSyncAlert(itemsCount: Int, message: String) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("☁️ Sincronização Levo")
            .setContentText("$message ($itemsCount ações sincronizadas)")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(9999, notification)
        } catch (_: SecurityException) {
            // Handled safely
        }
    }
}
