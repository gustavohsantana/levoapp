package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.notification.LevoNotificationManager
import com.example.ui.screens.LevoApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val initialDeliveryId = intent?.getStringExtra(LevoNotificationManager.EXTRA_DELIVERY_ID)

    setContent {
      MyApplicationTheme {
        LevoApp(initialDeliveryId = initialDeliveryId)
      }
    }
  }
}

