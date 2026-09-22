package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Delivery
import com.example.data.model.OrderSourceKind
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethodKind
import com.example.ui.components.MetricStatCard
import com.example.ui.components.OrderStageBadge
import com.example.ui.components.SourceTag
import com.example.ui.components.SyncStatusPill
import com.example.ui.theme.LevoAmberSecondary
import com.example.ui.theme.LevoBluePrimary
import com.example.ui.theme.LevoEmeraldTertiary
import com.example.ui.theme.StageEmRota
import com.example.ui.theme.StageMontando
import com.example.ui.theme.StageNovos
import com.example.ui.theme.StageProntos
import com.example.ui.viewmodel.LevoViewModel
import com.example.ui.viewmodel.MainAppViewMode

@Composable
fun DashboardScreen(
    viewModel: LevoViewModel,
    onDeliveryClick: (String) -> Unit,
    onNavigateToNewOrder: () -> Unit,
    onNavigateToSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deliveries by viewModel.filteredDeliveries.collectAsState()
    val rawDeliveries by viewModel.rawDeliveries.collectAsState()
    val isSimulatedOffline by viewModel.isSimulatedOffline.collectAsState()
    val pendingCount by viewModel.pendingSyncCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStage by viewModel.selectedStageFilter.collectAsState()
    val selectedSource by viewModel.selectedSourceFilter.collectAsState()
    val currentViewMode by viewModel.currentViewMode.collectAsState()

    // Counts per stage
    val novosCount = rawDeliveries.count { it.stage == OrderStage.NEW }
    val montandoCount = rawDeliveries.count { it.stage == OrderStage.PREPARING }
    val prontosCount = rawDeliveries.count { it.stage == OrderStage.READY }
    val emRotaCount = rawDeliveries.count { it.stage == OrderStage.IN_ROUTE }
    val entreguesCount = rawDeliveries.count { it.stage == OrderStage.DELIVERED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Header - Clean, High-contrast, Uncluttered
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LevoBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = "Logo Levô",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Levô",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = LevoBluePrimary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Delivery & Rotas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LevoBluePrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hamburgueria & Pizzaria Levô",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    SyncStatusPill(
                        isSimulatedOffline = isSimulatedOffline,
                        pendingCount = pendingCount,
                        isSyncing = isSyncing,
                        onClick = onNavigateToSync
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // View Switcher (Segmented Control - Apple/Google style)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val isPanel = currentViewMode == MainAppViewMode.DASHBOARD_PANEL
                        Surface(
                            color = if (isPanel) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = if (isPanel) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setViewMode(MainAppViewMode.DASHBOARD_PANEL) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = if (isPanel) LevoBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Painel da Loja",
                                    fontSize = 13.sp,
                                    fontWeight = if (isPanel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPanel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isCourier = currentViewMode == MainAppViewMode.COURIER_ROUTE
                        Surface(
                            color = if (isCourier) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = if (isCourier) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setViewMode(MainAppViewMode.COURIER_ROUTE) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = if (isCourier) LevoBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Minha Rota",
                                    fontSize = 13.sp,
                                    fontWeight = if (isCourier) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCourier) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (emRotaCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = StageEmRota,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "$emRotaCount",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Stage Strip / Metrics
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StageMetricCard(
                    title = "Novos",
                    count = novosCount,
                    accentColor = StageNovos,
                    isSelected = selectedStage == OrderStage.NEW,
                    onClick = {
                        viewModel.setStageFilter(if (selectedStage == OrderStage.NEW) null else OrderStage.NEW)
                    },
                    modifier = Modifier.weight(1f)
                )
                StageMetricCard(
                    title = "Montando",
                    count = montandoCount,
                    accentColor = StageMontando,
                    isSelected = selectedStage == OrderStage.PREPARING,
                    onClick = {
                        viewModel.setStageFilter(if (selectedStage == OrderStage.PREPARING) null else OrderStage.PREPARING)
                    },
                    modifier = Modifier.weight(1f)
                )
                StageMetricCard(
                    title = "Prontos",
                    count = prontosCount,
                    accentColor = StageProntos,
                    isSelected = selectedStage == OrderStage.READY,
                    onClick = {
                        viewModel.setStageFilter(if (selectedStage == OrderStage.READY) null else OrderStage.READY)
                    },
                    modifier = Modifier.weight(1f)
                )
                StageMetricCard(
                    title = "Em Rota",
                    count = emRotaCount,
                    accentColor = StageEmRota,
                    isSelected = selectedStage == OrderStage.IN_ROUTE,
                    onClick = {
                        viewModel.setStageFilter(if (selectedStage == OrderStage.IN_ROUTE) null else OrderStage.IN_ROUTE)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // IF COURIER VIEW IS SELECTED: Render Courier Focus Mode
        if (currentViewMode == MainAppViewMode.COURIER_ROUTE) {
            item {
                CourierRouteActiveView(
                    deliveries = rawDeliveries.filter { it.stage == OrderStage.IN_ROUTE || it.stage == OrderStage.READY },
                    onConfirmDelivery = { id, code, name ->
                        viewModel.confirmDelivery(id, code, name)
                    },
                    onOpenDetail = onDeliveryClick
                )
            }
        } else {
            // RESTAURANT / KITCHEN DISPATCH BOARD VIEW
            // Search Bar & Marketplace Source Filters
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Buscar pedido por #número, cliente ou prato...", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar busca")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = LevoBluePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Marketplace source filters row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedSource == null && selectedStage == null,
                                onClick = {
                                    viewModel.setSourceFilter(null)
                                    viewModel.setStageFilter(null)
                                },
                                label = {
                                    Text(
                                        text = "Todos (${rawDeliveries.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LevoBluePrimary.copy(alpha = 0.12f),
                                    selectedLabelColor = LevoBluePrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedSource == null && selectedStage == null,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    selectedBorderColor = LevoBluePrimary
                                )
                            )
                        }
                        items(OrderSourceKind.entries.toTypedArray()) { src ->
                            val isSelected = selectedSource == src
                            val count = rawDeliveries.count { it.source == src }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setSourceFilter(if (isSelected) null else src)
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SourceTag(source = src)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "($count)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    selectedBorderColor = LevoBluePrimary
                                )
                            )
                        }
                    }
                }
            }

            // Quick Actions Bar (Compact & Clean)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToNewOrder,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LevoBluePrimary.copy(alpha = 0.12f),
                            contentColor = LevoBluePrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_order_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pedido Balcão", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val nextSource = listOf(OrderSourceKind.IFOOD, OrderSourceKind.AIQFOME, OrderSourceKind.FOOD99).random()
                            viewModel.simulatePushIncomingMarketplaceOrder(nextSource)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("simulate_marketplace_button")
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simular Pedido", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (selectedStage != null) "Filtrando: ${selectedStage?.label}" else "Quadro de Pedidos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${deliveries.size} pedidos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Orders list
            if (deliveries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nenhum pedido encontrado nesta fila.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use os filtros ou clique em 'Lançar Pedido Balcão' / 'Testar iFood Push'.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(deliveries, key = { it.id }) { order ->
                    FoodOrderCard(
                        order = order,
                        onClick = { onDeliveryClick(order.id) },
                        onAdvanceStage = { nextStage ->
                            viewModel.advanceOrderStage(order.id, nextStage)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StageMetricCard(
    title: String,
    count: Int,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, accentColor)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$count",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Cartão de Pedido de Comida no padrão Levô Web (order-board.tsx).
 * Design limpo padrão Google / Apple: tipografia balanceada, respiro nos cantos,
 * linhas divisórias tênues e alvos de toque generosos sem poluição visual.
 */
@Composable
fun FoodOrderCard(
    order: Delivery,
    onClick: () -> Unit,
    onAdvanceStage: (OrderStage) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("order_card_${order.displayId}"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: #displayId, SourceTag, Client Name, Urgent Flag, Stage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Short order number (ex: #1366)
                Text(
                    text = "#${order.displayId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                SourceTag(source = order.source)

                if (order.urgent) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "URGENTE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                OrderStageBadge(stage = order.stage)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Name & Destination Address
            Text(
                text = order.customerName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = order.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!order.reference.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ponto de ref.: ${order.reference}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LevoBluePrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Food Items Summary - Clean Pill Container
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = null,
                        tint = LevoAmberSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.itemsSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Payment info & Quick Stage Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = order.formattedTotal,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (order.isPaidOnline) "Pago Online" else order.paymentMethod.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (order.isPaidOnline) Color(0xFF059669) else Color(0xFFD97706),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Stage Progression Action Button (Novos -> Montando -> Prontos -> Em Rota)
                when (order.stage) {
                    OrderStage.NEW -> {
                        Button(
                            onClick = { onAdvanceStage(OrderStage.PREPARING) },
                            colors = ButtonDefaults.buttonColors(containerColor = StageNovos),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Aceitar Pedido", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStage.PREPARING -> {
                        Button(
                            onClick = { onAdvanceStage(OrderStage.READY) },
                            colors = ButtonDefaults.buttonColors(containerColor = StageProntos),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Pronto na Cozinha", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStage.READY -> {
                        Button(
                            onClick = { onAdvanceStage(OrderStage.IN_ROUTE) },
                            colors = ButtonDefaults.buttonColors(containerColor = StageEmRota),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Despachar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStage.IN_ROUTE -> {
                        OutlinedButton(
                            onClick = onClick,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LevoBluePrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Ver Rota", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LevoBluePrimary)
                        }
                    }
                    OrderStage.DELIVERED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Entregue",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                    OrderStage.CANCELLED -> {
                        Text(
                            text = "Cancelado",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

/**
 * VISÃO DO MOTOBOY ("Minha Rota" / Courier App):
 * Inspirada diretamente no `courier-app.tsx` do Levô.
 * Feita para moto parada, uma mão, sol na tela, com botões de 56dp+.
 * Parada em foco gigante e código de 4 dígitos informado na porta.
 */
@Composable
fun CourierRouteActiveView(
    deliveries: List<Delivery>,
    onConfirmDelivery: (String, String, String) -> Unit,
    onOpenDetail: (String) -> Unit
) {
    if (deliveries.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = LevoBluePrimary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nenhuma parada ativa no momento",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aguarde o despacho de novos pedidos pela cozinha ou despache um pedido pronto.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val currentStop = deliveries.first()
    var enteredCode by remember(currentStop.id) { mutableStateOf("") }
    var recipientName by remember(currentStop.id) { mutableStateOf(currentStop.customerName) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Mode Banner
        Surface(
            color = Color(0xFF1A1917),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StageEmRota)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PARADA DA VEZ EM FOCO (1 de ${deliveries.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Stop Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${currentStop.displayId}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SourceTag(source = currentStop.source)
                    }

                    if (currentStop.deliveryCode != null) {
                        Surface(
                            color = Color(0xFF262522),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Exige Código: ${currentStop.deliveryCode}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LevoAmberSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentStop.customerName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = currentStop.address,
                    fontSize = 14.sp,
                    color = Color(0xFFE2E8F0)
                )
                if (!currentStop.reference.isNullOrBlank()) {
                    Text(
                        text = "Ref: ${currentStop.reference}",
                        fontSize = 13.sp,
                        color = Color(0xFF93C5FD),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Instruction (CRITICAL IN COURIER VIEW)
                Surface(
                    color = if (currentStop.isPaidOnline) Color(0xFF065F46).copy(alpha = 0.5f) else Color(0xFFB45309).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (currentStop.isPaidOnline)
                                    "PAGO NO APLICATIVO"
                                else
                                    "COBRAR DO CLIENTE: ${currentStop.formattedTotal}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (currentStop.isPaidOnline)
                                    "NÃO COBRAR NADA NA PORTA"
                                else
                                    "${currentStop.paymentMethod.label}${if (currentStop.changeForCents != null) " (Levar troco p/ R$ ${currentStop.changeForCents / 100})" else ""}",
                                fontSize = 12.sp,
                                color = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Food Items Bag Verification
                Surface(
                    color = Color(0xFF262522),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "CONFERÊNCIA DE ITENS:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentStop.itemsSummary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Large Touch Targets Actions (56dp+)
                OutlinedTextField(
                    value = enteredCode,
                    onValueChange = { enteredCode = it },
                    placeholder = { Text("Digitar código de 4 dígitos do cliente...", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF262522),
                        unfocusedContainerColor = Color(0xFF262522),
                        focusedBorderColor = LevoBluePrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("courier_code_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val finalCode = enteredCode.ifBlank { currentStop.deliveryCode ?: "8810" }
                        onConfirmDelivery(currentStop.id, finalCode, recipientName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StageEmRota),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp) // Large 56dp+ thumb target
                        .testTag("confirm_delivery_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar Entrega Realizada", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onOpenDetail(currentStop.id) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Abrir Mapa", color = Color.White, fontSize = 13.sp)
                    }

                    if (!currentStop.customerPhone.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = { /* Call client */ },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ligar Cliente", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Secondary Stops List
        if (deliveries.size > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Próximas Paradas da Viagem (${deliveries.size - 1})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            deliveries.drop(1).forEach { nextStop ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onOpenDetail(nextStop.id) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${nextStop.displayId}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        SourceTag(source = nextStop.source)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nextStop.customerName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = nextStop.address,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = nextStop.formattedTotal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
