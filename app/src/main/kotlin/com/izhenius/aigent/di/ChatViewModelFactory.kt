package com.izhenius.aigent.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.izhenius.aigent.presentation.viewmodel.ChatViewModel

class ChatViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(ServiceLocator.openAiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}