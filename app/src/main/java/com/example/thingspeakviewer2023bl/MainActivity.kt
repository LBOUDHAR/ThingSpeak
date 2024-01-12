/****************************************************************************
    COPYRIGHT (c) Lies BOUDHAR 1982-2024

    Project name: IoT in Agriculture
    App name : send to ThingSpeak for Android 10 and up
    Date: 30/12/2023
    Version: 1.0
    Circuit: No
    CODE NAME: LiesB_IoT30122023_00A
    Coder: Lies BOUDHAR
    programmer: Lies BOUDHAR
    maintainer: Lies BOUDHAR
    Revision: 03/01/2024 à 17:27
    Microcontroller: No
    instagram: soko.009
    youtube: lies boudhar

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
****************************************************************************/

package com.example.thingspeakviewer2023bl

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response

import java.io.IOException
import io.github.muddz.styleabletoast.StyleableToast


class MainActivity : AppCompatActivity() {
    private lateinit var dataInput: EditText
    private lateinit var writeKey: EditText
    private lateinit var fieldSelect: Spinner
    lateinit var field: String
    var fieldposition: Int = 0
    private val sharedPrefFile = "thingspeakviewer2023bl"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
        var toast: StyleableToast
        val connectButton = findViewById<Button>(R.id.button_Connect)
        val sendtoButton = findViewById<Button>(R.id.button_Send)
        dataInput = this.findViewById(R.id.data_input_01)
        writeKey = this.findViewById(R.id.writekey_Input)
        fieldSelect = findViewById(R.id.field_Select)

        val fields = resources.getStringArray(R.array.fields)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fields)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fieldSelect.adapter = adapter

        fieldSelect.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                field = fields[position]
                fieldposition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        loadPreferences()

        connectButton.setOnClickListener {
            if (checkForInternet(this)) {
                toast = StyleableToast.makeText(
                    this,
                    "Connected",
                    R.style.BLies_Toast_Style02)
                toast.show()
            } else {
               toast = StyleableToast.makeText(
                   this,
                   "Disconnected",
                   R.style.AllStyles)
               toast.show()
            }
        }

        sendtoButton.setOnClickListener {
            if (checkForInternet(this)) {
                when {
                    sendto() -> {
                        toast = StyleableToast.makeText(
                            this,
                            "data sent to ThingSpeak",
                            R.style.BLies_Toast_Style03
                        )
                        toast.show()
                    }
                    else -> {
                        toast = StyleableToast.makeText(
                            this,
                            "there is a problem",
                            R.style.BLies_Toast_Style02
                        )
                        toast.show()
                    }
                }
            }else{
                toast = StyleableToast.makeText(
                    this,
                    "No internet connection",
                    R.style.AllStyles)
                toast.show()
            }
        }
    }

    private fun loadPreferences() {
        dataInput.setText((sharedPreferences.getInt("datainput",0)).toString())
        writeKey.setText(sharedPreferences.getString("writeKey","0"))
        fieldSelect.setSelection((sharedPreferences.getInt("field",0)))
    }

    private fun savePreferences(datainput: Int, position: Int, writeKey: String) {
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        editor.putInt("datainput",datainput)
        editor.putInt("field",position)
        editor.putString("writeKey",writeKey)
        editor.apply()
        editor.commit()
    }

    private fun checkForInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager?
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun sendto(): Boolean {
        val data: String = dataInput.text.toString()
        val datainput: Int = data.toInt()
        val writekey: String = writeKey.text.toString()
        var state = true
        val url = "https://api.thingspeak.com/update?api_key=HD84DB1KX269FBVE&$field=$datainput"
        val httpclient = OkHttpClient()
        val builder = Request.Builder()
        val request = builder.url(url).build()

        httpclient.newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                e?.printStackTrace()
                state = false
            }
            override fun onResponse(response: Response?) {
                if (response != null) {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    } else {
                        savePreferences(datainput,fieldposition, writekey)
                        state = true
                    }
                }
            }
        })
        return state
    }
}