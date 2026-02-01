package com.allubie.nana.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.BackupManager
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.dao.NoteDao
import com.allubie.nana.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BackupState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isSuccess: Boolean = false
)

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val noteDao: NoteDao,
    private val backupManager: BackupManager
) : ViewModel() {
    
    val themeMode = preferencesManager.themeMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ThemeMode.SYSTEM
    )
    
    val currencyCode = preferencesManager.currencyCode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "USD"
    )
    
    val currencySymbol = preferencesManager.currencySymbol.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "$"
    )
    
    val timezone = preferencesManager.timezone.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        java.util.TimeZone.getDefault().id
    )
    
    val use24HourFormat = preferencesManager.use24HourFormat.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )
    
    private val _backupState = MutableStateFlow(BackupState())
    val backupState = _backupState.asStateFlow()
    
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }
    
    fun setCurrency(code: String, symbol: String) {
        viewModelScope.launch {
            preferencesManager.setCurrency(code, symbol)
        }
    }
    
    fun setTimezone(timezoneId: String) {
        viewModelScope.launch {
            preferencesManager.setTimezone(timezoneId)
            // Also update the JVM default for immediate effect
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(timezoneId))
        }
    }
    
    fun setUse24HourFormat(use24Hour: Boolean) {
        viewModelScope.launch {
            preferencesManager.setUse24HourFormat(use24Hour)
        }
    }
    
    fun backupData() {
        viewModelScope.launch {
            _backupState.value = BackupState(isLoading = true)
            backupManager.exportBackup().fold(
                onSuccess = { file ->
                    _backupState.value = BackupState(
                        message = "Backup saved to Downloads/${file.name}",
                        isSuccess = true
                    )
                },
                onFailure = { e ->
                    _backupState.value = BackupState(
                        message = "Backup failed: ${e.message}",
                        isSuccess = false
                    )
                }
            )
        }
    }
    
    fun restoreData(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState(isLoading = true)
            backupManager.importBackup(uri).fold(
                onSuccess = {
                    _backupState.value = BackupState(
                        message = "Data restored successfully",
                        isSuccess = true
                    )
                },
                onFailure = { e ->
                    _backupState.value = BackupState(
                        message = "Restore failed: ${e.message}",
                        isSuccess = false
                    )
                }
            )
        }
    }
    
    fun clearBackupMessage() {
        _backupState.value = BackupState()
    }
    
    fun emptyTrash() {
        viewModelScope.launch {
            noteDao.emptyTrash()
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                SettingsViewModel(
                    application.preferencesManager,
                    application.database.noteDao(),
                    application.backupManager
                )
            }
        }
    }
}
