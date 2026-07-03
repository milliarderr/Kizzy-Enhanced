/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UserViewModel.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_profile.ui.user

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.kizzy.domain.model.Resource
import com.my.kizzy.domain.model.rpc.RpcConfig
import com.my.kizzy.domain.model.user.User
import com.my.kizzy.domain.use_case.get_user.GetUserUseCase
import com.my.kizzy.feature_rpc_base.AppUtils
import com.my.kizzy.preference.Prefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase
): ViewModel() {

    private val _state: MutableState<UserState> = mutableStateOf(UserState.Loading)
    val state: State<UserState> = _state

    init {
        getUser()
    }
     private fun getUser(){
        getUserUseCase(Prefs[Prefs.USER_ID,""]).onEach { result ->
            when(result){
                is Resource.Success -> {
                    _state.value = UserState.LoadingCompleted(
                        user = result.data?.copy(
                            bio = Prefs[Prefs.USER_BIO],
                            nitro = Prefs[Prefs.USER_NITRO]
                        ),
                        rpcConfig = currentRpcConfig()
                    )
                    Prefs[Prefs.USER_DATA] = Json.encodeToString(result.data)
                }
                is Resource.Error -> {
                    val user = Json.decodeFromString<User>(Prefs[Prefs.USER_DATA,"{}"])
                    _state.value = UserState.Error(
                        error = result.message ?: "An unexpected error occurred",
                        user = user,
                        rpcConfig = currentRpcConfig()
                    )
                }
                is Resource.Loading -> {
                    _state.value = UserState.Loading
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun currentRpcConfig(): RpcConfig = try {
        when {
            AppUtils.customRpcRunning() -> {
                val json = Prefs[Prefs.LAST_RUN_CUSTOM_RPC, ""]
                if (json.isNotBlank()) runCatching { Json.decodeFromString<RpcConfig>(json) }
                    .getOrDefault(kizzyDefault())
                else kizzyDefault()
            }
            AppUtils.mediaRpcRunning() -> RpcConfig(name = "Media RPC", details = "Playing media", type = "2")
            AppUtils.experimentalRpcRunning() -> RpcConfig(name = "Experimental RPC", details = "Active")
            AppUtils.appDetectionRunning() -> RpcConfig(name = "App Detection", details = "Detecting apps")
            else -> kizzyDefault()
        }
    } catch (e: Exception) { kizzyDefault() }

    private fun kizzyDefault() = RpcConfig(name = "Kizzy", details = "Using Kizzy")
}