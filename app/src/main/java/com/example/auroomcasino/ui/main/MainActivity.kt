package com.example.auroomcasino.ui.main

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
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
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var dataBase: DatabaseReference
    private var webView: WebView? = null
    private var customViewContainer: FrameLayout? = null
    private var customViewCallback: CustomViewCallback? = null
    private var mCustomView: View? = null
    private var mWebChromeClient: myWebChromeClient? = null
    private var mWebViewClient: myWebViewClient? = null
    private var visibility = 0
    private var downloadProgress = 0
    private lateinit var linearImage: ConstraintLayout

    @SuppressLint("ResourceType", "SetJavaScriptEnabled")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customViewContainer = findViewById<View>(R.id.customViewContainer) as FrameLayout
        webView = findViewById<View>(R.id.webView) as WebView
        linearImage = findViewById(R.id.linear_image)

        initAnim()

        //Приоретет к стялям приложения
        if(WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webView!!.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
            if (Build.VERSION.SDK_INT >= 21) {
                this.supportActionBar?.show()
                getWindow().setStatusBarColor(getResources().getColor(R.color.black));
            }
        }

        //Опредиляет размеры дисплея
        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val height: Int = Resources.getSystem().displayMetrics.heightPixels

        //Ключи связки webView
        val webSettings = webView!!.settings
        mWebViewClient = myWebViewClient()
        webView!!.settings.javaScriptCanOpenWindowsAutomatically = true;
        webView!!.settings.databaseEnabled = true;
        webView!!.webViewClient = mWebViewClient!!
        mWebChromeClient = myWebChromeClient()
        webView!!.webChromeClient = mWebChromeClient
        webView!!.getSettings().setAppCacheEnabled(true);
        webView!!.settings.setAppCacheEnabled(true)
        webView!!.settings.saveFormData = true
        webView!!.settings.javaScriptEnabled = true;
        webView!!.settings.domStorageEnabled = true;

        //Скармливаю url сайта
        webView!!.loadUrl("https://auroombet.com/ru")

        //Если размер дисплея ниже заданных параметров размер зайта 14 sp
        if (width <= 1080 && height <= 1920){
            webSettings.defaultFontSize = 14
        }


        // Огроничение для выхода в системный браузер
        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            //Слушатель на первичную загрузку сайта
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (visibility != 1){
                    loadingView!!.start()
                    linearImage.visibility = View.VISIBLE
                    downloadProgress = 1
                }
            }

            //Слушатель на повторную загрузку сайта
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (downloadProgress == 1){
                    loadingView!!.stop()
                    linearImage.visibility = View.GONE
                    visibility = 1
                }
            }
        }
    }

    private fun initAnim() {
        loadingView!!.start()
    }

    fun inCustomView(): Boolean {
        return mCustomView != null
    }

    fun hideCustomView() {
        mWebChromeClient!!.onHideCustomView()
    }

    override fun onStart() {
        super.onStart()
        initFirebase()
    }

    override fun onStop() {
        super.onStop() //To change body of overridden methods use File | Settings | File Templates.
        if (inCustomView()) {
            hideCustomView()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (inCustomView()) {
                hideCustomView()
                return true
            }
            if (mCustomView == null && webView!!.canGoBack()) {
                webView!!.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    internal inner class myWebChromeClient : WebChromeClient() {
        private var mVideoProgressView: View? = null
        override fun onShowCustomView(view: View, requestedOrientation: Int, callback: CustomViewCallback) {
            onShowCustomView(view, callback) //To change body of overridden methods use File | Settings | File Templates.
        }



        override fun onShowCustomView(view: View, callback: CustomViewCallback) {

            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden()
                return
            }
            mCustomView = view
            webView!!.visibility = View.GONE
            customViewContainer!!.visibility = View.VISIBLE
            customViewContainer!!.addView(view)
            customViewCallback = callback
        }

        override fun getVideoLoadingProgressView(): View? {
            if (mVideoProgressView == null) {
                val inflater = LayoutInflater.from(this@MainActivity)
                mVideoProgressView = inflater.inflate(R.layout.vidio_progres, null)
            }
            return mVideoProgressView
        }



        override fun onHideCustomView() {
            super.onHideCustomView() //To change body of overridden methods use File | Settings | File Templates.
            if (mCustomView == null) return
            webView!!.visibility = View.VISIBLE
            customViewContainer!!.visibility = View.GONE

            // Hide the custom view.
            mCustomView!!.visibility = View.GONE

            // Remove the custom view from its container.
            customViewContainer!!.removeView(mCustomView)
            customViewCallback!!.onCustomViewHidden()
            mCustomView = null
        }
    }

    internal inner class myWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return super.shouldOverrideUrlLoading(view, url) //To change body of overridden methods use File | Settings | File Templates.
        }
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
                            Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_LONG)
                                .show()
                        }
                    })
                }
            })

        } catch (e: Exception) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        }
    }
}