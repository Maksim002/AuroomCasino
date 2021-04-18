package com.example.auroomcasino

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.timelysoft.tsjdomcom.utils.MyUtils
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var casinoWeb: WebView
    private lateinit var dataBase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Имплементация WebView
        casinoWeb = findViewById(R.id.casino_web)
        initWebView()
    }

    private fun initFirebase() {
        //Подключаемся к базе firebase
        dataBase = FirebaseDatabase.getInstance().getReference("AuroomCasino")
        try {
            //Генерируем токен для пушей
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result?.token
                if (token != null) {
                    dataBase.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            //Если база не является пустой
                            if (snapshot.exists()) {
                                //Пробегаемся по базе и сравниваем элементы
                                for (i in snapshot.children) {
                                    //Если токен равен элементу в базе
                                    if (token.toString() == i.value){
                                        //Удоляем  элемент
                                        dataBase.child(MyUtils.toMyKey(token)).removeValue()

                                    }
                                }
                                //Пробегаемся по базе и подсчитываем элементы
                                for (j in 0..snapshot.childrenCount){
                                    //Если элемент в базе является последним
                                    if (j >= snapshot.childrenCount){
                                        //Добавляем новый элемент
                                        dataBase.child(MyUtils.toMyKey(token)).setValue(token)
                                    }
                                }
                            } else {
                                //Если база пуста добавляем элемент
                                dataBase.child(MyUtils.toMyKey(token)) .setValue(token)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_LONG).show()
                        }
                    })
                }
            })

        } catch (e: Exception) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        //Ключи webView
        val webSettings: WebSettings = casinoWeb.getSettings()
        webSettings.javaScriptEnabled = true
        casinoWeb.loadUrl("https://auroombet.com/ru")

        // Огроничение для выхода в системный браузер
        casinoWeb.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initFirebase()
        //для Работы в начном режиме
//        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
//            WebSettingsCompat.setForceDark(casinoWeb.settings, WebSettingsCompat.FORCE_DARK_OFF)
//        }
    }
}