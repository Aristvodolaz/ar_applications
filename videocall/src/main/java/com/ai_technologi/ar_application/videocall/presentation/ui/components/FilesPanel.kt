package com.ai_technologi.ar_application.videocall.presentation.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai_technologi.ar_application.videocall.domain.model.SharedFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Панель файлов.
 *
 * @param files список файлов
 * @param onSendFile колбэк, вызываемый при отправке файла
 * @param modifier модификатор
 */
@Composable
fun FilesPanel(
    files: List<SharedFile>,
    onSendFile: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Запуск выбора файла
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, uri)
            val fileType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val filePath = copyUriToFile(context, uri)?.absolutePath ?: return@let
            
            onSendFile(filePath, fileName, fileType)
        }
    }
    
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
                text = "Файлы",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Кнопка добавления файла
            Button(
                onClick = {
                    filePickerLauncher.launch("*/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Прикрепить файл"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Прикрепить файл")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Список файлов
            if (files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет файлов",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(files) { file ->
                        FileItem(file = file)
                    }
                }
            }
        }
    }
}

/**
 * Элемент списка файлов.
 *
 * @param file файл
 */
@Composable
private fun FileItem(
    file: SharedFile
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { /* Открыть файл */ }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка файла
        Icon(
            imageVector = getFileIcon(file.type),
            contentDescription = "Тип файла",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Информация о файле
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "${formatFileSize(file.size)} • ${formatTimestamp(file.timestamp)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Получение иконки для типа файла.
 *
 * @param fileType тип файла
 * @return иконка
 */
private fun getFileIcon(fileType: String): ImageVector {
    return when {
        fileType.startsWith("image/") -> Icons.Default.Image
        fileType.startsWith("video/") -> Icons.Default.Videocam
        fileType == "application/pdf" -> Icons.Default.PictureAsPdf
        else -> Icons.Default.Description
    }
}

/**
 * Форматирование размера файла.
 *
 * @param size размер файла в байтах
 * @return отформатированная строка
 */
private fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.2f MB", mb)
        kb >= 1.0 -> String.format("%.2f KB", kb)
        else -> "$size B"
    }
}

/**
 * Форматирование временной метки.
 *
 * @param timestamp временная метка в миллисекундах
 * @return отформатированная строка
 */
private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Получение имени файла из URI.
 *
 * @param context контекст
 * @param uri URI файла
 * @return имя файла
 */
private fun getFileName(context: android.content.Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    result = it.getString(nameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1) {
            result = result?.substring(cut!! + 1)
        }
    }
    return result ?: "file"
}

/**
 * Копирование файла из URI во временный файл.
 *
 * @param context контекст
 * @param uri URI файла
 * @return временный файл
 */
private fun copyUriToFile(context: android.content.Context, uri: Uri): File? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val fileName = getFileName(context, uri)
    val tempFile = File(context.cacheDir, fileName)
    
    inputStream.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    
    return tempFile
} 