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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.my.kizzy.resources.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

fun Int.formatSize(): String =
    (this / 1024f / 1024f)
        .takeIf { it > 0f }
        ?.run { " ${String.format("%.2f", this)} MB" } ?: ""

private enum class DownloadState { IDLE, DOWNLOADING, DONE, ERROR }

@Composable
fun UpdateDialog(
    modifier: Modifier = Modifier,
    newVersionPublishDate: String,
    newVersionSize: Int,
    newVersionLog: String,
    apkUrl: String? = null,
    onDismissRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var downloadState by remember { mutableStateOf(DownloadState.IDLE) }
    var progress by remember { mutableFloatStateOf(0f) }
    var downloadId by remember { mutableLongStateOf(-1L) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id != downloadId) return
                val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = dm.query(DownloadManager.Query().setFilterById(id))
                if (cursor.moveToFirst()) {
                    val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (cursor.getInt(statusCol) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloadState = DownloadState.DONE
                        progress = 1f
                        val apkFile = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "kizzy-enhanced-update.apk"
                        )
                        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", apkFile)
                        } else {
                            Uri.fromFile(apkFile)
                        }
                        ctx.startActivity(
                            Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/vnd.android.package-archive")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        )
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
        onDispose { context.unregisterReceiver(receiver) }
    }

    fun startDownload() {
        val url = apkUrl ?: return
        downloadState = DownloadState.DOWNLOADING
        progress = 0f
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Kizzy Enhanced")
            .setDescription(context.getString(R.string.update))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "kizzy-enhanced-update.apk")
            .setMimeType("application/vnd.android.package-archive")
        downloadId = dm.enqueue(request)
        scope.launch {
            while (downloadState == DownloadState.DOWNLOADING) {
                val cursor = dm.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val done = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    if (total > 0) progress = done.toFloat() / total.toFloat()
                }
                cursor.close()
                delay(300)
            }
        }
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = { if (downloadState != DownloadState.DOWNLOADING) onDismissRequest() },
        icon = {
            Icon(imageVector = Icons.Outlined.Update, contentDescription = "Update")
        },
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
            Column {
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f, fill = false),
                        text = newVersionLog,
                    )
                }
                AnimatedVisibility(visible = downloadState != DownloadState.IDLE) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        when {
                            downloadState == DownloadState.DOWNLOADING && progress <= 0f ->
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            downloadState == DownloadState.DOWNLOADING ->
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            else ->
                                LinearProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (downloadState) {
                                DownloadState.DOWNLOADING -> "${(progress * 100).toInt()}%"
                                DownloadState.DONE -> stringResource(R.string.update_installing)
                                DownloadState.ERROR -> stringResource(R.string.update_download_error)
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = downloadState == DownloadState.IDLE || downloadState == DownloadState.ERROR,
                onClick = { if (apkUrl != null) startDownload() }
            ) {
                Text(text = stringResource(R.string.update))
            }
        },
        dismissButton = {
            TextButton(
                enabled = downloadState != DownloadState.DOWNLOADING,
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
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
        modifier = Modifier
            .height(500.dp)
            .width(300.dp)
    )
}
