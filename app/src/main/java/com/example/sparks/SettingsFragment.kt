package com.example.sparks

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val uputstvoPreference:Preference?=findPreference("uputstvo")

            uputstvoPreference?.setOnPreferenceClickListener {
                showDialog(getString(R.string.how_to_use), getString(R.string.how_to_use_value))
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
                builder.setPositiveButton("OK"){ dialog, _ ->
                    val emailIntent = Intent(Intent.ACTION_SEND)
                    emailIntent.type = "text/plain"
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("spark-feedback@outlook.com"))
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
                    emailIntent.putExtra(Intent.EXTRA_TEXT, editText.text)
                    emailIntent.type = "message/rfc822"
                    startActivity(Intent.createChooser(emailIntent, "Send email using..."))
                    dialog.dismiss()
                }
                builder.show()

                true
            }

            val oAplikaciji:Preference?=findPreference("o aplikaciji")
            oAplikaciji?.setOnPreferenceClickListener {
                showDialog(getString(R.string.about_app), getString(R.string.about_app_value))
                true
            }

            val language:Preference?=findPreference("language")

            language?.setOnPreferenceChangeListener { _, newValue ->
                when(newValue.toString()) {
                    "English" -> changeLanguage("en", activity)
                    "Srpski" -> changeLanguage("sr", activity)
                }

                true
            }

            val color:Preference?=findPreference("color")

            color?.setOnPreferenceChangeListener { _, newValue ->
                ResourcesCompat.getColor(this.resources,R.color.colorPrimary,null)
                when(newValue.toString()){
                    getString(R.string.blue)-> changeColor(R.style.AppTheme, R.color.colorPrimary, activity)

                    getString(R.string.green)-> changeColor(R.style.GreenTheme, R.color.green, activity)

                    getString(R.string.red)-> changeColor(R.style.RedTheme, R.color.red, activity)

                    getString(R.string.yellow)-> changeColor(R.style.YellowTheme, R.color.yellow, activity)

                    getString(R.string.pink)-> changeColor(R.style.PinkTheme, R.color.pink, activity)

                }

                true
            }
        }

    private fun showDialog(title: String, message: String) {
        val alertBuilder=AlertDialog.Builder(requireContext())
        alertBuilder.setMessage(message)
        alertBuilder.setTitle(title)
        alertBuilder.setPositiveButton("OK"){ dialog, _ -> dialog.dismiss() }
        alertBuilder.show()
    }

    private fun changeColor(theme: Int, color: Int, activity: FragmentActivity?) {
        this.requireContext().setTheme(theme)

        requireActivity().recreate()
        activity?.findViewById<Toolbar>(R.id.drawer_toolbar)?.setBackgroundColor(resources.getColor(color))

        val sharedPref = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)
        with(sharedPref?.edit()){
            this?.putInt("THEME",theme)
            this?.putInt("BACKGROUND",resources.getColor(color))
            this?.commit()
        }
    }

    private fun changeLanguage(
        lang: String,
        activity: FragmentActivity?
    ) {
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration()
        config.locale = locale

        requireActivity().baseContext.resources.updateConfiguration(config,
            requireActivity().baseContext.resources.displayMetrics)
        requireActivity().recreate()

        val sharedPref = activity?.getSharedPreferences("preferences",Context.MODE_PRIVATE)
        with(sharedPref?.edit()){
            this?.putString("LANG", lang)
            this?.commit()
        }
    }
}