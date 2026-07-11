/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * ExperimentalRpcScreen.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package xyz.dead8309.feature_experimental_rpc

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppsOutage
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AppSettingsAlt
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.my.kizzy.data.rpc.Constants
import com.my.kizzy.data.rpc.TemplateKeys
import com.my.kizzy.feature_rpc_base.AppUtils
import com.my.kizzy.feature_rpc_base.services.AppDetectionService
import com.my.kizzy.feature_rpc_base.services.CustomRpcService
import com.my.kizzy.feature_rpc_base.services.ExperimentalRpc
import com.my.kizzy.feature_rpc_base.services.MediaRpcService
import com.my.kizzy.resources.R
import com.my.kizzy.ui.components.BackButton
import com.my.kizzy.ui.components.RpcField
import com.my.kizzy.ui.components.RpcFieldWithCompletions
import com.my.kizzy.ui.components.SettingItem
import com.my.kizzy.ui.components.Subtitle
import com.my.kizzy.ui.components.SwitchBar
import com.my.kizzy.ui.components.preference.PreferenceSwitch
import com.my.kizzy.ui.components.preference.PreferencesHint

private val completions = listOf(
    TemplateKeys.MEDIA_TITLE to R.string.completion_media_title,
    TemplateKeys.MEDIA_ARTIST to R.string.completion_media_artist,
    TemplateKeys.MEDIA_AUTHOR to R.string.completion_media_author,
    TemplateKeys.MEDIA_ALBUM to R.string.completion_media_album,
    TemplateKeys.MEDIA_ALBUM_ARTIST to R.string.completion_media_album_artist,
    TemplateKeys.MEDIA_DURATION to R.string.completion_media_duration,
    TemplateKeys.MEDIA_WRITER to R.string.completion_media_writer,
    TemplateKeys.MEDIA_COMPOSER to R.string.completion_media_composer,
    TemplateKeys.BATTERY to R.string.completion_battery,
    TemplateKeys.PLAYBACK to R.string.completion_playback,
    TemplateKeys.APP_NAME to R.string.completion_app_name,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalRpcScreen(
    onBackPressed: () -> Unit,
    state: UiState,
    hasUsageAccess: Boolean,
    hasNotificationAccess: Boolean,
    navigateToAppSelection: () -> Unit,
    onEvent: (UiEvent) -> Unit,
) {
    val context = LocalContext.current
    var experimentalRpcRunning by remember { mutableStateOf(AppUtils.experimentalRpcRunning()) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.main_experimentalRpc),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                navigationIcon = { BackButton { onBackPressed() } },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {
            AnimatedVisibility(
                visible = !hasUsageAccess
            ) {
                PreferencesHint(
                    title = stringResource(id = R.string.usage_access),
                    description = stringResource(id = R.string.usage_access_desc),
                    icon = Icons.Default.AppsOutage,
                ) {
                    when (hasUsageAccess) {
                        false -> context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        else -> Unit
                    }
                }
            }
            AnimatedVisibility(
                visible = !hasNotificationAccess
            ) {
                PreferencesHint(
                    title = stringResource(id = R.string.permission_required),
                    description = stringResource(id = R.string.request_for_notification_access),
                    icon = Icons.Default.Warning,
                ) {
                    if (!hasNotificationAccess) {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                }
            }

            SwitchBar(
                title = stringResource(id = R.string.enable_experimental_rpc),
                isChecked = experimentalRpcRunning,
                enabled = hasUsageAccess && hasNotificationAccess,
            ) {
                experimentalRpcRunning = !experimentalRpcRunning
                when (experimentalRpcRunning) {
                    true -> {
                        context.stopService(Intent(context, AppDetectionService::class.java))
                        context.stopService(Intent(context, CustomRpcService::class.java))
                        context.stopService(Intent(context, MediaRpcService::class.java))
                        context.startService(Intent(context, ExperimentalRpc::class.java))
                    }

                    false -> context.stopService(Intent(context, ExperimentalRpc::class.java))
                }
            }
            LazyColumn {
                item {
                    Subtitle(text = stringResource(R.string.general_settings))
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.enable_appsRpc),
                        description = stringResource(R.string.experimental_rpc_detect_apps),
                        isChecked = state.isAppsRpcPartEnabled,
                        onClick = {
                            onEvent(UiEvent.ToggleAppsRpcPart(!state.isAppsRpcPartEnabled))
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_apps),
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 16.dp)
                                    .size(24.dp),
                                contentDescription = null
                            )
                        },
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.enable_mediaRpc),
                        description = stringResource(R.string.experimental_rpc_detect_media),
                        isChecked = state.isMediaRpcPartEnabled,
                        onClick = {
                            onEvent(UiEvent.ToggleMediaRpcPart(!state.isMediaRpcPartEnabled))
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_media_rpc),
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 16.dp)
                                    .size(24.dp),
                                contentDescription = null
                            )
                        },
                    )
                }
                item {
                    SettingItem(
                        title = stringResource(R.string.app_preferences),
                        description = stringResource(R.string.app_preferences_desc),
                        icon = Icons.Outlined.AppSettingsAlt
                    ) {
                        navigateToAppSelection()
                    }
                }
                item {
                    Subtitle(
                        text = stringResource(R.string.main_mediaRpc)
                    )
                }

                // Title source selector
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.experimental_rpc_title_toggle),
                        description = stringResource(R.string.experimental_rpc_title_toggle_desc),
                        isChecked = state.showSongAsTitle,
                        onClick = { onEvent(UiEvent.ToggleShowSongAsTitle(!state.showSongAsTitle)) },
                        icon = Icons.Outlined.Title,
                    )
                }
                if (state.showSongAsTitle) {
                    item {
                        val titleSources = linkedMapOf(
                            stringResource(R.string.title_source_song) to "title",
                            stringResource(R.string.title_source_artist) to "artist",
                        )
                        val titleIcon = if (state.titleSourceExpanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown
                        val titleLabel = titleSources.entries
                            .firstOrNull { it.value == state.titleSource }?.key ?: ""
                        RpcField(
                            value = titleLabel,
                            label = R.string.experimental_rpc_title_toggle,
                            trailingIcon = {
                                Icon(
                                    imageVector = titleIcon,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        onEvent(UiEvent.TriggerTitleSourceDropDown)
                                    }
                                )
                            },
                            content = {
                                DropdownMenu(
                                    expanded = state.titleSourceExpanded,
                                    onDismissRequest = { onEvent(UiEvent.TriggerTitleSourceDropDown) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    titleSources.forEach { (label, value) ->
                                        DropdownMenuItem(
                                            text = { Text(text = label) },
                                            onClick = { onEvent(UiEvent.SetTitleSource(value)) },
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // Media Display Options
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.show_cover_art),
                        description = stringResource(R.string.show_cover_art_desc),
                        isChecked = state.showCoverArt,
                        onClick = {
                            onEvent(UiEvent.ToggleShowCoverArt(!state.showCoverArt))
                        },
                        icon = Icons.Outlined.Image,
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.show_app_icon),
                        isChecked = state.showAppIcon,
                        onClick = {
                            onEvent(UiEvent.ToggleShowAppIcon(!state.showAppIcon))
                        },
                        icon = Icons.Outlined.Apps,
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.show_playback_state),
                        isChecked = state.showPlaybackState,
                        onClick = {
                            onEvent(UiEvent.ToggleShowPlaybackState(!state.showPlaybackState))
                        },
                        icon = Icons.Default.PlayCircle
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.enable_timestamps),
                        isChecked = state.enableTimestamps,
                        onClick = {
                            onEvent(UiEvent.ToggleEnableTimestamps(!state.enableTimestamps))
                        },
                        icon = Icons.Default.Timer
                    )
                }
                if (state.enableTimestamps) {
                    item {
                        val tsIcon = if (state.timestampModeExpanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown

                        val tsModes = linkedMapOf(
                            stringResource(R.string.timestamp_mode_default) to "default",
                            stringResource(R.string.timestamp_mode_current) to "current",
                            stringResource(R.string.timestamp_mode_custom) to "custom",
                        )
                        val tsLabel = tsModes.entries
                            .firstOrNull { it.value == state.timestampMode }?.key ?: state.timestampMode

                        RpcField(
                            value = tsLabel,
                            label = R.string.timestamp_mode,
                            trailingIcon = {
                                Icon(
                                    imageVector = tsIcon,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        onEvent(UiEvent.TriggerTimestampModeDropDownMenu)
                                    })
                            },
                            content = {
                                DropdownMenu(
                                    expanded = state.timestampModeExpanded,
                                    onDismissRequest = {
                                        onEvent(UiEvent.TriggerTimestampModeDropDownMenu)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    tsModes.forEach { (label, value) ->
                                        DropdownMenuItem(
                                            text = { Text(text = label) },
                                            onClick = { onEvent(UiEvent.SetTimestampMode(value)) },
                                        )
                                    }
                                }
                            }
                        )
                    }
                    if (state.timestampMode == "custom") {
                        item {
                            RpcField(
                                value = state.customTimestampStart,
                                label = R.string.start_timestamp_ms,
                                onValueChange = { onEvent(UiEvent.SetCustomTimestampStart(it)) }
                            )
                        }
                        item {
                            RpcField(
                                value = state.customTimestampEnd,
                                label = R.string.end_timestamp_ms,
                                onValueChange = { onEvent(UiEvent.SetCustomTimestampEnd(it)) }
                            )
                        }
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.hide_on_pause),
                        isChecked = state.hideOnPause,
                        onClick = {
                            onEvent(UiEvent.ToggleHideOnPause(!state.hideOnPause))
                        },
                        icon = Icons.Default.PauseCircle
                    )
                }
                item {
                    Subtitle(text = stringResource(R.string.advance_settings))
                }
                item {
                    RpcFieldWithCompletions(
                        value = state.templateName,
                        label = R.string.activity_name,
                        onValueChange = { onEvent(UiEvent.SetTemplateName(it)) },
                        completionList = completions
                    )
                }
                item {
                    RpcFieldWithCompletions(
                        value = state.templateDetails,
                        label = R.string.activity_details,
                        onValueChange = { onEvent(UiEvent.SetTemplateDetails(it)) },
                        completionList = completions
                    )
                }
                item {
                    RpcFieldWithCompletions(
                        value = state.templateState,
                        label = R.string.activity_state,
                        onValueChange = { onEvent(UiEvent.SetTemplateState(it)) },
                        completionList = completions
                    )
                }

                item {
                    val platformIcon = if (state.platformIsExpanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown

                    val platforms = linkedMapOf(
                        stringResource(R.string.platform_none_desktop) to "",
                        stringResource(R.string.platform_desktop) to "desktop",
                        stringResource(R.string.platform_web) to "web",
                        stringResource(R.string.platform_android) to "android",
                        stringResource(R.string.platform_ios) to "ios",
                        stringResource(R.string.platform_xbox) to "xbox",
                        stringResource(R.string.platform_playstation) to "playstation",
                        stringResource(R.string.platform_vr) to "vr",
                    )
                    val platformLabel = platforms.entries
                        .firstOrNull { it.value == state.platform }?.key ?: state.platform

                    RpcField(
                        value = platformLabel,
                        label = R.string.activity_platform,
                        trailingIcon = {
                            Icon(
                                imageVector = platformIcon,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    onEvent(UiEvent.TriggerPlatformDropDownMenu)
                                })
                        },
                        content = {
                            DropdownMenu(
                                expanded = state.platformIsExpanded,
                                onDismissRequest = {
                                    onEvent(UiEvent.TriggerPlatformDropDownMenu)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                platforms.forEach { (label, value) ->
                                    DropdownMenuItem(
                                        text = { Text(text = label) },
                                        onClick = { onEvent(UiEvent.SetPlatform(value)) },
                                    )
                                }
                            }
                        }
                    ) {
                        // Selection is driven by the dropdown; ignore free-text input.
                    }
                }

                item {
                    Subtitle(text = stringResource(R.string.rpc_buttons))
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.use_custom_buttons),
                        isChecked = state.buttonsEnabled,
                        onClick = {
                            onEvent(UiEvent.ToggleButtons(!state.buttonsEnabled))
                        },
                    )
                }
                if (state.buttonsEnabled) {
                    item {
                        RpcField(
                            value = state.button1Text,
                            label = R.string.activity_button1_text,
                            onValueChange = { onEvent(UiEvent.SetButton1Text(it)) }
                        )
                    }
                    item {
                        RpcField(
                            value = state.button1Url,
                            label = R.string.activity_button1_url,
                            onValueChange = { onEvent(UiEvent.SetButton1Url(it)) }
                        )
                    }
                    item {
                        RpcField(
                            value = state.button2Text,
                            label = R.string.activity_button2_text,
                            onValueChange = { onEvent(UiEvent.SetButton2Text(it)) }
                        )
                    }
                    item {
                        RpcField(
                            value = state.button2Url,
                            label = R.string.activity_button2_url,
                            onValueChange = { onEvent(UiEvent.SetButton2Url(it)) }
                        )
                    }
                }

                item {
                    Subtitle(text = stringResource(R.string.application_id))
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.custom_application_id),
                        description = stringResource(R.string.custom_application_id_desc),
                        isChecked = state.useCustomAppId,
                        onClick = {
                            onEvent(UiEvent.ToggleUseCustomAppId(!state.useCustomAppId))
                        },
                    )
                }
                if (state.useCustomAppId) {
                    item {
                        RpcField(
                            value = state.applicationId,
                            label = R.string.application_id,
                            onValueChange = { onEvent(UiEvent.SetApplicationId(it)) }
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(R.string.rpc_templates_note),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }
            }
        }
    }
}


