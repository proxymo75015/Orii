package com.origamilabs.orii

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import com.origamilabs.orii.viewmodel.ItemViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val itemViewModel: ItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textViewItems)

        // Observer le LiveData du ViewModel pour afficher les données
        itemViewModel.items.observe(this) { items ->
            textView.text = items.joinToString("\n") { "${it.name}: ${it.description}" }
        }

        // Lancer le chargement des données
        itemViewModel.loadItems()
    }
}
