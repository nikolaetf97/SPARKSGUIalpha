package com.example.sparks

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList

class PlatesActivity : NavigationBarActivity(R.id.nav_plates) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup(R.layout.activity_plates, null)
        loadData()

        val addButton = findViewById<Button>(R.id.addButton)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        adapter = PlatesAdapter(plates) { pdata : PlatesData -> itemClicked(pdata)}

        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val context = this
        val swipeToDeleteCallback = object : SwipeToDeleteCallback(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(multiDeleteControl==1)
                {
                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                    return
                }

                multiDeleteControl=1
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setTitle(R.string.plate_delete)
                alertDialog.setMessage(R.string.plate_delete_msg)
                alertDialog.setNegativeButton(R.string.cancel)
                { _, _ -> }
                alertDialog.setPositiveButton(R.string.delete) { _, _ ->
                    deleteData(viewHolder.adapterPosition)
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                }
                alertDialog.setOnDismissListener {
                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                    multiDeleteControl=0
                }
                alertDialog.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        addButton.setOnClickListener{
            val dialogView = layoutInflater.inflate(R.layout.plate_add, null)

            val builder = AlertDialog.Builder(this).setView(dialogView)

            val pI = dialogView.findViewById<EditText>(R.id.plateInput)
            val dsc = dialogView.findViewById<EditText>(R.id.description)
            val pL = dialogView.findViewById<TextView>(R.id.plateLimit)
            val dscL = dialogView.findViewById<TextView>(R.id.descriptionLimit)

            pL.text = String.format(resources.getString(R.string.plates_limit_text), pI.text.length, resources.getString(R.string.plates_limit))
            dscL.text = String.format(resources.getString(R.string.description_limit_text), dsc.text.length, resources.getString(R.string.description_limit))

            specialButtonsInitialize(dialogView)

            val customDialog = builder.show()

            pI.doOnTextChanged { _, _, _, _ ->
                pL.text = String.format(resources.getString(R.string.plates_limit_text), pI.text.length, resources.getString(R.string.plates_limit))
            }

            dsc.doOnTextChanged { _, _, _, _ ->
                dscL.text = String.format(resources.getString(R.string.description_limit_text), dsc.text.length, resources.getString(R.string.description_limit))
            }

            val btDismiss = dialogView.findViewById<Button>(R.id.cancelButton)
            btDismiss.setOnClickListener(){
                customDialog.dismiss()
            }

            val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

            confirmButton.setOnClickListener {
                if(pI.text.toString().trim().isEmpty())
                {
                    pI.hint = resources.getString(R.string.empty_plate_input)
                    pI.setHintTextColor(ContextCompat.getColor(context, R.color.emptyPlatesWarningColor))
                }
                else
                {
                    saveData(pI.text.toString(), dsc.text.toString())
                    adapter.notifyItemInserted(adapter.itemCount)
                    customDialog.dismiss()
                }
            }
        }
    }

    private lateinit var plates : ArrayList<PlatesData>
    private lateinit var adapter : PlatesAdapter
    private var multiDeleteControl = 0

    private fun saveData(name : String, description : String)
    {
        val sharedPreferences = getSharedPreferences("sharedP", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        plates.add(PlatesData(name,description))
        val json = gson.toJson(plates)
        editor.putString("dataList", json)
        editor.apply()
    }

    private fun deleteData(pos : Int)
    {
        val sharedPreferences = getSharedPreferences("sharedP", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        plates.removeAt(pos)
        val json = gson.toJson(plates)
        editor.putString("dataList", json)
        editor.apply()
    }

    private fun loadData()
    {
        val sharedPreferences = getSharedPreferences("sharedP", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("dataList", null)

        val itemType = object : TypeToken<ArrayList<PlatesData>>() {}.type

        plates = gson.fromJson<ArrayList<PlatesData>>(json, itemType) ?: ArrayList<PlatesData>()
    }

    private fun itemClicked(item : PlatesData){
        if(multiDeleteControl==1) return
        multiDeleteControl=1
        val dialogView = layoutInflater.inflate(R.layout.plate_add, null)

        val builder = AlertDialog.Builder(this).setView(dialogView)

        builder.setOnDismissListener { multiDeleteControl=0 }

        val nameView = dialogView.findViewById<TextView>(R.id.plateInput)
        val descriptionView = dialogView.findViewById<TextView>(R.id.description)

        nameView.text = item.name
        descriptionView.text = item.description

        val pI = dialogView.findViewById<EditText>(R.id.plateInput)
        val dsc = dialogView.findViewById<EditText>(R.id.description)
        val pL = dialogView.findViewById<TextView>(R.id.plateLimit)
        val dscL = dialogView.findViewById<TextView>(R.id.descriptionLimit)

        pL.text = String.format(resources.getString(R.string.plates_limit_text), pI.text.length, resources.getString(R.string.plates_limit))
        dscL.text = String.format(resources.getString(R.string.description_limit_text), dsc.text.length, resources.getString(R.string.description_limit))

        specialButtonsInitialize(dialogView)

        val customDialog = builder.show()

        pI.doOnTextChanged { _, _, _, _ ->
            pL.text = String.format(resources.getString(R.string.plates_limit_text), pI.text.length, resources.getString(R.string.plates_limit))
        }

        dsc.doOnTextChanged { _, _, _, _ ->
            dscL.text = String.format(resources.getString(R.string.description_limit_text), dsc.text.length, resources.getString(R.string.description_limit))
        }

        val btDismiss = dialogView.findViewById<Button>(R.id.cancelButton)
        btDismiss.setOnClickListener(){
            customDialog.dismiss()
        }

        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

        //if another name

        var k = 0
        confirmButton.setOnClickListener(){
            if(nameView.text.trim().isEmpty())
            {
                pI.hint = resources.getString(R.string.empty_plate_input)
                pI.setHintTextColor(ContextCompat.getColor(this, R.color.emptyPlatesWarningColor))
            }
            else
            {
                if (nameView.text.toString() != item.name) {
                    plates[item.pos].name = nameView.text.toString()
                    k = 1
                }
                if (descriptionView.text.toString() != item.description) {
                    plates[item.pos].description = descriptionView.text.toString()
                    k = 1
                }

                if (k == 1) {
                    adapter.notifyItemChanged(item.pos)

                    val sharedPreferences = getSharedPreferences("sharedP", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    val gson = Gson()
                    val json = gson.toJson(plates)
                    editor.putString("dataList", json)
                    editor.apply()
                }
                customDialog.dismiss()
            }
        }
    }

    /*override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        if (p0.itemId != itemId) {
            when (p0.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))

                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))

                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))

                R.id.user_manual -> {
                    val alert= AlertDialog.Builder(this)
                    alert.setTitle(getString(R.string.how_to_use))
                    alert.setMessage(getString(R.string.how_to_use_value))
                    alert.setPositiveButton("OK"){dialog, which ->
                        dialog.dismiss()
                    }
                    alert.show()
                }
                R.id.report_error -> {
                    val builder = AlertDialog.Builder(this)
                    val inflater = layoutInflater
                    builder.setTitle(getString(R.string.error_info_value))
                    val dialogLayout = inflater.inflate(R.layout.error_dialog, null)
                    val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
                    builder.setView(dialogLayout)
                    builder.setPositiveButton("OK"){dialog, which ->
                        val emailIntent = Intent(Intent.ACTION_SEND)
                        emailIntent.setType("text/plain")
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("spark-feedback@outlook.com"))
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
                        emailIntent.putExtra(Intent.EXTRA_TEXT, editText.text)
                        emailIntent.setType("message/rfc822")
                        startActivity(Intent.createChooser(emailIntent, "Send email using..."))
                    }
                    builder.show()
                }

                R.id.about -> {
                    var alert= AlertDialog.Builder(this)
                    alert.setTitle(getString(R.string.about_app))
                    alert.setMessage(getString(R.string.about_app_value))
                    alert.setPositiveButton("OK"){dialog,which->
                        dialog.dismiss()
                    }
                    alert.show()
                }

                R.id.nav_logs -> {
                    val logsLayout = layoutInflater.inflate(R.layout.dialog_logs, null)
                    val logsDialog = AlertDialog.Builder(this)
                    logsDialog.setView(logsLayout)
                    logsDialog.setTitle(getString(R.string.logovi))
                    logsLayout.recycler_view.layoutManager = LinearLayoutManager(this)
                    logsLayout.recycler_view.adapter = LogDataAdapter(this, Supplier.logData)
                    logsDialog.setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    logsDialog.show()
                    return false
                }
            }
            return true
        } else
            return false
    }*/

    private fun specialButtonsInitialize(v : View)
    {
        val pI = v.findViewById<EditText>(R.id.plateInput)

        v.findViewById<Button>(R.id.special_button1).setOnClickListener { pI.text.append(resources.getString(R.string.special_letter1_plate))}
        v.findViewById<Button>(R.id.special_button2).setOnClickListener { pI.text.append(resources.getString(R.string.special_letter2_plate))}
        v.findViewById<Button>(R.id.special_button3).setOnClickListener { pI.text.append(resources.getString(R.string.special_letter3_plate))}
        v.findViewById<Button>(R.id.special_button4).setOnClickListener { pI.text.append(resources.getString(R.string.special_letter4_plate))}
        v.findViewById<Button>(R.id.special_button5).setOnClickListener { pI.text.append(resources.getString(R.string.special_letter5_plate))}
    }
}