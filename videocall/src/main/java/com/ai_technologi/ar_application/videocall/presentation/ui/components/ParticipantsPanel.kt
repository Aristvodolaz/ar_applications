package com.ai_technologi.ar_application.videocall.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ai_technologi.ar_application.core.network.models.ParticipantInfo

/**
 * Панель участников видеозвонка.
 *
 * @param participants список участников
 * @param modifier модификатор
 */
@Composable
fun ParticipantsPanel(
    participants: List<ParticipantInfo>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "Участники (${participants.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            participants.forEach { participant ->
                ParticipantItem(participant = participant)
            }
        }
    }
}

/**
 * Элемент списка участников.
 *
 * @param participant информация об участнике
 */
@Composable
private fun ParticipantItem(
    participant: ParticipantInfo
) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар участника
        if (participant.avatarUrl != null) {
            AsyncImage(
                model = participant.avatarUrl,
                contentDescription = "Аватар ${participant.displayName}",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        } else {
            Surface(
                modifier = Modifier
                    .size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Аватар ${participant.displayName}",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Имя участника
        Text(
            text = participant.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Статус микрофона
        val isMicEnabled = participant.status == "speaking" || participant.status == "connected"
        Icon(
            imageVector = if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = "Статус микрофона",
            tint = if (isMicEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
    }
} 