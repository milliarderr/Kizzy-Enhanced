/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UpdateDialog.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.ui.components

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.my.kizzy.resources.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

fun Int.formatSize(): String =
    (this / 1024f / 1024f)
        .takeIf { it > 0f }
        ?.run { " ${String.format("%.2f", this)} MB" } ?: ""

private enum class DownloadState { IDLE, DOWNLOADING, DONE, ERROR }

/**
 * MD3-style wavy progress indicator drawn with Canvas.
 * - determinate: flat bar up to [progress], wavy tail beyond
 * - indeterminate: full wavy animation
 */
@Composable
fun WavyProgressIndicator(
    progress: Float = -1f,          // -1 = indeterminate
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 4.dp,
    waveAmplitude: Float = 4f,
    waveLength: Float = 24f,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.height(strokeWidth * 3)) {
        val sw = strokeWidth.toPx()
        val w = size.width
        val h = size.height
        val cy = h / 2f
        val isIndeterminate = progress < 0f
        val fillX = if (isIndeterminate) w else (progress * w).coerceIn(0f, w)

        // Track (background)
        drawLine(
            color = trackColor,
            start = Offset(0f, cy),
            end = Offset(w, cy),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )

        // Wavy filled part
        val path = Path()
        var started = false
        var x = 0f
        val step = 2f
        while (x <= fillX) {
            val angle = (x / waveLength) * 2f * PI.toFloat() - phaseShift
            val y = cy + sin(angle) * waveAmplitude
            if (!started) { path.moveTo(x, y); started = true }
            else path.lineTo(x, y)
            x += step
        }
        if (started) {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = sw, cap = StrokeCap.Round)
            )
        }

        // Flat filled part (deterministic) - when more than 5% done, flatten the tail
        if (!isIndeterminate && fillX > w * 0.05f) {
            val flatEnd = (fillX - waveLength / 2).coerceAtLeast(0f)
            if (flatEnd > 0f) {
                drawLine(
                    color = color,
                    start = Offset(0f, cy),
                    end = Offset(flatEnd, cy),
                    strokeWidth = sw,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun UpdateDialog(
    modifier: Modifier = Modifier,
    newVersionPublishDate: String,
    newVersionSize: Int,
    newVersionLog: String,
    apkUrl: String? = null,
    onDismissRequest: () -> Unit = {}
) {
    var showDownloadDialog by remember { mutableStateOf(false) }

    if (showDownloadDialog && apkUrl != null) {
        DownloadProgressDialog(
            apkUrl = apkUrl,
            onDismiss = {
                showDownloadDialog = false
                onDismissRequest()
            }
        )
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = { Icon(imageVector = Icons.Outlined.Update, contentDescription = "Update") },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.change_log))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$newVersionPublishDate ${newVersionSize.formatSize()}",
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        text = {
            SelectionContainer {
                Text(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    text = newVersionLog,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (apkUrl != null) showDownloadDialog = true }) {
                Text(text = stringResource(R.string.update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun DownloadProgressDialog(apkUrl: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var downloadState by remember { mutableStateOf(DownloadState.IDLE) }
    var progress by remember { mutableFloatStateOf(0f) }
    var downloadId by remember { mutableLongStateOf(-1L) }

    DisposableEffect(Unit) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id != downloadId) return
                val cursor = dm.query(DownloadManager.Query().setFilterById(id))
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloadState = DownloadState.DONE
                        progress = 1f
                        launchInstaller(ctx, id, dm)
                    } else {
                        downloadState = DownloadState.ERROR
                    }
                }
                cursor.close()
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Kizzy Enhanced")
            .setDescription(context.getString(R.string.update))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "kizzy-enhanced-update.apk")
            .setMimeType("application/vnd.android.package-archive")
        downloadId = dm.enqueue(request)
        downloadState = DownloadState.DOWNLOADING

        scope.launch {
            while (downloadState == DownloadState.DOWNLOADING) {
                val cursor = dm.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val done = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (total > 0) progress = done.toFloat() / total.toFloat()
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloadState = DownloadState.DONE
                            progress = 1f
                            launchInstaller(context, downloadId, dm)
                        }
                        DownloadManager.STATUS_FAILED -> {
                            downloadState = DownloadState.ERROR
                        }
                        else -> {}
                    }
                }
                cursor.close()
                delay(500)
            }
        }

        onDispose { context.unregisterReceiver(receiver) }
    }

    Dialog(
        onDismissRequest = { if (downloadState != DownloadState.DOWNLOADING) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = downloadState != DownloadState.DOWNLOADING)
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when (downloadState) {
                        DownloadState.DONE -> stringResource(R.string.update_installing)
                        DownloadState.ERROR -> stringResource(R.string.update_download_error)
                        else -> stringResource(R.string.update)
                    },
                    style = MaterialTheme.typography.titleLarge,
                )

                WavyProgressIndicator(
                    progress = if (progress > 0f) progress else -1f,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = when (downloadState) {
                        DownloadState.DOWNLOADING -> "${(progress * 100).toInt()}%"
                        DownloadState.DONE -> "100%"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )

                if (downloadState == DownloadState.ERROR || downloadState == DownloadState.DONE) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun UpdateDialogPreview() {
    UpdateDialog(
        newVersionLog = "1. Fix bugs\n2. Fix bugs\n3. Fix bugs",
        newVersionPublishDate = "2021-10-10",
        newVersionSize = 1000000,
        apkUrl = null,
        onDismissRequest = {},
        modifier = Modifier.height(500.dp).width(300.dp)
    )
}

private fun launchInstaller(context: Context, downloadId: Long, dm: DownloadManager) {
    // On API 26+, check if we can install unknown apps; if not, open settings first
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            context.startActivity(
                Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            return
        }
    }

    val file = java.io.File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "kizzy-enhanced-update.apk"
    )

    // Build URI — FileProvider for N+, raw file URI for older
    val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        try {
            androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.provider", file
            )
        } catch (_: Exception) {
            dm.getUriForDownloadedFile(downloadId) ?: return
        }
    } else {
        Uri.fromFile(file)
    }

    try {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
    } catch (_: Exception) {}
}
