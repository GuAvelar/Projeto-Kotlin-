package br.com.projetointegrador.keepsafe
import android.Manifest
import ConfigFragment
import HomeFragment
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import br.com.projetointegrador.keepsafe.databinding.ActivityForgotPasswordBinding
import br.com.projetointegrador.keepsafe.databinding.ActivityHomeBinding
import br.com.projetointegrador.keepsafe.databinding.ActivityMainBinding
import br.com.projetointegrador.keepsafe.databinding.ActivityRegisterBinding
import br.com.projetointegrador.keepsafe.fragments.MapFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {
    private var binding: ActivityHomeBinding? = null

    private val REQUEST_LOCATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val homeFragment = HomeFragment()
        val mapFragment = MapFragment()
        val settingsFragment = ConfigFragment()

        makeCurrentFragment(homeFragment)

        binding?.bottomNavigation?.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.ic_home -> makeCurrentFragment(homeFragment)
                R.id.ic_map -> makeCurrentFragment(mapFragment)
                R.id.ic_settings -> makeCurrentFragment(settingsFragment)
            }
            true
        }

        requestLocationPermission()
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }

    override fun onDestroy(){
        super.onDestroy()
        binding = null
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION_CODE
            )
        } else {

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission denied, inform the user or take appropriate action
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
