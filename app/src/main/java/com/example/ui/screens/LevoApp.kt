package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.LevoViewModel

enum class MainDestination(val title: String) {
    DELIVERIES("Quadro Levô"),
    NEW_DELIVERY("Novo Pedido"),
    SYNC_PUSH("Sync & Push")
}

@Composable
fun LevoApp(
    viewModel: LevoViewModel = viewModel(),
    initialDeliveryId: String? = null
) {
    var currentDestination by remember { mutableStateOf(MainDestination.DELIVERIES) }
    var selectedDeliveryId by remember { mutableStateOf(initialDeliveryId) }

    val pendingCount by viewModel.pendingSyncCount.collectAsState()

    Scaffold(
        bottomBar = {
            if (selectedDeliveryId == null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.testTag("levo_bottom_navigation")
                    ) {
                        NavigationBarItem(
                            selected = currentDestination == MainDestination.DELIVERIES,
                            onClick = { currentDestination = MainDestination.DELIVERIES },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination == MainDestination.DELIVERIES) {
                                        Icons.Filled.DeliveryDining
                                    } else {
                                        Icons.Outlined.DeliveryDining
                                    },
                                    contentDescription = "Quadro Levô"
                                )
                            },
                            label = {
                                Text(
                                    text = "Quadro Levô",
                                    fontWeight = if (currentDestination == MainDestination.DELIVERIES) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_deliveries")
                        )

                        NavigationBarItem(
                            selected = currentDestination == MainDestination.NEW_DELIVERY,
                            onClick = { currentDestination = MainDestination.NEW_DELIVERY },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination == MainDestination.NEW_DELIVERY) {
                                        Icons.Filled.AddCircle
                                    } else {
                                        Icons.Outlined.AddCircleOutline
                                    },
                                    contentDescription = "Novo Pedido"
                                )
                            },
                            label = {
                                Text(
                                    text = "Novo Pedido",
                                    fontWeight = if (currentDestination == MainDestination.NEW_DELIVERY) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_new_delivery")
                        )

                        NavigationBarItem(
                            selected = currentDestination == MainDestination.SYNC_PUSH,
                            onClick = { currentDestination = MainDestination.SYNC_PUSH },
                            icon = {
                                BadgedBox(badge = {
                                    if (pendingCount > 0) {
                                        Badge { Text("$pendingCount") }
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (currentDestination == MainDestination.SYNC_PUSH) {
                                            Icons.Filled.Sync
                                        } else {
                                            Icons.Outlined.Sync
                                        },
                                        contentDescription = "Sincronização & Push"
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = "Sync & Push",
                                    fontWeight = if (currentDestination == MainDestination.SYNC_PUSH) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_sync_push")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (selectedDeliveryId != null) {
            DeliveryDetailScreen(
                deliveryId = selectedDeliveryId!!,
                viewModel = viewModel,
                onBack = { selectedDeliveryId = null },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "nav_transition",
                modifier = Modifier.padding(innerPadding)
            ) { dest ->
                when (dest) {
                    MainDestination.DELIVERIES -> DashboardScreen(
                        viewModel = viewModel,
                        onDeliveryClick = { id -> selectedDeliveryId = id },
                        onNavigateToNewOrder = { currentDestination = MainDestination.NEW_DELIVERY },
                        onNavigateToSync = { currentDestination = MainDestination.SYNC_PUSH }
                    )
                    MainDestination.NEW_DELIVERY -> NewDeliveryScreen(
                        viewModel = viewModel,
                        onDeliveryCreated = {
                            currentDestination = MainDestination.DELIVERIES
                        }
                    )
                    MainDestination.SYNC_PUSH -> SyncAndPushScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
