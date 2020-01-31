package com.farm.farmestar

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.onesignal.OneSignal
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    //Shared Prefs info
    var prefs: Prefs? = null
    var stateVersion = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = Prefs(this)
        println("Version from Pref: " + prefs?.version)


        //TODO: url is set to staging, before production this needs to be pushed to prod env
//        var farmestarURL = "https://farmestar.herokuapp.com/"
        var farmestarURL = "https//farmestar-prod.herokuapp.com"

        //Setup WebView for FarmEstar
        val farmestarWebView = WebView(this)
        farmestarWebView.loadUrl(farmestarURL)

        //WebView Client
        farmestarWebView.webChromeClient = WebChromeClient()

        //Web Settings
        farmestarWebView.settings.javaScriptEnabled = true
        farmestarWebView.settings.domStorageEnabled = true
        farmestarWebView.settings.javaScriptCanOpenWindowsAutomatically = true

        //Make Call to Check version
        run("https//farmestar-prod.herokuapp.com/api/app/version", farmestarWebView)


        // OneSignal Implementation
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        //Set the Content View After application Configuration
        setContentView(farmestarWebView)
    }

    //Version API call
    fun run(url: String, webView: WebView): Boolean {
        val request = Request.Builder()
            .url(url)
            .build()
        var result = false
        val version = prefs!!.version

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { result = false}
            override fun onResponse(call: Call, response: Response){
                val resStr = response.body()?.string().toString()
                val jsonArray = JSONArray(resStr)
                val json = jsonArray.getJSONObject(0)

                //Parse Version
                val appVersion = json.getString("version")
                val needsUpdate = compare(version, appVersion)


                if (needsUpdate){
                    println("Updating...")
                    //Update Clear Cache
                    webView?.post{
                        webView.clearCache(true)
                        webView.reload()
                    }
                    //Update Prefs to new version string
                    prefs?.version = stateVersion
                }else {
                    println("State Version: $stateVersion")
                    println("Prefs Version: $version")
                    println("No Update")
                }
            }
        })
        return  result
    }

    private fun compare(v1: String?, v2: String?): Boolean {
        val s1 = normalisedVersion(v1)
        val s2 = normalisedVersion(v2)
        val cmp = s1!!.compareTo(s2!!)
        val cmpStr = if (cmp > 0) false else cmp < 0
        println("CompString: $cmpStr")
        if (cmpStr){
            println("V2: $v2")
            stateVersion = v2!!
        }
        return cmpStr
    }

    private fun normalisedVersion(version: String?): String? {
        return normalisedVersion(version, ".", 8)
    }

    private fun normalisedVersion(
        version: String?,
        sep: String?,
        maxWidth: Int
    ): String? {
        val split: Array<String> =
            Pattern.compile(sep, Pattern.LITERAL).split(version)
        val sb = StringBuilder()
        for (s in split) {
            sb.append(String.format("%" + maxWidth + 's', s))
        }
        return sb.toString()
    }
}
