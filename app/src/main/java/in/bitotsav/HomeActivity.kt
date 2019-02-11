package `in`.bitotsav

import `in`.bitotsav.databinding.ActivityHomeBinding
import `in`.bitotsav.shared.ui.UiUtilViewModel
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.thelittlefireman.appkillermanager.managers.KillerManager
import com.thelittlefireman.appkillermanager.ui.DialogKillerManagerBuilder
import org.koin.androidx.viewmodel.ext.viewModel

class HomeActivity : AppCompatActivity() {

    enum class Theme(@StyleRes val themeRes: Int) {
        RED(R.style.AppThemeRed),
        GREEN(R.style.AppTheme)
    }

    private val uiUtilViewModel by viewModel<UiUtilViewModel>()
    private lateinit var binding: ActivityHomeBinding
    val primaryColor by lazy {
        TypedValue().apply {
            theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.values().random().themeRes)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = uiUtilViewModel
        uiUtilViewModel.mColor = primaryColor
        binding.lifecycleOwner = this
        handlePlatformLimitations()
        setupBottomNavMenu()

        // AppKillerManager
        startDialog(KillerManager.Actions.ACTION_AUTOSTART)
        startDialog(KillerManager.Actions.ACTION_NOTIFICATIONS)
        startDialog(KillerManager.Actions.ACTION_POWERSAVING)
    }

    private fun setupBottomNavMenu() {
        val navController = Navigation.findNavController(this, R.id.mainFragment)
        findViewById<BottomNavigationView>(R.id.mainNavigation)
            .setupWithNavController(navController)
    }

    private fun handlePlatformLimitations() {
        when {
            // Can't have light status bar in older than M due to white-only icons
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> with(window) {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = primaryColor
            }
        }
    }

    private fun startDialog(actions: KillerManager.Actions) {
        DialogKillerManagerBuilder().setContext(this).setAction(actions).show()
    }
}
