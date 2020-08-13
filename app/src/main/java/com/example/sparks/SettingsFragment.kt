package com.example.sparks

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.*
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val uputstvoPreference:Preference?=findPreference("uputstvo")

            uputstvoPreference?.setOnPreferenceClickListener {
                val alert=AlertDialog.Builder(this.requireContext())
                alert.setTitle(getString(R.string.how_to_use))
                alert.setMessage(getString(R.string.how_to_use_value))
                alert.setPositiveButton("OK"){dialog, which ->
                    dialog.dismiss()
                }
                alert.show()


                true
            }

            val report:Preference?=findPreference("signature")
            report?.setOnPreferenceClickListener {
                val builder = AlertDialog.Builder(this.requireContext())
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
                    dialog.dismiss()
                }
                builder.show()
                true
            }

            val oAplikaciji:Preference?=findPreference("o aplikaciji")
            oAplikaciji?.setOnPreferenceClickListener {
                var alert=AlertDialog.Builder(this.requireContext())
                alert.setTitle(getString(R.string.about_app))
                alert.setMessage(getString(R.string.about_app_value))
                alert.setPositiveButton("OK"){dialog,which->
                    dialog.dismiss()
                }
                alert.show()
                true
            }

            val language:Preference?=findPreference("language")

            language?.setOnPreferenceChangeListener { preference, newValue ->

                when(newValue.toString()) {
                    "English" -> {

                        val locale = Locale("en")
                        Locale.setDefault(locale)
                        val config = Configuration()
                        config.locale = locale
                        requireActivity().baseContext.resources.updateConfiguration(config,
                            requireActivity().baseContext.resources.displayMetrics)

                        requireActivity().recreate()

                        val sharedPref = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)
                        with(sharedPref?.edit()){

                            this?.putString("LANG","en")
                            this?.commit()
                        }

                    }
                    "Srpski" -> {

                        val locale = Locale("sr")
                        Locale.setDefault(locale)
                        val config = Configuration()
                        config.locale = locale
                        requireActivity().baseContext.resources.updateConfiguration(config,
                            requireActivity().baseContext.resources.displayMetrics)

                        requireActivity().recreate()

                        val sharedPref = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)
                        with(sharedPref?.edit()){

                            this?.putString("LANG","sr")
                            this?.commit()
                        }

                    }
                }
                true

            }

            val color:Preference?=findPreference("color")

            color?.setOnPreferenceChangeListener { preference, newValue ->
                ResourcesCompat.getColor(this.resources,R.color.colorPrimary,null)

                when(newValue.toString()){
                    getString(R.string.blue)->{

                       this.requireContext().setTheme(R.style.AppTheme)
                        requireActivity().recreate()
                       activity?.findViewById<Toolbar>(R.id.drawer_toolbar)?.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                        val sharedPref = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)
                        with(sharedPref?.edit()){

                            this?.putInt("THEME",R.style.AppTheme)
                            this?.putInt("BACKGROUND",resources.getColor(R.color.colorPrimary))
                            this?.commit()
                        }
                    }

                    getString(R.string.green)->{
                        //mijenjaju se dugmad i slova u iskacucim prozorima
                        this.requireContext().setTheme(R.style.GreenTheme)
                        //mijenja boju onog gore toolbara gdje pise spark
                        requireActivity().recreate()
                        activity?.findViewById<Toolbar>(R.id.drawer_toolbar)?.setBackgroundColor(resources.getColor(R.color.fabBackground))
                        val sharedPref = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)
                        with(sharedPref?.edit()){

                            this?.putInt("THEME",R.style.GreenTheme)
                            this?.putInt("BACKGROUND",resources.getColor(R.color.fabBackground))
                            this?.commit()
                        }
                    }

                    getString(R.string.red)->{
                        //mijenjaju se dugmad i slova u iskacucim prozorima
                        this.requireContext().setTheme(R.style.RedTheme)
                        //mijenja boju onog gore toolbara gdje pise spark
                        requireActivity().recreate()
                        activity?.findViewById<Toolbar>(R.id.drawer_toolbar)?.setBackgroundColor(resources.getColor(R.color.red))

                        val sharedPref = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)
                        with(sharedPref?.edit()){

                            this?.putInt("THEME",R.style.RedTheme)
                            this?.putInt("BACKGROUND",resources.getColor(R.color.red))
                            this?.commit()
                        }

                    }

                    getString(R.string.yellow)->{
                        //mijenjaju se dugmad i slova u iskacucim prozorima
                        this.requireContext().setTheme(R.style.YellowTheme)
                        //mijenja boju onog gore toolbara gdje pise spark
                        requireActivity().recreate()
                        activity?.findViewById<Toolbar>(R.id.drawer_toolbar)?.setBackgroundColor(resources.getColor(R.color.yellow))

                        val sharedPref = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)
                        with(sharedPref?.edit()){

                            this?.putInt("THEME",R.style.YellowTheme)
                            this?.putInt("BACKGROUND",resources.getColor(R.color.yellow))
                            this?.commit()
                        }

                    }

                    getString(R.string.pink)->{
                        //mijenjaju se dugmad i slova u iskacucim prozorima
                        this.requireContext().setTheme(R.style.PinkTheme)
                        //mijenja boju onog gore toolbara gdje pise spark
                        requireActivity().recreate()
                        activity?.findViewById<Toolbar>(R.id.drawer_toolbar)?.setBackgroundColor(resources.getColor(R.color.pink))

                        val sharedPref = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)
                        with(sharedPref?.edit()){

                            this?.putInt("THEME",R.style.PinkTheme)
                            this?.putInt("BACKGROUND",resources.getColor(R.color.pink))
                            this?.commit()
                        }

                    }

                }


                true
            }
        }
    }