package com.example.auroomcasino.ui.main


import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
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
import com.example.auroomcasino.ui.fragment.ExistingBottomFragment
import com.example.auroomcasino.utils.MyUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

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
    private var mAuth: FirebaseAuth? = null
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private var codeNumber = 1

    @SuppressLint("ResourceType", "SetJavaScriptEnabled")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadingView!!.start()
        firebaseToServer(true)
    }

    private fun firebaseToServer(boolean: Boolean) {
        val configSettings = FirebaseRemoteConfigSettings.Builder().build()
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.fetch(0).addOnCompleteListener(OnCompleteListener<Void?> { task ->
            if (task.isSuccessful) {
                remoteConfig.fetchAndActivate()
                if (boolean){
                    val urlApi = remoteConfig.getString("url_" + codeNumber.toString())
                    servesApi(urlApi)
                }else{
                    codeNumber += 1
                    val urlApi = remoteConfig.getString("url_" + codeNumber.toString())
                    if (urlApi.isNotEmpty()) {
                        servesApi(urlApi)
                    }else{
                        errorFragment()
                    }
                }
            } else {
                errorFragment()
            }
        })
    }

    private fun errorFragment(){
        val bottomSheetDialogFragment = ExistingBottomFragment()
        bottomSheetDialogFragment.isCancelable = false;
        bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag)
    }

    private fun servesApi(urlApi: String){
        CoroutineScope(Dispatchers.IO).launch {
            if (isServerOperation(urlApi)) {
                runOnUiThread { initView(urlApi) }
            }
        }
    }

    private fun isServerOperation(urlApi: String): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if (netInfo != null && netInfo.isConnected) {
            try {
                val url = URL(urlApi)
                val urlc = url.openConnection() as HttpURLConnection
                urlc.connectTimeout = 3000
                urlc.connect()
                if (urlc.responseCode == 200) {
                    return true
                }
                // also check different code for down or the site is blocked, example
                if (urlc.responseCode == 521) {
                    // Web server of the site is down
                    return false
                }
            } catch (e1: MalformedURLException) {
                e1.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        firebaseToServer(false)
        return false
    }

    private fun initView(urlApi: String) {
        customViewContainer = findViewById<View>(R.id.customViewContainer) as FrameLayout
        webView = findViewById<View>(R.id.webView) as WebView
        linearImage = findViewById(R.id.linear_image)

        initAnim()

        //Приоретет к стялям приложения
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(
                webView!!.getSettings(),
                WebSettingsCompat.FORCE_DARK_OFF
            );
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
        webView!!.loadUrl(urlApi)

        //Если размер дисплея ниже заданных параметров размер зайта 14 sp
        if (width <= 1080 && height <= 1920) {
            webSettings.defaultFontSize = 14
        }

        // Огроничение для выхода в системный браузер
        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return if (url.startsWith("tel:") || url.startsWith("viber:")) {
                    try {
                        view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                } else if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url)
                    true
                } else {
                    view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    return true
                }
            }

            //Слушатель на первичную загрузку сайта
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (visibility != 1) {
                    linearImage.visibility = View.VISIBLE
                    downloadProgress = 1
                }
            }

            //Слушатель на повторную загрузку сайта
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (downloadProgress == 1) {
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
        mAuth = FirebaseAuth.getInstance()

        mAuth!!.signInWithEmailAndPassword("auroom@mail.ru", "aurom1994")
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    initFirebase()
                }
            }
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
        override fun onShowCustomView(
            view: View,
            requestedOrientation: Int,
            callback: CustomViewCallback
        ) {
            onShowCustomView(
                view,
                callback
            ) //To change body of overridden methods use File | Settings | File Templates.
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
            return super.shouldOverrideUrlLoading(
                view,
                url
            ) //To change body of overridden methods use File | Settings | File Templates.
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