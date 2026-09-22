package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderSourceKind(val label: String) {
    IFOOD("iFood"),
    AIQFOME("aiqfome"),
    FOOD99("99Food"),
    SITE("Cardápio"),
    MANUAL("Balcão");

    companion object {
        fun fromString(value: String): OrderSourceKind {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MANUAL
        }
    }
}

enum class OrderFulfillment(val label: String) {
    DELIVERY("Entrega Própria"),
    PICKUP("Retirada no Balcão"),
    PLATFORM("Entrega da Plataforma")
}

enum class OrderStage(val label: String) {
    NEW("Novos"),              // Chegou do iFood/Cardápio/Balcão - aguardando aceitar
    PREPARING("Montando"),     // Na chapa / forno / cozinha
    READY("Prontos"),          // Embalado na expedição esperando saída
    IN_ROUTE("Em Rota"),       // Na rua com o motoboy
    DELIVERED("Entregue"),     // Finalizado com sucesso
    CANCELLED("Cancelado");

    companion object {
        fun fromString(value: String): OrderStage {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NEW
        }
    }
}

enum class PaymentMethodKind(val label: String) {
    ONLINE("Pago no App (Não cobrar)"),
    PIX("Pix na Entrega"),
    CARD_DEBIT("Cartão Débito"),
    CARD_CREDIT("Cartão Crédito"),
    CASH("Dinheiro");

    companion object {
        fun fromString(value: String): PaymentMethodKind {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ONLINE
        }
    }
}

data class FoodItem(
    val name: String,
    val quantity: Int,
    val unitPriceCents: Int,
    val options: List<String> = emptyList()
)

@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey
    val id: String,                         // Ex: "ord_d819a"
    val displayId: String,                  // Ex: "1366" (iFood) ou "104"
    val source: OrderSourceKind,            // IFOOD, AIQFOME, FOOD99, SITE, MANUAL
    val fulfillment: OrderFulfillment = OrderFulfillment.DELIVERY,
    val customerName: String,
    val customerPhone: String? = null,
    val address: String,                    // Endereço completo
    val reference: String? = null,          // Ponto de referência (Apto 42, Bloco B)
    val establishmentName: String = "Hamburgueria & Pizzaria Levô",
    val establishmentAddress: String = "Av. São João, 450 - Centro",
    val itemsSummary: String,               // Ex: "2× Smash Burger Duplo · 1× Fritas · 1× Coca Zero"
    val itemsDetailed: String? = null,      // Complementos detalhados
    val amountCents: Int,                   // Total do pedido em centavos
    val deliveryFeeCents: Int = 0,          // Taxa de entrega em centavos
    val paymentMethod: PaymentMethodKind = PaymentMethodKind.ONLINE,
    val changeForCents: Int? = null,        // Troco para quanto em dinheiro
    val stage: OrderStage = OrderStage.NEW,
    val deliveryCode: String? = null,       // Código de 4 dígitos informado na entrega
    val courierName: String? = "Gustavo Santana",
    val urgent: Boolean = false,            // Marcação de pedido urgente
    val notes: String? = null,              // Ex: "Sem cebola, maionese à parte"
    val confirmationProof: String? = null,  // Comprovante (recebedor/código)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
) {
    val formattedTotal: String
        get() = String.format("R$ %.2f", amountCents / 100.0)

    val formattedFee: String
        get() = String.format("R$ %.2f", deliveryFeeCents / 100.0)

    val isPaidOnline: Boolean
        get() = paymentMethod == PaymentMethodKind.ONLINE
}

@Entity(tableName = "sync_queue")
data class SyncAction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deliveryId: String,
    val actionType: String, // "CREATE_ORDER", "ADVANCE_STAGE", "CONFIRM_DELIVERY"
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val statusText: String = "Pendente"
)
