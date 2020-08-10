package com.example.sparks

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.*

class SettingsFragment : PreferenceFragmentCompat() {


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val uputstvoPreference:Preference?=findPreference("uputstvo")

            uputstvoPreference?.setOnPreferenceClickListener {
                val alert=AlertDialog.Builder(this.requireContext())
                alert.setTitle("Uputstvo za upotrebu")
                alert.setMessage("1.Kao korisnik ove aplikacije potrebno je prvo da odaberete destinaciju, kako bi vam aplikacija mogla prikazati informacije o parking mjestima u krugu od 500 " +
                        "2.odaberite parking, potom je potebno da unesete registarske tablice, vrijeme na koje se parking placa \n" +
                        "3.nakon toga pritisnite posalji")
                alert.setPositiveButton("OK"){dialog, which ->
                    dialog.dismiss()
                }
                alert.show()


                true
            }

            val oAplikaciji:Preference?=findPreference("o aplikaciji")
            oAplikaciji?.setOnPreferenceClickListener {
                var alert=AlertDialog.Builder(this.requireContext())
                alert.setTitle("O aplikaciji")
                alert.setMessage("Aplikacija SPARK je rezultat urađenog projekta iz predmeta Projektovanje Softvera, omogućava korisniku da lakše pronađe slobodno parking mjesto kao i da mu prikaže mnoštvo korisnih informacija o parkinzima u Banja Luci \n \nVerzija:1.0")
                alert.setPositiveButton("OK"){dialog,which->
                    dialog.dismiss()
                }
                alert.show()
                true
            }

            val color:Preference?=findPreference("color")



            color?.setOnPreferenceChangeListener { preference, newValue ->
                ResourcesCompat.getColor(this.resources,R.color.colorPrimary,null);

                when(newValue.toString()){
                    "Plava"->{

                        var alert=AlertDialog.Builder(this.requireContext())
                        alert.setTitle("O aplikaciji")
                        alert.setMessage("Plava")
                        alert.setPositiveButton("OK"){dialog,which->
                            dialog.dismiss()
                        }
                        alert.show()
                       activity?.findViewById<Toolbar>(R.id.drawer_toolbar)?.setBackgroundColor(resources.getColor(R.color.colorPrimary));

                    }

                    "Zelena"->{
                        //mijenjaju se dugmad i slova u iskacucim prozorima
                        this.requireContext().setTheme(R.style.BlueTheme)
                        //mijenja boju onog gore toolbara gdje pise spark
                        activity?.findViewById<Toolbar>(R.id.drawer_toolbar)?.setBackgroundColor(resources.getColor(R.color.fabBackground));
                    }

                }


                true
            }
        }
    }