package com.example.domain.wallet

import com.example.data.wallet.adapters.BCAAdapter
import com.example.data.wallet.adapters.DANAAdapter
import com.example.data.wallet.adapters.GenericWalletAdapter
import com.example.data.wallet.adapters.GoPayAdapter
import com.example.data.wallet.adapters.MandiriAdapter
import com.example.data.wallet.adapters.OVOAdapter

object WalletDetectionService {
    private val adapters: List<WalletAdapter> = listOf(
        GoPayAdapter(),
        DANAAdapter(),
        OVOAdapter(),
        BCAAdapter(),
        MandiriAdapter(),
        GenericWalletAdapter()
    )

    fun findAdapterForPackage(packageName: String): WalletAdapter? {
        return adapters.firstOrNull { it.supportedPackageNames.contains(packageName) }
            ?: adapters.lastOrNull() // Generic fallback
    }

    fun getAllAdapters(): List<WalletAdapter> = adapters
}
