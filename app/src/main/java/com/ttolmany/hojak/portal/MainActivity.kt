package com.ttolmany.hojak.portal

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val URL_DIARY = "https://jkeol-ubuntu.tail364926.ts.net:8790"
        private const val URL_BOATS = "https://jkeolham.github.io/hojak-boats/"
        private const val URL_RECIPES = "https://jkeolham.github.io/hojak-recipes/"
        private const val URL_SEASON = "https://jkeolham.github.io/hojak-season-2027/"

        private val HOJAK_DOMAINS = listOf(
            "tail364926.ts.net",
            "jkeolham.github.io",
            "localhost",
            "127.0.0.1"
        )
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var webContainer: FrameLayout
    private lateinit var bottomNav: BottomNavigationView

    // 4개 탭 각각의 독립된 컨테이너 (SwipeRefreshLayout + WebView)
    private lateinit var diarySwipe: SwipeRefreshLayout
    private lateinit var boatsSwipe: SwipeRefreshLayout
    private lateinit var recipesSwipe: SwipeRefreshLayout
    private lateinit var seasonSwipe: SwipeRefreshLayout

    private lateinit var diaryWebView: WebView
    private lateinit var boatsWebView: WebView
    private lateinit var recipesWebView: WebView
    private lateinit var seasonWebView: WebView

    private var currentTabId = R.id.menu_diary
    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootLayout = findViewById<View>(R.id.rootLayout)
        progressBar = findViewById(R.id.progressBar)
        webContainer = findViewById(R.id.webContainer)
        bottomNav = findViewById(R.id.bottomNav)

        // 시스템 바(상단 상태바, 하단 제스처 내비게이션 바) 안전 영역(WindowInsets) 동적 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, insets ->
            val statusBarInset = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // 상단 여백: 상태바 높이만큼 상단 패딩 적용 (상태바와 웹뷰 콘텐츠 겹침 방지 및 쾌적한 상단 여백)
            rootLayout.updatePadding(top = statusBarInset.top)

            // 하단 여백: 시스템 제스처바/내비게이션바 높이만큼 바텀 내비게이션 하단 패딩 적용 (글자 겹침 방지)
            bottomNav.updatePadding(bottom = navBarInset.bottom)

            insets
        }

        setupWebViews()
        setupBottomNav()
        setupBackNavigation()
    }

    private fun setupWebViews() {
        // 1. 조행일지 (PWA)
        val (dSwipe, dWv) = createWebViewTab(URL_DIARY)
        diarySwipe = dSwipe
        diaryWebView = dWv
        webContainer.addView(diarySwipe)

        // 2. 선사정보 바로가기
        val (bSwipe, bWv) = createWebViewTab(URL_BOATS)
        boatsSwipe = bSwipe
        boatsWebView = bWv
        boatsSwipe.visibility = View.GONE
        webContainer.addView(boatsSwipe)

        // 3. 집밥 레시피 노트
        val (rSwipe, rWv) = createWebViewTab(URL_RECIPES)
        recipesSwipe = rSwipe
        recipesWebView = rWv
        recipesSwipe.visibility = View.GONE
        webContainer.addView(recipesSwipe)

        // 4. 2027 쭈갑시즌 예약 추천일
        val (sSwipe, sWv) = createWebViewTab(URL_SEASON)
        seasonSwipe = sSwipe
        seasonWebView = sWv
        seasonSwipe.visibility = View.GONE
        webContainer.addView(seasonSwipe)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebViewTab(initialUrl: String): Pair<SwipeRefreshLayout, WebView> {
        val swipe = SwipeRefreshLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setColorSchemeColors(
                ContextCompat.getColor(this@MainActivity, R.color.accent_amber),
                ContextCompat.getColor(this@MainActivity, R.color.accent_dim)
            )
            setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(this@MainActivity, R.color.surface_card)
            )
        }

        val webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.bg_navy))

            CookieManager.getInstance().let { cm ->
                cm.setAcceptCookie(true)
                cm.setAcceptThirdPartyCookies(this, true)
            }

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (view?.visibility == View.VISIBLE) {
                        if (newProgress < 100) {
                            progressBar.visibility = View.VISIBLE
                            progressBar.progress = newProgress
                        } else {
                            progressBar.visibility = View.GONE
                        }
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    return handleUrlNavigation(view, url)
                }

                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url == null) return false
                    return handleUrlNavigation(view, url)
                }

                private fun handleUrlNavigation(view: WebView?, url: String): Boolean {
                    // Hojak 생태계 도메인은 웹뷰 내부에서 로딩
                    val isHojakDomain = HOJAK_DOMAINS.any { domain -> url.contains(domain, ignoreCase = true) }
                    if (isHojakDomain) {
                        return false
                    }

                    // 외부 선사 예약 사이트, 윈디, 바다타임, 유튜브 등은 외부 기본 브라우저로 실행
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addCategory(Intent.CATEGORY_BROWSABLE)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return false
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (view?.visibility == View.VISIBLE) {
                        progressBar.visibility = View.VISIBLE
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    swipe.isRefreshing = false
                    progressBar.visibility = View.GONE
                }

                @SuppressLint("WebViewClientOnReceivedSslError")
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    // Tailscale MagicDNS 등 사설 HTTPS 환경 호환을 위한 핸들링
                    val host = Uri.parse(error?.url ?: "").host ?: ""
                    if (host.contains("tail364926.ts.net") || host.contains("localhost")) {
                        handler?.proceed()
                    } else {
                        super.onReceivedSslError(view, handler, error)
                    }
                }
            }
        }

        // 스크롤이 최상단일 때만 스와이프 리프레시 활성화
        swipe.setOnChildScrollUpCallback { _, _ -> webView.scrollY > 0 }
        swipe.setOnRefreshListener { webView.reload() }
        swipe.addView(webView)

        webView.loadUrl(initialUrl)
        return Pair(swipe, webView)
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentTabId) {
                // 이미 선택된 탭을 다시 누른 경우: 최상단으로 부드럽게 스크롤
                getActiveWebView().scrollTo(0, 0)
                return@setOnItemSelectedListener true
            }

            currentTabId = item.itemId
            diarySwipe.visibility = if (currentTabId == R.id.menu_diary) View.VISIBLE else View.GONE
            boatsSwipe.visibility = if (currentTabId == R.id.menu_boats) View.VISIBLE else View.GONE
            recipesSwipe.visibility = if (currentTabId == R.id.menu_recipes) View.VISIBLE else View.GONE
            seasonSwipe.visibility = if (currentTabId == R.id.menu_season) View.VISIBLE else View.GONE

            progressBar.visibility = View.GONE
            true
        }
    }

    private fun getActiveWebView(): WebView {
        return when (currentTabId) {
            R.id.menu_boats -> boatsWebView
            R.id.menu_recipes -> recipesWebView
            R.id.menu_season -> seasonWebView
            else -> diaryWebView
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val activeWv = getActiveWebView()

                // 1. 현재 웹뷰에 뒤로갈 히스토리가 있으면 이전 페이지로
                if (activeWv.canGoBack()) {
                    activeWv.goBack()
                    return
                }

                // 2. 다른 탭에서 히스토리가 소진되면 첫 번째 탭(조행일지)으로 전환
                if (currentTabId != R.id.menu_diary) {
                    bottomNav.selectedItemId = R.id.menu_diary
                    return
                }

                // 3. 첫 번째 탭의 최상단에서 2초 내 재입력 시 안전 종료
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime < 2000L) {
                    finish()
                } else {
                    lastBackPressTime = now
                    Toast.makeText(this@MainActivity, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        // 웹 스토리지 및 쿠키의 디스크 즉시 동기화(Flush)
        CookieManager.getInstance().flush()
        if (::diaryWebView.isInitialized) diaryWebView.onPause()
        if (::boatsWebView.isInitialized) boatsWebView.onPause()
        if (::recipesWebView.isInitialized) recipesWebView.onPause()
        if (::seasonWebView.isInitialized) seasonWebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (::diaryWebView.isInitialized) diaryWebView.onResume()
        if (::boatsWebView.isInitialized) boatsWebView.onResume()
        if (::recipesWebView.isInitialized) recipesWebView.onResume()
        if (::seasonWebView.isInitialized) seasonWebView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        CookieManager.getInstance().flush()
        if (::diaryWebView.isInitialized) diaryWebView.destroy()
        if (::boatsWebView.isInitialized) boatsWebView.destroy()
        if (::recipesWebView.isInitialized) recipesWebView.destroy()
        if (::seasonWebView.isInitialized) seasonWebView.destroy()
    }
}
