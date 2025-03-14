package com.origamilabs.orii.ui

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.adapters.CallHistoryAdapter
import java.util.*

data class CallRecord(val number: String, val type: Int, val date: Long)

class CallHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val callHistoryList = mutableListOf<CallRecord>()

    companion object {
        private const val REQUEST_READ_CALL_LOG = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_history)
        recyclerView = findViewById(R.id.callHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALL_LOG),
                REQUEST_READ_CALL_LOG
            )
        } else {
            loadCallHistory()
        }
    }

    private fun loadCallHistory() {
        val cursor: Cursor? = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null,
            CallLog.Calls.DATE + " DESC"
        )
        cursor?.use {
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            while (it.moveToNext()) {
                val number = it.getString(numberIndex)
                val type = it.getInt(typeIndex)
                val date = it.getLong(dateIndex)
                callHistoryList.add(CallRecord(number, type, date))
            }
        }
        recyclerView.adapter = CallHistoryAdapter(callHistoryList)
    }
}
