package com.example.auroomcasino.ui.main

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.*
import android.webkit.WebSettings.PluginState
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.auroomcasino.R
import com.example.auroomcasino.utils.MyUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.timelysoft.tsjdomcom.utils.animOne
import com.timelysoft.tsjdomcom.utils.animThree
import com.timelysoft.tsjdomcom.utils.animTwo
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var casinoWeb: WebView
    private lateinit var dataBase: DatabaseReference
    private lateinit var linearImage: ConstraintLayout
    private var visibility = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Имплементация WebView
        casinoWeb = findViewById(R.id.casino_web)
        linearImage = findViewById(R.id.linear_image)

        initAnim()
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
                                    if (token.toString() == i.value) {
                                        //Удоляем  элемент
                                        dataBase.child(MyUtils.toMyKey(token)).removeValue()

                                    }
                                }
                                //Пробегаемся по базе и подсчитываем элементы
                                for (j in 0..snapshot.childrenCount) {
                                    //Если элемент в базе является последним
                                    if (j >= snapshot.childrenCount) {
                                        //Добавляем новый элемент
                                        dataBase.child(MyUtils.toMyKey(token)).setValue(token)
                                    }
                                }
                            } else {
                                //Если база пуста добавляем элемент
                                dataBase.child(MyUtils.toMyKey(token)).setValue(token)
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

    private fun initAnim() {
        val imageOne: ImageView = findViewById(R.id.im)
        val imageTwo: ImageView = findViewById(R.id.im1)
        val imageThree: ImageView = findViewById(R.id.im2)
        animOne(this, imageOne)
        animTwo(this, imageTwo)
        animThree(this, imageThree)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {

        //Приоретет к стялям приложения
        if(WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(casinoWeb.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
            if (Build.VERSION.SDK_INT >= 21) {
                this.supportActionBar?.show()
                getWindow().setStatusBarColor(getResources().getColor(R.color.black));
            }
        }

        //Ключи webView
        val settings: WebSettings = casinoWeb.getSettings()
        settings.pluginState = PluginState.OFF
        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)
        casinoWeb.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN;
        casinoWeb.loadUrl("https://auroombet.com/ru");
        // Огроничение для выхода в системный браузер
        casinoWeb.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (visibility != 1){
                linearImage.visibility = View.VISIBLE
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                linearImage.visibility = View.GONE
                visibility = 1
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initFirebase()
    }
}