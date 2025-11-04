package com.izhenius.aigent.presentation.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.izhenius.aigent.presentation.viewmodel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    Text(text = viewModel.title)
}