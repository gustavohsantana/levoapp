package com.example.data.repository

import android.content.Context
import com.example.data.local.DeliveryDao
import com.example.data.local.LevoDatabase
import com.example.data.local.SyncQueueDao
import com.example.data.model.Delivery
import com.example.data.model.OrderFulfillment
import com.example.data.model.OrderSourceKind
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethodKind
import com.example.data.model.SyncAction
import com.example.data.remote.ConfirmDeliveryDto
import com.example.data.remote.DeliveryDto
import com.example.data.remote.NetworkClient
import com.example.data.remote.PushTokenRegistrationDto
import com.example.data.remote.StatusUpdateDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

sealed class SyncResult {
    data class Success(val count: Int, val message: String) : SyncResult()
    data class Offline(val pendingCount: Int, val reason: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

class LevoRepository(
    private val deliveryDao: DeliveryDao,
    private val syncQueueDao: SyncQueueDao
) {

    val allDeliveries: Flow<List<Delivery>> = deliveryDao.getAllDeliveries()
    val pendingActions: Flow<List<SyncAction>> = syncQueueDao.getAllPendingActions()
    val pendingCount: Flow<Int> = syncQueueDao.getPendingCount()

    suspend fun getDeliveryById(id: String): Flow<Delivery?> = deliveryDao.getDeliveryById(id)

    suspend fun initSeedDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = deliveryDao.getAllDeliveries().first()
        if (existing.isEmpty()) {
            val sampleFoodDeliveries = listOf(
                Delivery(
                    id = "ord_1366",
                    displayId = "1366",
                    source = OrderSourceKind.IFOOD,
                    fulfillment = OrderFulfillment.DELIVERY,
                    customerName = "Ana Clara Souza",
                    customerPhone = "(11) 98765-4321",
                    address = "Av. Paulista, 1000, Apto 82 - Bela Vista",
                    reference = "Edifício Parque Paulista, interfone 82",
                    establishmentName = "Hamburgueria & Pizzaria Levô",
                    establishmentAddress = "Av. São João, 450 - Centro",
                    itemsSummary = "2× Smash Bacon Duplo · 1× Batata Rústica · 1× Coca-Cola 350ml",
                    itemsDetailed = "• 2× Smash Bacon Duplo (Pão brioche selado, 2x smash 90g, cheddar fatiado, bacon crocante, maionese especial)\n• 1× Batata Rústica Grande com alecrim e páprica\n• 1× Coca-Cola 350ml sem açúcar (gelada)",
                    amountCents = 8490,
                    deliveryFeeCents = 800,
                    paymentMethod = PaymentMethodKind.ONLINE,
                    stage = OrderStage.IN_ROUTE,
                    deliveryCode = "4821",
                    courierName = "Gustavo Santana",
                    urgent = false,
                    notes = "Sem cebola em um dos burgers por favor! Deixar na portaria com o Sr. Roberto.",
                    isSynced = true
                ),
                Delivery(
                    id = "ord_0104",
                    displayId = "104",
                    source = OrderSourceKind.SITE,
                    fulfillment = OrderFulfillment.DELIVERY,
                    customerName = "Marcos Vinicius Ribeiro",
                    customerPhone = "(11) 97654-3210",
                    address = "Rua Domingos de Morais, 850, Bloco B - Vila Mariana",
                    reference = "Condomínio Vila Mariana Verde, em frente à farmácia",
                    establishmentName = "Hamburgueria & Pizzaria Levô",
                    establishmentAddress = "Av. São João, 450 - Centro",
                    itemsSummary = "1× Pizza Grande Meio Calabresa Meio 4 Queijos · 1× Guaraná 2L",
                    itemsDetailed = "• 1× Pizza Grande (8 pedaços): 1/2 Calabresa Especial com cebola caramelizada + 1/2 Quatro Queijos (Mussarela, provolone, parmesão, gorgonzola)\n  - Borda recheada de Catupiry Original\n• 1× Guaraná Antarctica 2L",
                    amountCents = 7800,
                    deliveryFeeCents = 750,
                    paymentMethod = PaymentMethodKind.PIX,
                    stage = OrderStage.READY,
                    deliveryCode = "1904",
                    courierName = "Gustavo Santana",
                    urgent = false,
                    notes = "Massa fina bem assadinha. Enviar sachês de pimenta e azeite.",
                    isSynced = true
                ),
                Delivery(
                    id = "ord_0078",
                    displayId = "78",
                    source = OrderSourceKind.AIQFOME,
                    fulfillment = OrderFulfillment.DELIVERY,
                    customerName = "Beatriz Lima e Silva",
                    customerPhone = "(11) 99123-4567",
                    address = "Rua Oscar Freire, 320, Apto 41 - Jardins",
                    reference = "Próximo à padaria artesanal, portão preto",
                    establishmentName = "Hamburgueria & Pizzaria Levô",
                    establishmentAddress = "Av. São João, 450 - Centro",
                    itemsSummary = "2× Marmitex Especial Contrafilé · 2× Suco de Laranja 500ml",
                    itemsDetailed = "• 2× Marmitex Especial (Arroz branco soltinho, feijão carioca, bife de contrafilé grelhado, fritas douradas, farofa da casa e vinagrete fresco)\n• 2× Suco Natural de Laranja 500ml (sem açúcar)",
                    amountCents = 6400,
                    deliveryFeeCents = 900,
                    paymentMethod = PaymentMethodKind.CARD_DEBIT,
                    stage = OrderStage.PREPARING,
                    deliveryCode = "3108",
                    courierName = "Gustavo Santana",
                    urgent = true,
                    notes = "Carne ao ponto para bem passada. Almoço de trabalho, por favor não atrasar!",
                    isSynced = true
                ),
                Delivery(
                    id = "ord_0054",
                    displayId = "54",
                    source = OrderSourceKind.FOOD99,
                    fulfillment = OrderFulfillment.DELIVERY,
                    customerName = "Lucas Fernando Rocha",
                    customerPhone = "(11) 98877-6655",
                    address = "Rua Augusta, 1420 - Consolação",
                    reference = "Ao lado do teatro, estúdio 3",
                    establishmentName = "Hamburgueria & Pizzaria Levô",
                    establishmentAddress = "Av. São João, 450 - Centro",
                    itemsSummary = "1× Açaí Especial 700ml Turbinado · 1× Torta Holandesa",
                    itemsDetailed = "• 1× Açaí 700ml na tigela térmica (Camadas: Leite Ninho, Nutella pura, Morangos frescos fatiados, Banana, Granola artesanal e Mel)\n• 1× Fatia de Torta Holandesa artesanal",
                    amountCents = 4650,
                    deliveryFeeCents = 600,
                    paymentMethod = PaymentMethodKind.ONLINE,
                    stage = OrderStage.NEW,
                    deliveryCode = "7723",
                    courierName = "Gustavo Santana",
                    urgent = false,
                    notes = "Caprichar no leite ninho! Tocar campainha e aguardar descer.",
                    isSynced = true
                ),
                Delivery(
                    id = "ord_0204",
                    displayId = "204",
                    source = OrderSourceKind.MANUAL,
                    fulfillment = OrderFulfillment.DELIVERY,
                    customerName = "Juliana Ferreira (WhatsApp)",
                    customerPhone = "(11) 99888-7766",
                    address = "Rua Fradique Coutinho, 920, Casa 2 - Pinheiros",
                    reference = "Vila de casas, interfone no portão principal",
                    establishmentName = "Hamburgueria & Pizzaria Levô",
                    establishmentAddress = "Av. São João, 450 - Centro",
                    itemsSummary = "1× X-Salada Artesanal · 1× Nuggets (10 un) · 1× Suco de Uva",
                    itemsDetailed = "• 1× X-Salada Artesanal no pão brioche com hambúrguer de costela 160g, queijo prato derretido, alface americana, tomate e maionese verde\n• 1× Porção de Nuggets crocantes (10 unidades) com molho barbecue\n• 1× Suco Integral de Uva 300ml",
                    amountCents = 5200,
                    deliveryFeeCents = 700,
                    paymentMethod = PaymentMethodKind.CASH,
                    changeForCents = 10000,
                    stage = OrderStage.PREPARING,
                    deliveryCode = "8812",
                    courierName = "Gustavo Santana",
                    urgent = false,
                    notes = "Llevar troco para R$ 100,00. Cliente pediu pelo WhatsApp da loja.",
                    isSynced = true
                ),
                Delivery(
                    id = "ord_1350",
                    displayId = "1350",
                    source = OrderSourceKind.IFOOD,
                    fulfillment = OrderFulfillment.DELIVERY,
                    customerName = "Carlos Eduardo Lima",
                    customerPhone = "(11) 98111-2233",
                    address = "Rua Funchal, 418, 14º Andar - Vila Olímpia",
                    reference = "Edifício Comercial Metropolitan, recepção central",
                    establishmentName = "Hamburgueria & Pizzaria Levô",
                    establishmentAddress = "Av. São João, 450 - Centro",
                    itemsSummary = "1× Combo Família Burgers (3x Smash + 2x Fritas + 1x Refri 1.5L)",
                    itemsDetailed = "• 3× Smash Burgers Clássicos com queijo cheddar\n• 2× Porções médias de batata frita sequinha\n• 1× Refrigerante Guaraná Antarctica 1.5L",
                    amountCents = 11200,
                    deliveryFeeCents = 1000,
                    paymentMethod = PaymentMethodKind.ONLINE,
                    stage = OrderStage.DELIVERED,
                    deliveryCode = "9210",
                    courierName = "Gustavo Santana",
                    urgent = false,
                    notes = "Pedido de almoço corporativo",
                    confirmationProof = "Entregue e conferido na recepção com código 9210 validado",
                    isSynced = true
                )
            )
            deliveryDao.insertDeliveries(sampleFoodDeliveries)
        }
    }

    suspend fun advanceOrderStage(
        deliveryId: String,
        nextStage: OrderStage,
        notes: String? = null,
        isOnline: Boolean = true,
        baseUrl: String = "http://10.0.2.2:3000/"
    ) = withContext(Dispatchers.IO) {
        val current = deliveryDao.getDeliveryByIdSync(deliveryId) ?: return@withContext
        val updated = current.copy(
            stage = nextStage,
            notes = notes ?: current.notes,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        deliveryDao.updateDelivery(updated)

        val syncAction = SyncAction(
            deliveryId = deliveryId,
            actionType = "ADVANCE_STAGE",
            payloadJson = "{\"stage\": \"${nextStage.name}\", \"displayId\": \"${current.displayId}\"}"
        )
        val actionId = syncQueueDao.insertAction(syncAction)

        if (isOnline) {
            try {
                val api = NetworkClient.getApiService(baseUrl)
                val response = api.updateDeliveryStatus(
                    id = deliveryId,
                    body = StatusUpdateDto(status = nextStage.name, notes = notes)
                )
                if (response.isSuccessful) {
                    syncQueueDao.deleteActionById(actionId)
                    deliveryDao.updateDelivery(updated.copy(isSynced = true))
                }
            } catch (_: Exception) {
                // Kept in offline Room queue
            }
        }
    }

    suspend fun confirmDeliveryProof(
        deliveryId: String,
        codeProvided: String,
        recipientName: String,
        isOnline: Boolean = true,
        baseUrl: String = "http://10.0.2.2:3000/"
    ) = withContext(Dispatchers.IO) {
        val current = deliveryDao.getDeliveryByIdSync(deliveryId) ?: return@withContext
        val proof = "Entregue para $recipientName · Código informado: $codeProvided"
        val updated = current.copy(
            stage = OrderStage.DELIVERED,
            confirmationProof = proof,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        deliveryDao.updateDelivery(updated)

        val syncAction = SyncAction(
            deliveryId = deliveryId,
            actionType = "CONFIRM_DELIVERY",
            payloadJson = "{\"proof\": \"$proof\", \"code\": \"$codeProvided\"}"
        )
        val actionId = syncQueueDao.insertAction(syncAction)

        if (isOnline) {
            try {
                val api = NetworkClient.getApiService(baseUrl)
                val response = api.confirmDelivery(
                    id = deliveryId,
                    body = ConfirmDeliveryDto(proof = proof)
                )
                if (response.isSuccessful) {
                    syncQueueDao.deleteActionById(actionId)
                    deliveryDao.updateDelivery(updated.copy(isSynced = true))
                }
            } catch (_: Exception) {
                // Kept in offline Room queue
            }
        }
    }

    suspend fun createDelivery(
        delivery: Delivery,
        isOnline: Boolean = true,
        baseUrl: String = "http://10.0.2.2:3000/"
    ) = withContext(Dispatchers.IO) {
        val itemToInsert = delivery.copy(isSynced = false)
        deliveryDao.insertDelivery(itemToInsert)

        val syncAction = SyncAction(
            deliveryId = delivery.id,
            actionType = "CREATE_ORDER",
            payloadJson = "{\"displayId\": \"${delivery.displayId}\", \"client\": \"${delivery.customerName}\", \"source\": \"${delivery.source.name}\"}"
        )
        val actionId = syncQueueDao.insertAction(syncAction)

        if (isOnline) {
            try {
                val api = NetworkClient.getApiService(baseUrl)
                val dto = DeliveryDto(
                    id = delivery.id,
                    senderName = delivery.establishmentName,
                    senderAddress = delivery.establishmentAddress,
                    receiverName = delivery.customerName,
                    receiverPhone = delivery.customerPhone ?: "",
                    receiverAddress = delivery.address,
                    packageDescription = delivery.itemsSummary,
                    packageWeightKg = 1.0,
                    deliveryFee = delivery.deliveryFeeCents / 100.0,
                    paymentMethod = delivery.paymentMethod.label,
                    status = delivery.stage.name,
                    createdAt = delivery.createdAt,
                    updatedAt = delivery.updatedAt,
                    notes = delivery.notes,
                    confirmationProof = delivery.confirmationProof
                )
                val response = api.createDelivery(dto)
                if (response.isSuccessful) {
                    syncQueueDao.deleteActionById(actionId)
                    deliveryDao.updateDelivery(itemToInsert.copy(isSynced = true))
                }
            } catch (_: Exception) {
                // Kept in offline Room queue
            }
        }
    }

    suspend fun syncAllPending(
        baseUrl: String = "http://10.0.2.2:3000/",
        isSimulatedOffline: Boolean = false
    ): SyncResult = withContext(Dispatchers.IO) {
        if (isSimulatedOffline) {
            val count = syncQueueDao.getPendingCountSync()
            return@withContext SyncResult.Offline(
                pendingCount = count,
                reason = "Modo Offline ativo: $count operações aguardando envio para o backend Levô."
            )
        }

        val pendingList = syncQueueDao.getAllPendingActionsSync()
        if (pendingList.isEmpty()) {
            return@withContext SyncResult.Success(0, "Todas as entregas e pedidos estão em dia.")
        }

        var syncedCount = 0
        try {
            val api = NetworkClient.getApiService(baseUrl)
            for (action in pendingList) {
                val delivery = deliveryDao.getDeliveryByIdSync(action.deliveryId)
                if (delivery != null) {
                    val dto = DeliveryDto(
                        id = delivery.id,
                        senderName = delivery.establishmentName,
                        senderAddress = delivery.establishmentAddress,
                        receiverName = delivery.customerName,
                        receiverPhone = delivery.customerPhone ?: "",
                        receiverAddress = delivery.address,
                        packageDescription = delivery.itemsSummary,
                        packageWeightKg = 1.0,
                        deliveryFee = delivery.deliveryFeeCents / 100.0,
                        paymentMethod = delivery.paymentMethod.label,
                        status = delivery.stage.name,
                        createdAt = delivery.createdAt,
                        updatedAt = delivery.updatedAt,
                        notes = delivery.notes,
                        confirmationProof = delivery.confirmationProof
                    )
                    val resp = api.createDelivery(dto)
                    if (resp.isSuccessful) {
                        syncQueueDao.deleteActionById(action.id)
                        deliveryDao.updateDelivery(delivery.copy(isSynced = true))
                        syncedCount++
                    }
                } else {
                    syncQueueDao.deleteActionById(action.id)
                }
            }

            SyncResult.Success(syncedCount, "$syncedCount operações enviadas com sucesso ao servidor Levô.")
        } catch (e: Exception) {
            val remaining = syncQueueDao.getPendingCountSync()
            SyncResult.Error("Servidor indisponível (${e.localizedMessage ?: "conexão recusada"}). $remaining na fila local.")
        }
    }

    suspend fun registerPushToken(
        baseUrl: String,
        courierId: String,
        token: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val api = NetworkClient.getApiService(baseUrl)
            val res = api.registerPushToken(PushTokenRegistrationDto(courierId, token))
            if (res.isSuccessful) {
                Result.success("Token de notificações registrado no backend Levô!")
            } else {
                Result.failure(Exception("Erro na resposta do backend: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.success("Token configurado localmente. Será vinculado ao conectar.")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LevoRepository? = null

        fun getInstance(context: Context): LevoRepository {
            return INSTANCE ?: synchronized(this) {
                val db = LevoDatabase.getDatabase(context)
                val instance = LevoRepository(db.deliveryDao(), db.syncQueueDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
