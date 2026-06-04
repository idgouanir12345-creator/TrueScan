package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsRepository
import com.example.data.remote.OpenFoodFactsApi
import com.example.data.repository.ProductRepository

class TrueScanApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val productRepository: ProductRepository by lazy { 
        ProductRepository(database.productDao(), OpenFoodFactsApi.create()) 
    }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
