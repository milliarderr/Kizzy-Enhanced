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
import androidx.compose.material3.WavyLinearProgressIndicator
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.my.kizzy.resources.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            SelectionContainer {
                Text(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    text = newVersionLog,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (apkUrl != null) showDownloadDialog = true
            }) {
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
private fun DownloadProgressDialog(
    apkUrl: String,
    onDismiss: () -> Unit,
) {
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
                    val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (cursor.getInt(statusCol) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloadState = DownloadState.DONE
                        progress = 1f
                        val uri = dm.getUriForDownloadedFile(id)
                        if (uri != null) {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/vnd.android.package-archive")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            )
                        }
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
                    if (total > 0) progress = done.toFloat() / total.toFloat()
                }
                cursor.close()
                delay(300)
            }
        }

        onDispose { context.unregisterReceiver(receiver) }
    }

    Dialog(
        onDismissRequest = { if (downloadState != DownloadState.DOWNLOADING) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = downloadState != DownloadState.DOWNLOADING)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
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

                if (progress > 0f) {
                    WavyLinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    WavyLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

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
        modifier = Modifier
            .height(500.dp)
            .width(300.dp)
    )
}
