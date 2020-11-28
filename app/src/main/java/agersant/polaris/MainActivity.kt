package agersant.polaris

import agersant.polaris.databinding.ActivityMainBinding
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        App.appBarLayout = binding.appBarLayout
        App.toolbar = binding.toolbar

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_collection,
                R.id.nav_playlists,
                R.id.nav_now_playing,
                R.id.nav_queue,
                R.id.nav_search,
            ),
            binding.drawerLayout,
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
        binding.drawerNavigation.setupWithNavController(navController)
    }
}