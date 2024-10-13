package br.com.projetointegrador.keepsafe.fragments

import PlaceBottomSheetFragment
import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import br.com.projetointegrador.keepsafe.R
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import org.json.JSONObject

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMap()
    }


    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showLocationPermissionDialog(requireContext())
            return
        }

        // Habilitar a camada de localização do mapa
        map.isMyLocationEnabled = true

        val db = FirebaseFirestore.getInstance()
        db.collection("places")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val address = document.getString("Adress")
                    val latitude = document.getDouble("Latitude")
                    val longitude = document.getDouble("Longitude")

                    if (address != null && latitude != null && longitude != null) {
                        val dataJson = Gson().toJson(document.data)
                        val location = LatLng(latitude, longitude)
                        val markerOptions = MarkerOptions().position(location).title(address).snippet(dataJson)
                        map.addMarker(markerOptions)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Mover a câmera do mapa para a localização do usuário
                val userLatLng = LatLng(location.latitude, location.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            }
        }

        map.setOnMarkerClickListener { marker ->
            val snippet = marker.snippet.toString()

            // Analisa a string JSON usando Gson
            val gson = Gson()
            val placeData = gson.fromJson(snippet, PlaceData::class.java)

// Obtém os valores do objeto PlaceData
            val address = placeData.Adress
            val openingTime = placeData.OpeningTime
            val closingTime = placeData.ClosingTime
            val hourlyPrice = placeData.HourlyPrice
            val placeName = placeData.PlaceName
            val description = placeData.Description
            val latitude = placeData.Latitude
            val longitude = placeData.Longitude

// Cria uma instância do PlaceBottomSheetFragment
            val bottomSheetFragment = PlaceBottomSheetFragment()

// Passa os dados do marcador como argumentos para o fragmento
            val args = Bundle()
            args.putString("address", address)
            args.putString("openingTime", openingTime)
            args.putString("closingTime", closingTime)
            args.putString("hourlyPrice", hourlyPrice)
            args.putString("placeName", placeName)
            args.putString("description", description)
            args.putDouble("latitude", latitude)
            args.putDouble("longitude", longitude)
            bottomSheetFragment.arguments = args

            Log.d(TAG, "${args}")


            // Exibe o fragmento
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)

            // Retorna true para indicar que o evento foi consumido
            true
        }


    }

    data class PlaceData(
        val Adress: String,
        val ClosingTime: String,
        val HourlyPrice: String,
        val Latitude: Double,
        val Longitude: Double,
        val OpeningTime: String,
        val PlaceName: String,
        val Description: String
    )

}

private fun Any.snippet(toString: String) {

}

private fun showLocationPermissionDialog(context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Permissão de Localização Necessária")
        .setMessage("Esta aplicação precisa de permissão de localização para funcionar corretamente. Por favor, conceda permissão de localização nas configurações do aplicativo.")
        .setPositiveButton("OK") { _, _ ->

        }
        .setCancelable(false) // Impede que o usuário feche o diálogo clicando fora dele
        .show()
}