package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.VoterDatabase
import com.example.data.model.ParsedFile
import com.example.data.model.Voter
import com.example.data.repository.VoterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainScreen {
    LOGIN, DASHBOARD, SEARCH, UPLOAD, ANALYTICS, SETTINGS
}

class VoterViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("voter_search_prefs", Context.MODE_PRIVATE)

    private val database = VoterDatabase.getDatabase(application)
    private val repository = VoterRepository(database.voterDao(), database.parsedFileDao())

    // App Navigation State
    private val _currentScreen = MutableStateFlow(MainScreen.DASHBOARD)
    val currentScreen: StateFlow<MainScreen> = _currentScreen.asStateFlow()

    // Admin Authentication State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Filters and Search State
    val searchQuery = MutableStateFlow("")
    val selectedDivision = MutableStateFlow<String?>(null)
    val selectedDistrict = MutableStateFlow<String?>(null)
    val selectedUpazila = MutableStateFlow<String?>(null)
    val selectedUnion = MutableStateFlow<String?>(null)
    val selectedVillage = MutableStateFlow<String?>(null)
    val selectedConstituency = MutableStateFlow<String?>(null)

    // Pagination State
    val currentPage = MutableStateFlow(1)
    val pageSize = 10
    private val _totalMatches = MutableStateFlow(0)
    val totalMatches: StateFlow<Int> = _totalMatches.asStateFlow()

    // Query Results
    private val _searchResults = MutableStateFlow<List<Voter>>(emptyList())
    val searchResults: StateFlow<List<Voter>> = _searchResults.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    // Unique Options List from Database
    val uniqueDivisions: StateFlow<List<String>> = repository.uniqueDivisions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uniqueDistricts: StateFlow<List<String>> = repository.uniqueDistricts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uniqueUpazilas: StateFlow<List<String>> = repository.uniqueUpazilas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uniqueUnions: StateFlow<List<String>> = repository.uniqueUnions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uniqueVillages: StateFlow<List<String>> = repository.uniqueVillages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uniqueConstituencies: StateFlow<List<String>> = repository.uniqueConstituencies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // File Parsing and Store States
    private val _fileUploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val fileUploadState: StateFlow<UploadState> = _fileUploadState.asStateFlow()

    val parsedFilesLog: StateFlow<List<ParsedFile>> = repository.parsedFilesLog
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Analytics Counter Streams
    val totalVoters: StateFlow<Int> = repository.totalVotersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val maleVoters: StateFlow<Int> = repository.maleVotersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val femaleVoters: StateFlow<Int> = repository.femaleVotersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Supabase Core Configuration Strings
    val supabaseUrl = MutableStateFlow(sharedPrefs.getString("supabase_url", "https://your-project.supabase.co/rest/v1") ?: "")
    val supabaseKey = MutableStateFlow(sharedPrefs.getString("supabase_key", "") ?: "")

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        viewModelScope.launch {
            // Guarantee that we populate dynamic Bangladesh values if the local DB is empty,
            // supplying a complete visual initial demo state.
            repository.prepopulateIfEmpty()
            
            // Check persistent Admin Login state
            _isAdminLoggedIn.value = sharedPrefs.getBoolean("is_admin_logged_in", false)
            if (!_isAdminLoggedIn.value) {
                _currentScreen.value = MainScreen.LOGIN
            }

            // Bind reactive voter queries
            combine(
                searchQuery,
                selectedDivision,
                selectedDistrict,
                selectedUpazila,
                selectedUnion,
                selectedVillage,
                selectedConstituency,
                currentPage
            ) { params ->
                executeVotersSearch()
            }.collect()
        }
    }

    fun navigateTo(screen: MainScreen) {
        if (!_isAdminLoggedIn.value && screen != MainScreen.LOGIN) {
            _currentScreen.value = MainScreen.LOGIN
        } else {
            _currentScreen.value = screen
        }
    }

    fun loginAdmin(password: String) {
        if (password == "admin") {
            _isAdminLoggedIn.value = true
            sharedPrefs.edit().putBoolean("is_admin_logged_in", true).apply()
            _authError.value = null
            _currentScreen.value = MainScreen.DASHBOARD
        } else {
            _authError.value = "Invalid administrator passkey! Try 'admin'"
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        sharedPrefs.edit().putBoolean("is_admin_logged_in", false).apply()
        _currentScreen.value = MainScreen.LOGIN
    }

    fun saveSupabaseConfig(url: String, key: String) {
        supabaseUrl.value = url
        supabaseKey.value = key
        sharedPrefs.edit()
            .putString("supabase_url", url)
            .putString("supabase_key", key)
            .apply()
    }

    fun executeVotersSearch() {
        viewModelScope.launch {
            _isSearchLoading.value = true
            val queryText = searchQuery.value
            val div = selectedDivision.value
            val dist = selectedDistrict.value
            val upazila = selectedUpazila.value
            val unionVal = selectedUnion.value
            val villageVal = selectedVillage.value
            val constituencyVal = selectedConstituency.value
            val page = currentPage.value

            val resultsVal = repository.searchVoters(
                query = queryText,
                division = div,
                district = dist,
                upazila = upazila,
                union = unionVal,
                village = villageVal,
                constituency = constituencyVal,
                page = page,
                pageSize = pageSize
            )

            val totalCount = repository.getVotersTotalCount(
                query = queryText,
                division = div,
                district = dist,
                upazila = upazila,
                union = unionVal,
                village = villageVal,
                constituency = constituencyVal
            )

            _searchResults.value = resultsVal
            _totalMatches.value = totalCount
            _isSearchLoading.value = false
        }
    }

    fun clearSearchFilters() {
        selectedDivision.value = null
        selectedDistrict.value = null
        selectedUpazila.value = null
        selectedUnion.value = null
        selectedVillage.value = null
        selectedConstituency.value = null
        searchQuery.value = ""
        currentPage.value = 1
        executeVotersSearch()
    }

    fun uploadAndParseFile(uri: Uri, fileName: String, fileType: String) {
        viewModelScope.launch {
            _fileUploadState.value = UploadState.Processing(0)
            val parsedCount = repository.parseAndStoreFile(
                context = getApplication(),
                uri = uri,
                fileName = fileName,
                fileType = fileType
            )

            if (parsedCount > 0) {
                _fileUploadState.value = UploadState.Success(parsedCount, fileName)
                // Refresh list on upload success
                executeVotersSearch()
            } else {
                _fileUploadState.value = UploadState.Error("Unsupported format or parsing issue.")
            }
        }
    }

    fun resetUploadState() {
        _fileUploadState.value = UploadState.Idle
    }

    fun syncDataWithSupabase() {
        viewModelScope.launch {
            val url = supabaseUrl.value
            val key = supabaseKey.value
            if (url.isEmpty() || key.isEmpty()) {
                _syncState.value = SyncState.Error("Please configure Supabase credentials in Settings first!")
                return@launch
            }

            _syncState.value = SyncState.Syncing
            val result = repository.syncWithSupabase(url, key)
            if (result.isSuccess) {
                _syncState.value = SyncState.Success(result.getOrDefault(0))
            } else {
                _syncState.value = SyncState.Error(result.exceptionOrNull()?.message ?: "Unknown API sync issue.")
            }
        }
    }

    fun setSyncIdle() {
        _syncState.value = SyncState.Idle
    }

    fun wipeAllVoters() {
        viewModelScope.launch {
            repository.clearAllData()
            clearSearchFilters()
        }
    }

    fun insertVoterDirectly(voter: Voter) {
        viewModelScope.launch {
            repository.addNewVoter(voter)
            executeVotersSearch()
        }
    }
}

sealed interface UploadState {
    object Idle : UploadState
    data class Processing(val progress: Int) : UploadState
    data class Success(val count: Int, val fileName: String) : UploadState
    data class Error(val message: String) : UploadState
}

sealed interface SyncState {
    object Idle : SyncState
    object Syncing : SyncState
    data class Success(val count: Int) : SyncState
    data class Error(val message: String) : SyncState
}
