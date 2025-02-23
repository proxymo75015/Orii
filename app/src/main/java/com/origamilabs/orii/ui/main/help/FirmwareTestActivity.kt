package com.origamilabs.orii.ui.main.help

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.main.help.ui.help.FirmwareTestFragment

class FirmwareTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.firmware_test_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, FirmwareTestFragment.newInstance())
                .commitNow()
        }
    }
}
