package com.cotzul.aprobarpedido.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.cotzul.aprobarpedido.R
import com.cotzul.aprobarpedido.ui.fragment.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navView: BottomNavigationView
    private var doubleBackToExitPressedOnce = false

    // Fragments del menú principal (una sola instancia)
    private val homeFragment = HomeFragment()
    private val rentabilidadFragment = RentabilidadFragment()
    private val preciosFragment = PreciosFragment()
    private val perfilFragment = PerfilFragment()
    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navView = findViewById(R.id.bottom_navigation)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    // 🔹 Solo el login visible
                    findViewById<View>(R.id.nav_host_fragment).visibility = View.VISIBLE
                    findViewById<View>(R.id.main_container).visibility = View.GONE
                    navView.visibility = View.GONE
                    hideAllMainFragments()
                }

                // 🔹 Cuando ya entraste al dashboard (Home, etc.)
                R.id.homeFragment,
                R.id.rentabilidadFragment,
                R.id.preciosFragment,
                R.id.perfilFragment -> {
                    mostrarDashboard()
                }

                // 🔹 EditarPedido se muestra dentro del Home (en su propio nav interno)
                R.id.editarPedidoFragment -> {
                    findViewById<View>(R.id.nav_host_fragment).visibility = View.GONE
                    findViewById<View>(R.id.main_container).visibility = View.VISIBLE
                    navView.visibility = View.VISIBLE
                }

                else -> {
                    // Por si aparecen pantallas internas extra
                    findViewById<View>(R.id.nav_host_fragment).visibility = View.VISIBLE
                    findViewById<View>(R.id.main_container).visibility = View.GONE
                    navView.visibility = View.GONE
                }
            }
        }


        // 🔹 BottomNavigation: cambia de fragment sin destruirlos
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> switchFragment(homeFragment)
                R.id.rentabilidadFragment -> switchFragment(rentabilidadFragment)
                R.id.preciosFragment -> switchFragment(preciosFragment)
                R.id.perfilFragment -> switchFragment(perfilFragment)
            }
            true
        }

        onBackPressedDispatcher.addCallback(this@MainActivity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (activeFragment != homeFragment) {
                    navView.selectedItemId = R.id.homeFragment
                    switchFragment(homeFragment)
                } else {
                    if (doubleBackToExitPressedOnce) {
                        finishAffinity()
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(
                        this@MainActivity,
                        "Presiona de nuevo para salir",
                        Toast.LENGTH_SHORT
                    ).show()

                    android.os.Handler(mainLooper).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            }
        })
    }

    // ✅ Inicializa los fragments del menú principal (solo una vez)
    private fun inicializarFragments() {
        if (activeFragment != null) return

        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, perfilFragment, "perfil").hide(perfilFragment)
            .add(R.id.main_container, preciosFragment, "precios").hide(preciosFragment)
            .add(R.id.main_container, rentabilidadFragment, "rentabilidad").hide(rentabilidadFragment)
            .add(R.id.main_container, homeFragment, "home")
            .commit()

        activeFragment = homeFragment
    }

    // ✅ Cambia entre fragments sin destruirlos
    private fun switchFragment(target: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        activeFragment?.let { transaction.hide(it) }

        if (target.isAdded) {
            transaction.show(target)
        } else {
            transaction.add(R.id.main_container, target)
        }

        transaction.commitAllowingStateLoss()
        activeFragment = target
    }

    // ✅ Oculta todos los fragments principales
    private fun hideAllMainFragments() {
        val transaction = supportFragmentManager.beginTransaction()
        listOf(homeFragment, rentabilidadFragment, preciosFragment, perfilFragment).forEach {
            if (it.isAdded) transaction.hide(it)
        }
        transaction.commitAllowingStateLoss()
        activeFragment = null
    }

    fun mostrarDashboard() {
        findViewById<View>(R.id.nav_host_fragment).visibility = View.GONE
        findViewById<View>(R.id.main_container).visibility = View.VISIBLE
        navView.visibility = View.VISIBLE
        inicializarFragments()
    }


    fun abrirEditarPedido(args: Bundle) {
        val navController = navHostFragment.navController
        navController.navigate(R.id.editarPedidoFragment, args)
    }


    // ✅ Diálogo de confirmación de salida
    private fun mostrarDialogoSalida() {
        AlertDialog.Builder(this)
            .setTitle("Sistema")
            .setMessage("¿Deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                val navController = navHostFragment.navController
                navController.popBackStack(R.id.loginFragment, false)
                navController.navigate(R.id.loginFragment)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
