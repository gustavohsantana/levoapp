package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderFulfillment
import com.example.data.model.OrderSourceKind
import com.example.data.model.OrderStage
import com.example.ui.theme.LevoAmberSecondary
import com.example.ui.theme.LevoBluePrimary
import com.example.ui.theme.LevoEmeraldTertiary
import com.example.ui.theme.SourceAiqfome
import com.example.ui.theme.SourceAiqfomeText
import com.example.ui.theme.SourceFood99
import com.example.ui.theme.SourceFood99Text
import com.example.ui.theme.SourceIfood
import com.example.ui.theme.SourceIfoodText
import com.example.ui.theme.SourceSite
import com.example.ui.theme.SourceSiteText
import com.example.ui.theme.StageEmRota
import com.example.ui.theme.StageMontando
import com.example.ui.theme.StageNovos
import com.example.ui.theme.StageProntos

@Composable
fun SyncStatusPill(
    isSimulatedOffline: Boolean,
    pendingCount: Int,
    isSyncing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val backgroundColor = when {
        isSyncing -> LevoBluePrimary.copy(alpha = 0.15f)
        isSimulatedOffline -> Color(0xFFEF4444).copy(alpha = 0.15f)
        pendingCount > 0 -> LevoAmberSecondary.copy(alpha = 0.15f)
        else -> LevoEmeraldTertiary.copy(alpha = 0.15f)
    }

    val contentColor = when {
        isSyncing -> LevoBluePrimary
        isSimulatedOffline -> Color(0xFFDC2626)
        pendingCount > 0 -> Color(0xFFD97706)
        else -> Color(0xFF059669)
    }

    Surface(
        modifier = modifier
            .testTag("sync_status_pill")
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSyncing) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sincronizando",
                    tint = contentColor,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(rotation)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sincronizando...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            } else if (isSimulatedOffline) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (pendingCount > 0) "Offline ($pendingCount pend.)" else "Modo Offline",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            } else if (pendingCount > 0) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(contentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$pendingCount pendências",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Sincronizado",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Online • Sincronizado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * Tag idêntica à do painel Web do Levô (source-tag.tsx).
 * Identifica imediatamente se o pedido veio do iFood, aiqfome, 99Food, Cardápio ou Balcão.
 */
@Composable
fun SourceTag(
    source: OrderSourceKind,
    modifier: Modifier = Modifier
) {
    val (bg, fg, label) = when (source) {
        OrderSourceKind.IFOOD -> Triple(SourceIfood.copy(alpha = 0.12f), SourceIfoodText, "iFood")
        OrderSourceKind.AIQFOME -> Triple(SourceAiqfome.copy(alpha = 0.12f), SourceAiqfomeText, "aiqfome")
        OrderSourceKind.FOOD99 -> Triple(SourceFood99.copy(alpha = 0.18f), SourceFood99Text, "99Food")
        OrderSourceKind.SITE -> Triple(SourceSite.copy(alpha = 0.12f), SourceSiteText, "Cardápio Web")
        OrderSourceKind.MANUAL -> Triple(Color(0xFF64748B).copy(alpha = 0.12f), Color(0xFF334155), "Balcão")
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.testTag("source_tag_${source.name.lowercase()}")
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
        )
    }
}

/**
 * Badge de estágio das colunas do Levô (Novos, Montando, Prontos, Em Rota, Entregue)
 */
@Composable
fun OrderStageBadge(
    stage: OrderStage,
    modifier: Modifier = Modifier
) {
    val (bg, fg, dotColor) = when (stage) {
        OrderStage.NEW -> Triple(StageNovos.copy(alpha = 0.12f), Color(0xFFB45309), StageNovos)
        OrderStage.PREPARING -> Triple(StageMontando.copy(alpha = 0.12f), Color(0xFF334155), StageMontando)
        OrderStage.READY -> Triple(StageProntos.copy(alpha = 0.12f), Color(0xFF0369A1), StageProntos)
        OrderStage.IN_ROUTE -> Triple(StageEmRota.copy(alpha = 0.12f), Color(0xFF047857), StageEmRota)
        OrderStage.DELIVERED -> Triple(Color(0xFF059669).copy(alpha = 0.12f), Color(0xFF065F46), Color(0xFF059669))
        OrderStage.CANCELLED -> Triple(Color(0xFFEF4444).copy(alpha = 0.12f), Color(0xFF991B1B), Color(0xFFEF4444))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.testTag("order_stage_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stage.label,
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.1.sp
            )
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
