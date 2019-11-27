package com.farm.farmestar

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.onesignal.OneSignal
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    //Shared Prefs info
//    var prefs: Prefs? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        prefs = Prefs(this)
//        val _version = prefs!!.version
        val _version = "1.0.1"

        //TODO: url is set to staging, before production this needs to be pushed to prod env
        var farmestarURL = "https://farmestar.herokuapp.com/"
//        var farmestarURL = "http://127.0.0.1:8080/"
//        var farmestarURL = "https//farmestar-prod.herokuapp.com"

        //Setup WebView for Igedla
        val farmestarWebView = WebView(this)
        farmestarWebView.loadUrl(farmestarURL)

        //WebView Client
        farmestarWebView.webChromeClient = WebChromeClient()

        //Web Settings
        farmestarWebView.settings.javaScriptEnabled = true
        farmestarWebView.settings.domStorageEnabled = true
        farmestarWebView.settings.javaScriptCanOpenWindowsAutomatically = true

        //Make Call to Check version
//        val needsUpdate = run("https://farmestar.herokuapp.com/api/app/version", _version)

//        if (needsUpdate){
//            //Update by clearing cache
//        }

//        run("https://farmestar.herokuapp.com/api/app/version", _version)



        // OneSignal Implementation
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        //Set the Content View After application Configuration
        setContentView(farmestarWebView)
    }

    //Version API call
    fun run(url: String, currentVersion: String?): Boolean {
        val request = Request.Builder()
            .url(url)
            .build()
        var result = false

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { result = false}
            override fun onResponse(call: Call, response: Response){
                val resStr = response.body()?.string().toString()
                val json = JSONObject(resStr)

                println(json.getString("version"))

                result = true
            }
        })

        return  result
    }
}
