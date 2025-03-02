package com.origamilabs.orii.data

import android.content.Context
import com.origamilabs.orii.model.Item
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DataRepository {
    override suspend fun getItems(): List<Item> {
        // Exemple : lire un fichier JSON dans les assets
        // val jsonString = context.assets.open("data.json").bufferedReader().use { it.readText() }
        // Ensuite, parse le JSON en liste d'Item avec Gson ou Moshi.
        // Pour cet exemple, nous retournons une liste codée en dur.
        delay(500) // Simule une opération asynchrone
        return listOf(
            Item("Élément 1", "Description de l'élément 1"),
            Item("Élément 2", "Description de l'élément 2")
        )
    }
}
