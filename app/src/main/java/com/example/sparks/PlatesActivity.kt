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

class PlatesActivity : NavigationBarActivity(R.id.nav_plates) {

    private var enableEdit=true
    private var enableAdd=true
    private lateinit var swipeToDeleteCallback : SwipeToDeleteCallback

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
        swipeToDeleteCallback = object : SwipeToDeleteCallback(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(!enableSwipe)
                    return

                setClickable(false)
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
                    setClickable(true)
                }
                alertDialog.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        addButton.setOnClickListener {
            if (enableAdd) {
                setClickable(false)

                val dialogView = layoutInflater.inflate(R.layout.plate_add, null)
                val builder = AlertDialog.Builder(this).setView(dialogView)
                val pI = dialogView.findViewById<EditText>(R.id.plateInput)
                val dsc = dialogView.findViewById<EditText>(R.id.description)
                val pL = dialogView.findViewById<TextView>(R.id.plateLimit)
                val dscL = dialogView.findViewById<TextView>(R.id.descriptionLimit)

                pL.text = String.format(
                    resources.getString(R.string.plates_limit_text),
                    pI.text.length,
                    resources.getString(R.string.plates_limit)
                )
                dscL.text = String.format(
                    resources.getString(R.string.description_limit_text),
                    dsc.text.length,
                    resources.getString(R.string.description_limit)
                )

                specialButtonsInitialize(dialogView)

                val customDialog = builder.show()
                customDialog.setOnDismissListener {
                    setClickable(true)
                }

                pI.doOnTextChanged { _, _, _, _ ->
                    pL.text = String.format(
                        resources.getString(R.string.plates_limit_text),
                        pI.text.length,
                        resources.getString(R.string.plates_limit)
                    )
                }

                dsc.doOnTextChanged { _, _, _, _ ->
                    dscL.text = String.format(
                        resources.getString(R.string.description_limit_text),
                        dsc.text.length,
                        resources.getString(R.string.description_limit)
                    )
                }

                val btDismiss = dialogView.findViewById<Button>(R.id.cancelButton)
                btDismiss.setOnClickListener {
                    customDialog.dismiss()
                }

                val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
                confirmButton.setOnClickListener {
                    if (pI.text.toString().trim().isEmpty()) {
                        pI.hint = resources.getString(R.string.empty_plate_input)
                        pI.setHintTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.emptyPlatesWarningColor
                            )
                        )
                    } else {
                        saveData(pI.text.toString(), dsc.text.toString())
                        adapter.notifyItemInserted(adapter.itemCount)
                        customDialog.dismiss()
                    }
                }
            }
        }
    }

    private lateinit var plates : ArrayList<PlatesData>
    private lateinit var adapter : PlatesAdapter

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

        plates = gson.fromJson<ArrayList<PlatesData>>(json, itemType) ?: ArrayList()
    }

    private fun setClickable(clickable: Boolean)
    {
        enableAdd=clickable
        enableEdit=clickable
        swipeToDeleteCallback.enableSwipe=clickable
    }

    private fun itemClicked(item : PlatesData){
        if(!enableEdit) return

        setClickable(false)

        val dialogView = layoutInflater.inflate(R.layout.plate_add, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)

        builder.setOnDismissListener {
            setClickable(true)}

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
        btDismiss.setOnClickListener {
            customDialog.dismiss()
        }

        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        var k = 0

        confirmButton.setOnClickListener {
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