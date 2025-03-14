package com.origamilabs.orii.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.CallRecord
import java.text.SimpleDateFormat
import java.util.*

class CallHistoryAdapter(private val callRecords: List<CallRecord>) :
    RecyclerView.Adapter<CallHistoryAdapter.CallHistoryViewHolder>() {

    class CallHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val numberText: TextView = view.findViewById(R.id.callNumberTextView)
        val typeText: TextView = view.findViewById(R.id.callTypeTextView)
        val dateText: TextView = view.findViewById(R.id.callDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call_history, parent, false)
        return CallHistoryViewHolder(view)
    }

    override fun getItemCount() = callRecords.size

    override fun onBindViewHolder(holder: CallHistoryViewHolder, position: Int) {
        val record = callRecords[position]
        holder.numberText.text = record.number
        holder.typeText.text = when (record.type) {
            1 -> "Entrant"
            2 -> "Sortant"
            3 -> "Manqué"
            else -> "Inconnu"
        }
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.dateText.text = formatter.format(Date(record.date))
    }
}
