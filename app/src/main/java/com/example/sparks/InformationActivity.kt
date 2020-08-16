package com.example.sparks

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class InformationActivity : NavigationBarActivity(R.id.nav_info) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup(R.layout.activity_info, null)

        findViewById<TextView>(R.id.textView3).setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle(getString(R.string.reduction_time))
            val dialogLayout = inflater.inflate(R.layout.dialog_reserve, null)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Ok"){ dialog, _ ->
                    dialog.dismiss()
            }
            builder.show()
        }

    }
}