package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderSourceKind
import com.example.data.model.PaymentMethodKind
import com.example.ui.theme.LevoAmberSecondary
import com.example.ui.theme.LevoBluePrimary
import com.example.ui.theme.LevoEmeraldTertiary
import com.example.ui.viewmodel.LevoViewModel

data class QuickDish(
    val name: String,
    val priceCents: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewDeliveryScreen(
    viewModel: LevoViewModel,
    onDeliveryCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSimulatedOffline by viewModel.isSimulatedOffline.collectAsState()

    var selectedSource by remember { mutableStateOf(OrderSourceKind.MANUAL) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var itemsSummary by remember { mutableStateOf("") }
    var totalAmountInput by remember { mutableStateOf("45.00") }
    var deliveryFeeInput by remember { mutableStateOf("7.00") }
    var selectedPayment by remember { mutableStateOf(PaymentMethodKind.PIX) }
    var changeForInput by remember { mutableStateOf("") }
    var deliveryCode by remember { mutableStateOf((1000..9999).random().toString()) }
    var notes by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf(false) }

    val quickDishes = listOf(
        QuickDish("2× Smash Bacon Burger", 3800),
        QuickDish("1× Pizza Gde Calabresa", 5600),
        QuickDish("1× Marmitex Contrafilé", 3200),
        QuickDish("1× Batata Frita Crocante", 1800),
        QuickDish("1× Açaí 500ml Especial", 2400),
        QuickDish("1× Coca-Cola Lata 350ml", 700),
        QuickDish("1× Guaraná Antarctica 2L", 1400)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 90.dp)
            .testTag("new_delivery_screen")
    ) {
        // Header
        Text(
            text = "Lançar Pedido",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Cadastre um pedido manual (balcão, WhatsApp ou telefone) para a cozinha e expedição.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Offline Banner info
        Surface(
            color = if (isSimulatedOffline) LevoAmberSecondary.copy(alpha = 0.12f) else LevoBluePrimary.copy(alpha = 0.08f),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, (if (isSimulatedOffline) LevoAmberSecondary else LevoBluePrimary).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = if (isSimulatedOffline) LevoAmberSecondary else LevoBluePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isSimulatedOffline)
                        "Modo Offline Ativo: O pedido será salvo localmente no Room e sincronizado automaticamente quando houver conexão."
                    else
                        "Sincronização Online Ativa: O pedido é enviado diretamente ao painel Levô e emitido via push.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card: Canal / Origem
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Canal de Entrada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedSource == OrderSourceKind.MANUAL,
                        onClick = { selectedSource = OrderSourceKind.MANUAL },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("Balcão / Loja", style = MaterialTheme.typography.labelMedium) }
                    )
                    FilterChip(
                        selected = selectedSource == OrderSourceKind.SITE,
                        onClick = { selectedSource = OrderSourceKind.SITE },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("Cardápio Web", style = MaterialTheme.typography.labelMedium) }
                    )
                    FilterChip(
                        selected = selectedSource == OrderSourceKind.IFOOD,
                        onClick = { selectedSource = OrderSourceKind.IFOOD },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("iFood Manual", style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card: Dados do Cliente
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = LevoBluePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dados do Cliente & Entrega",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nome do Cliente *") },
                    placeholder = { Text("Ex: Mariana Souza") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Telefone / WhatsApp") },
                    placeholder = { Text("Ex: (11) 98765-4321") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Endereço Completo de Entrega *") },
                    placeholder = { Text("Ex: Rua das Rosas, 240, Apto 51 - Pinheiros") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_address")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Ponto de Referência") },
                    placeholder = { Text("Ex: Portaria social, ao lado da praça") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card: Comanda de Itens / Refeições
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Fastfood, contentDescription = null, tint = LevoAmberSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Itens da Comanda & Refeições",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Clique para adicionar itens rápidos do cardápio:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickDishes.forEach { dish ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                val current = itemsSummary.trim()
                                itemsSummary = if (current.isEmpty()) dish.name else "$current · ${dish.name}"
                                val currentTotal = totalAmountInput.toDoubleOrNull() ?: 0.0
                                totalAmountInput = String.format("%.2f", currentTotal + (dish.priceCents / 100.0)).replace(",", ".")
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(dish.name, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = itemsSummary,
                    onValueChange = { itemsSummary = it },
                    label = { Text("Resumo dos Itens *") },
                    placeholder = { Text("Ex: 2× Smash Bacon · 1× Fritas · 1× Coca-Cola 350ml") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_items_summary")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observação da Cozinha (opcional)") },
                    placeholder = { Text("Ex: Sem cebola, enviar sachês de maionese verde") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card: Valores e Pagamento
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = LevoEmeraldTertiary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Financeiro & Pagamento",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalAmountInput,
                        onValueChange = { totalAmountInput = it },
                        label = { Text("Total do Pedido (R$)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_total_amount")
                    )

                    OutlinedTextField(
                        value = deliveryFeeInput,
                        onValueChange = { deliveryFeeInput = it },
                        label = { Text("Taxa Frete (R$)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Forma de Cobrança:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PaymentMethodKind.entries.forEach { method ->
                        FilterChip(
                            selected = selectedPayment == method,
                            onClick = { selectedPayment = method },
                            label = { Text(method.label, fontSize = 11.sp) }
                        )
                    }
                }

                if (selectedPayment == PaymentMethodKind.CASH) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = changeForInput,
                        onValueChange = { changeForInput = it },
                        label = { Text("Troco para quanto em R$?") },
                        placeholder = { Text("Ex: 100.00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = deliveryCode,
                    onValueChange = { deliveryCode = it },
                    label = { Text("Código de 4 dígitos do cliente (conferência na porta)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFEF4444),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                if (customerName.isBlank()) {
                    errorMessage = "Por favor informe o nome do cliente."
                    return@Button
                }
                if (address.isBlank()) {
                    errorMessage = "Por favor informe o endereço de entrega."
                    return@Button
                }
                if (itemsSummary.isBlank()) {
                    errorMessage = "Por favor informe os itens do pedido."
                    return@Button
                }

                val totalDouble = totalAmountInput.toDoubleOrNull() ?: 45.0
                val feeDouble = deliveryFeeInput.toDoubleOrNull() ?: 7.0
                val changeDouble = changeForInput.toDoubleOrNull()

                val totalCents = (totalDouble * 100).toInt()
                val feeCents = (feeDouble * 100).toInt()
                val changeCents = if (changeDouble != null) (changeDouble * 100).toInt() else null

                viewModel.createManualFoodOrder(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    address = address,
                    reference = reference,
                    itemsSummary = itemsSummary,
                    amountCents = totalCents,
                    deliveryFeeCents = feeCents,
                    paymentMethod = selectedPayment,
                    changeForCents = changeCents,
                    deliveryCode = deliveryCode,
                    notes = notes,
                    source = selectedSource
                )

                successNotice = true
                onDeliveryCreated()
            },
            colors = ButtonDefaults.buttonColors(containerColor = LevoBluePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("submit_new_order_button")
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Emitir Pedido no Painel Levô", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
