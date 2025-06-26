package com.example.gravito_android_webview_sample

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject

class WebAppInterface(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MYPREF", Context.MODE_PRIVATE)

    /** Show a toast from the web page */
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun getValueFromWebView(value: String) {
        try {
            // Parse the JSON string into a JSONObject
            val jsonObject = JSONObject(value)
            val type = jsonObject.optString("type")

            when (type) {
                "save" -> {
                    // Extract values from the JSON object
                    val tcString = jsonObject.optString("tcstring", "")
                    val acString = jsonObject.optString("acstring", "")
                    val nonTcfData = jsonObject.optString("nontcfdata", "")

                    // Store values in SharedPreferences
                    val editor = sharedPreferences.edit()
                    editor.putString("TC_STRING", tcString)
                    editor.putString("AC_STRING", acString)
                    editor.putString("NON_TCF_DATA", nonTcfData)
                    editor.apply()
                }
                "close" -> {
                    // Placeholder for handling "close"
                    Log.d("WebAppInterface", "Handling event1")
                }
                "load" -> {
                    // Placeholder for handling "event2"
                    Log.d("WebAppInterface", "Handling load")
                }
                else -> {
                    // Placeholder for unknown types
                    Log.d("WebAppInterface", "Unknown type: $type")
                }
            }
        } catch (e: JSONException) {
            Log.e("WebAppInterface", "Failed to parse JSON: ${e.message}")
        }
    }

    /** Retrieve value from SharedPreferences */
    @JavascriptInterface
    fun getValueFromStorage(): String {
        try {
            val tcString = sharedPreferences.getString("TC_STRING", null)
            val acString = sharedPreferences.getString("AC_STRING", null)
            val nonTcfData = sharedPreferences.getString("NON_TCF_DATA", null)

            val jsonObject = JSONObject()
            jsonObject.put("tcstring", tcString) // Add `null` if not present
            jsonObject.put("acstring", acString) // Add `null` if not present
            jsonObject.put("nontcfdata", nonTcfData) // Add `null` if not present

            return jsonObject.toString() // Return the JSON object as a string
        } catch (e: JSONException) {
            Log.e("WebAppInterface", "Failed to create JSON: ${e.message}")
            return "{}" // Return an empty JSON object in case of error
        }
    }

    /** Handle button click from WebView */
    @JavascriptInterface
    fun onButtonClick() {
        // Add your button click logic here
    }
}