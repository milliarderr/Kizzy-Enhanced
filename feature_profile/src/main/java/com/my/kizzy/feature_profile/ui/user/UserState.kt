/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UserState.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_profile.ui.user

import androidx.compose.runtime.Stable
import com.my.kizzy.domain.model.rpc.RpcConfig
import com.my.kizzy.domain.model.user.User

@Stable
sealed interface UserState {
    object Loading: UserState
    class Error(val error: String, val user: User?, val rpcConfig: RpcConfig): UserState
    class LoadingCompleted(val user: User?, val rpcConfig: RpcConfig): UserState
}