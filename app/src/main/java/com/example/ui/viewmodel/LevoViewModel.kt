package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Delivery
import com.example.data.model.OrderFulfillment
import com.example.data.model.OrderSourceKind
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethodKind
import com.example.data.model.SyncAction
import com.example.data.repository.LevoRepository
import com.example.data.repository.SyncResult
import com.example.notification.LevoNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class CourierProfile(
    val id: String = "MOT-8890",
    val name: String = "Gustavo Santana",
    val vehicle: String = "Honda CG 160 Cargo",
    val rating: Double = 4.9,
    val completedCount: Int = 142
)

enum class MainAppViewMode {
    DASHBOARD_PANEL, // Painel do Estabelecimento / Cozinha (estilo Levô Web)
    COURIER_ROUTE    // Modo "Minha Rota" do Motoboy (estilo courier-app.tsx)
}

class LevoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LevoRepository.getInstance(application)
    private val notificationManager = LevoNotificationManager(application)

    val courierProfile = MutableStateFlow(CourierProfile())
    val currentViewMode = MutableStateFlow(MainAppViewMode.DASHBOARD_PANEL)

    val rawDeliveries: StateFlow<List<Delivery>> = repository.allDeliveries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingActions: StateFlow<List<SyncAction>> = repository.pendingActions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingSyncCount: StateFlow<Int> = repository.pendingCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val searchQuery = MutableStateFlow("")
    val selectedStageFilter = MutableStateFlow<OrderStage?>(null)
    val selectedSourceFilter = MutableStateFlow<OrderSourceKind?>(null)

    val isSimulatedOffline = MutableStateFlow(false)
    val isSyncing = MutableStateFlow(false)
    val lastSyncMessage = MutableStateFlow<String?>("Conectado ao painel Levô")

    val backendUrl = MutableStateFlow("http://10.0.2.2:3000/")
    val fcmPushToken = MutableStateFlow("fcm_token_levo_native_${UUID.randomUUID().toString().take(12)}")

    val filteredDeliveries: StateFlow<List<Delivery>> = combine(
        rawDeliveries,
        searchQuery,
        selectedStageFilter,
        selectedSourceFilter
    ) { list, query, stageFilter, sourceFilter ->
        list.filter { delivery ->
            val matchesQuery = query.isBlank() ||
                    delivery.displayId.contains(query, ignoreCase = true) ||
                    delivery.customerName.contains(query, ignoreCase = true) ||
                    delivery.address.contains(query, ignoreCase = true) ||
                    delivery.itemsSummary.contains(query, ignoreCase = true) ||
                    (delivery.reference?.contains(query, ignoreCase = true) == true)

            val matchesStage = stageFilter == null || delivery.stage == stageFilter
            val matchesSource = sourceFilter == null || delivery.source == sourceFilter
            matchesQuery && matchesStage && matchesSource
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initSeedDataIfEmpty()
        }
    }

    fun setViewMode(mode: MainAppViewMode) {
        currentViewMode.value = mode
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setStageFilter(stage: OrderStage?) {
        selectedStageFilter.value = stage
    }

    fun setSourceFilter(source: OrderSourceKind?) {
        selectedSourceFilter.value = source
    }

    fun toggleSimulatedOffline() {
        val nextState = !isSimulatedOffline.value
        isSimulatedOffline.value = nextState
        lastSyncMessage.value = if (nextState) {
            "Modo Offline ativado: Todas as ações serão salvas localmente no Room."
        } else {
            "Modo Online restaurado: Pronto para sincronização com o painel Levô."
        }
    }

    fun setBackendUrl(url: String) {
        backendUrl.value = url
    }

    fun advanceOrderStage(deliveryId: String, nextStage: OrderStage) {
        viewModelScope.launch {
            val isOnline = !isSimulatedOffline.value
            repository.advanceOrderStage(
                deliveryId = deliveryId,
                nextStage = nextStage,
                isOnline = isOnline,
                baseUrl = backendUrl.value
            )

            val order = rawDeliveries.value.find { it.id == deliveryId }
            notificationManager.showStatusUpdateAlert(
                deliveryId = "#${order?.displayId ?: deliveryId}",
                statusText = nextStage.label,
                clientName = order?.customerName ?: "Cliente"
            )
        }
    }

    fun confirmDelivery(deliveryId: String, codeProvided: String, recipientName: String) {
        viewModelScope.launch {
            val isOnline = !isSimulatedOffline.value
            repository.confirmDeliveryProof(
                deliveryId = deliveryId,
                codeProvided = codeProvided,
                recipientName = recipientName,
                isOnline = isOnline,
                baseUrl = backendUrl.value
            )

            val order = rawDeliveries.value.find { it.id == deliveryId }
            notificationManager.showStatusUpdateAlert(
                deliveryId = "#${order?.displayId ?: deliveryId}",
                statusText = "Entregue com sucesso",
                clientName = recipientName
            )
        }
    }

    fun createManualFoodOrder(
        customerName: String,
        customerPhone: String,
        address: String,
        reference: String,
        itemsSummary: String,
        amountCents: Int,
        deliveryFeeCents: Int,
        paymentMethod: PaymentMethodKind,
        changeForCents: Int?,
        deliveryCode: String?,
        notes: String?,
        source: OrderSourceKind = OrderSourceKind.MANUAL
    ) {
        viewModelScope.launch {
            val randomDisplayId = (105..999).random().toString()
            val newOrder = Delivery(
                id = "ord_$randomDisplayId",
                displayId = randomDisplayId,
                source = source,
                fulfillment = OrderFulfillment.DELIVERY,
                customerName = customerName.ifBlank { "Cliente Balcão" },
                customerPhone = customerPhone.ifBlank { "(11) 99999-0000" },
                address = address.ifBlank { "Balcão do Restaurante" },
                reference = reference.ifBlank { null },
                establishmentName = "Hamburgueria & Pizzaria Levô",
                establishmentAddress = "Av. São João, 450 - Centro",
                itemsSummary = itemsSummary.ifBlank { "1× Combo Especial da Casa" },
                itemsDetailed = itemsSummary,
                amountCents = if (amountCents > 0) amountCents else 4500,
                deliveryFeeCents = deliveryFeeCents,
                paymentMethod = paymentMethod,
                changeForCents = changeForCents,
                stage = OrderStage.NEW,
                deliveryCode = deliveryCode?.ifBlank { null } ?: (1000..9999).random().toString(),
                courierName = "Gustavo Santana",
                urgent = false,
                notes = notes,
                isSynced = false
            )

            repository.createDelivery(
                delivery = newOrder,
                isOnline = !isSimulatedOffline.value,
                baseUrl = backendUrl.value
            )

            notificationManager.showNewDeliveryAlert(newOrder)
            lastSyncMessage.value = "Pedido #${newOrder.displayId} registrado e alertado via Push!"
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            isSyncing.value = true
            lastSyncMessage.value = "Sincronizando com o backend Levô..."

            val result = repository.syncAllPending(
                baseUrl = backendUrl.value,
                isSimulatedOffline = isSimulatedOffline.value
            )

            when (result) {
                is SyncResult.Success -> {
                    lastSyncMessage.value = result.message
                    if (result.count > 0) {
                        notificationManager.showSyncAlert(
                            itemsCount = result.count,
                            message = "Sincronizado com o painel Levô."
                        )
                    }
                }
                is SyncResult.Offline -> {
                    lastSyncMessage.value = result.reason
                }
                is SyncResult.Error -> {
                    lastSyncMessage.value = "Erro: ${result.message}"
                }
            }
            isSyncing.value = false
        }
    }

    fun simulatePushIncomingMarketplaceOrder(source: OrderSourceKind = OrderSourceKind.IFOOD) {
        viewModelScope.launch {
            val randomNum = (1370..9999).random().toString()
            val samplePicks = when (source) {
                OrderSourceKind.IFOOD -> Triple(
                    "Mariana Siqueira (iFood)",
                    "Alameda Santos, 1400, Apto 93 - Cerqueira César",
                    "2× Smash Melted Cheddar & Bacon · 1× Batata Frita Individual · 1× Coca-Cola Lata"
                )
                OrderSourceKind.AIQFOME -> Triple(
                    "Rodrigo Paiva (aiqfome)",
                    "Rua Bela Cintra, 890, Bloco A - Consolação",
                    "1× Marmitex Supremo de Picanha · 1× Suco de Maracujá 500ml"
                )
                OrderSourceKind.FOOD99 -> Triple(
                    "Camila Nogueira (99Food)",
                    "Rua Augusta, 2100 - Jardins",
                    "1× Pizza Média Quatro Queijos · 1× Guaraná Antarctica 1.5L"
                )
                OrderSourceKind.SITE -> Triple(
                    "Felipe Antunes (Cardápio Digital)",
                    "Av. Rebouças, 1550, Sala 402 - Pinheiros",
                    "1× Combo Duplo Artesanal (2 Lanches + 2 Batatas) · 2× Cervejas Long Neck"
                )
                OrderSourceKind.MANUAL -> Triple(
                    "Dra. Patrícia Mendes (WhatsApp)",
                    "Rua Oscar Freire, 1100 - Jardins",
                    "1× Salada Caesar com Tiras de Frango · 1× Água com Gás e Limão"
                )
            }

            val incomingOrder = Delivery(
                id = "ord_$randomNum",
                displayId = randomNum,
                source = source,
                fulfillment = OrderFulfillment.DELIVERY,
                customerName = samplePicks.first,
                customerPhone = "(11) 97700-1122",
                address = samplePicks.second,
                reference = "Interfone e portaria 24h",
                establishmentName = "Hamburgueria & Pizzaria Levô",
                establishmentAddress = "Av. São João, 450 - Centro",
                itemsSummary = samplePicks.third,
                itemsDetailed = samplePicks.third,
                amountCents = (4200..9800).random(),
                deliveryFeeCents = 800,
                paymentMethod = if (source == OrderSourceKind.IFOOD || source == OrderSourceKind.FOOD99)
                    PaymentMethodKind.ONLINE else PaymentMethodKind.PIX,
                stage = OrderStage.NEW,
                deliveryCode = (1000..9999).random().toString(),
                courierName = "Gustavo Santana",
                urgent = false,
                notes = "Pedido recebido em tempo real pela integração!",
                isSynced = true
            )

            repository.createDelivery(
                delivery = incomingOrder,
                isOnline = true,
                baseUrl = backendUrl.value
            )

            notificationManager.showNewDeliveryAlert(incomingOrder)
            lastSyncMessage.value = "🔔 Novo pedido #${incomingOrder.displayId} [${source.label}] recebido via push!"
        }
    }

    fun testRegisterPushToken() {
        viewModelScope.launch {
            val result = repository.registerPushToken(
                baseUrl = backendUrl.value,
                courierId = courierProfile.value.id,
                token = fcmPushToken.value
            )
            lastSyncMessage.value = result.getOrNull() ?: "Token registrado."
        }
    }
}
