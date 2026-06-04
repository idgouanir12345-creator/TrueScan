package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.TrueScanApplication
import com.example.data.local.SettingsRepository
import com.example.data.model.Product
import com.example.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ProductUiState {
    object Idle : ProductUiState
    object Loading : ProductUiState
    data class Success(val product: Product) : ProductUiState
    data class Error(val message: String) : ProductUiState
}

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val results: List<Product>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class TrueScanViewModel(
    application: Application,
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    // Product detail State
    private val _productState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val productState: StateFlow<ProductUiState> = _productState.asStateFlow()

    // Search query result state
    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    // Query string holder for the search text field
    val searchQuery = MutableStateFlow("")

    // List of scanned products
    val historyList: StateFlow<List<Product>> = productRepository.scanHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // List of favorite products
    val favoritesList: StateFlow<List<Product>> = productRepository.favorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current theme Choice
    val themeMode: StateFlow<String> = settingsRepository.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    /**
     * Clear detail page state
     */
    fun clearProductState() {
        _productState.value = ProductUiState.Idle
    }

    /**
     * Grabs a single product detail either by scan or by search click.
     */
    fun fetchProductDetail(barcode: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _productState.value = ProductUiState.Loading
            try {
                val product = productRepository.getProduct(barcode, forceRefresh)
                _productState.value = ProductUiState.Success(product)
            } catch (e: Exception) {
                _productState.value = ProductUiState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    /**
     * Toggles bookmarking state.
     */
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            productRepository.setFavorite(product.barcode, !product.isFavorite)
            // If the current detail product is this product, update UI state
            val currentState = _productState.value
            if (currentState is ProductUiState.Success && currentState.product.barcode == product.barcode) {
                _productState.value = ProductUiState.Success(
                    currentState.product.copy(isFavorite = !product.isFavorite)
                )
            }
        }
    }

    /**
     * Trigger Product Search
     */
    fun searchProducts(query: String) {
        searchQuery.value = query
        if (query.trim().isEmpty()) {
            _searchState.value = SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            try {
                val results = productRepository.searchProductsByNameOrBarcode(query)
                _searchState.value = SearchUiState.Success(results)
            } catch (e: Exception) {
                _searchState.value = SearchUiState.Error(e.message ?: "Failed to perform search.")
            }
        }
    }

    /**
     * Settings theme update
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemePreference(mode)
        }
    }

    /**
     * Settings clear caches
     */
    fun clearHistory(keepFavorites: Boolean) {
        viewModelScope.launch {
            productRepository.clearHistory(keepFavorites)
        }
    }

    /**
     * Provision Factory
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrueScanViewModel::class.java)) {
                val app = application as TrueScanApplication
                @Suppress("UNCHECKED_CAST")
                return TrueScanViewModel(application, app.productRepository, app.settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
