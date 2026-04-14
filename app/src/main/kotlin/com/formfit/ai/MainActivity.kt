package com.formfit.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.formfit.ai.navigation.AppNavGraph
import com.formfit.ai.ui.theme.FormFitTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.parseFragmentAndImportSession
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { false }

        enableEdgeToEdge()

        handleDeepLinkIntent(intent)

        setContent {
            FormFitTheme {
                AppNavGraph()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        val scheme = uri.scheme ?: return

        if (scheme == "formfitai") {
            val fragment = uri.fragment
            if (!fragment.isNullOrBlank()) {
                lifecycleScope.launch {
                    runCatching {
                        supabaseClient.auth.parseFragmentAndImportSession(fragment)
                    }
                }
            }
        }
    }
}
