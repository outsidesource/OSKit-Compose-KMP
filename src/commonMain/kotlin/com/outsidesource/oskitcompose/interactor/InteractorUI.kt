package com.outsidesource.oskitcompose.interactor

import androidx.compose.runtime.*
import com.outsidesource.oskitkmp.interactor.IInteractor
import com.outsidesource.oskitkmp.interactor.Interactor
import kotlinx.coroutines.flow.Flow

@Composable
fun <S : Any> IInteractor<S>.collectAsState(): S = remember(this) { flow() }.collectAsState(state).value