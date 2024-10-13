import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import br.com.projetointegrador.keepsafe.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class PlaceBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLocation: Location
    private var locationCallback: LocationCallback? = null
    private var lockerLatitude: Double = 0.0
    private var lockerLongitude: Double = 0.0
    private lateinit var view: View // Definindo uma variável para a view

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.bottom_sheet_layout, container, false)

        // Obter os argumentos passados para o fragmento
        val args = arguments

        // Verificar se os argumentos não são nulos antes de acessá-los
        if (args != null) {
            // Inicializar o FusedLocationProviderClient
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

            // Verificar a permissão de acesso à localização
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Obter a localização atual do usuário
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            userLocation = it
                            // Obter a localização do armário
                            lockerLatitude = args.getDouble("latitude")
                            lockerLongitude = args.getDouble("longitude")

                            // Calcular a distância entre o usuário e o armário
                            val distance = userLocation.distanceTo(Location("").apply {
                                latitude = lockerLatitude
                                longitude = lockerLongitude
                            })

                            // Verificar se a distância é maior que 500 metros
                            if (distance >= 500) {
                                // Se a distância for maior ou igual a 500 metros, exibir um alerta e não mostrar o BottomSheet
                                showDistanceAlert()
                                dismiss() // Fecha o BottomSheet
                            } else {
                                configureViews()
                            }
                        }
                    }
            }
        }

        return view
    }

    private fun configureViews() {
        // Obter referências para os TextViews e botões
        val addressTextView = view.findViewById<TextView>(R.id.addressTextView)
        val openingTimeTextView = view.findViewById<TextView>(R.id.openingTimeTextView)
        val closingTimeTextView = view.findViewById<TextView>(R.id.closingTimeTextView)
        val hourlyPriceTextView = view.findViewById<TextView>(R.id.hourlyPriceTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        val placeTextView = view.findViewById<TextView>(R.id.placeTextView)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        val qrCodeImageView = view.findViewById<ImageView>(R.id.qrCodeImageView)
        qrCodeImageView.visibility = View.GONE

        val auth = FirebaseAuth.getInstance()

        // Configurar o OnClickListener para o botão de cancelar
        cancelButton.setOnClickListener {
            // Fecha o BottomSheetDialogFragment
            dismiss()
        }

        // Configurar o OnClickListener para o botão de confirmar
        confirmButton.setOnClickListener {
            // Verificar se o usuário está autenticado no Firebase
            if (auth.currentUser != null) {
                val userId = auth.currentUser!!.uid
                val args = arguments

                // Obtém uma instância do Firestore
                val db = FirebaseFirestore.getInstance()

                // Verifica se o usuário já tem uma reserva que não está finalizada
                db.collection("users").document(userId)
                    .collection("lockers")
                    .whereNotEqualTo("status", "finished")
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            // O usuário já tem uma reserva não finalizada, exibir um alerta
                            showExistingReservationAlert()
                        } else {
                            // Verificar se o usuário tem um cartão de crédito cadastrado
                            db.collection("cardCreditUsers").document(userId).get()
                                .addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        // O usuário tem um cartão de crédito cadastrado, criar uma nova reserva
                                        val reservationId = UUID.randomUUID().toString()
                                        val reservationData = hashMapOf(
                                            "status" to "pending",
                                            "userId" to userId,
                                            "rentalTime" to FieldValue.serverTimestamp(),
                                            "placeName" to args?.getString("placeName")
                                            // Adicione outros campos da reserva, se necessário
                                        )

                                        db.collection("users").document(userId)
                                            .collection("lockers").document(reservationId)
                                            .set(reservationData)
                                            .addOnSuccessListener {
                                                showSuccessDialog()
                                                Log.d(TAG, "Reserva adicionada com sucesso com ID: $reservationId")
                                                dismiss()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "Erro ao adicionar reserva", e)
                                            }
                                    } else {
                                        // Usuário não tem cartão cadastrado, exibir um alerta
                                        showNoCreditCardAlert()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Erro ao verificar o cartão de crédito do usuário", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Erro ao verificar reserva existente", e)
                    }
            } else {
                // O usuário não está autenticado, exibir um alerta
                showLoginAlert()
            }
        }
        // Obter os argumentos passados para o fragmento
        val args = arguments

        // Verificar se os argumentos não são nulos antes de acessá-los
        if (args != null) {
            // Configurar os TextViews com os dados dos argumentos
            addressTextView.text = "Endereço: ${args.getString("address")}"
            openingTimeTextView.text = "Horário de abertura: ${args.getString("openingTime")}"
            closingTimeTextView.text = "Horário de fechamento: ${args.getString("closingTime")}"
            hourlyPriceTextView.text = "Preço por hora: ${args.getString("hourlyPrice")}"
            placeTextView.text = "${args.getString("placeName")}"
            descriptionTextView.text = "${args.getString("description")}"
        }
    }

    private fun showDistanceAlert() {
        // Cria um AlertDialog informando que a distância é maior que 500 metros
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Distância Excedida")
        builder.setMessage("Você está muito longe do armário.")
        builder.setPositiveButton("OK") { dialog, _ ->
            // Fecha o AlertDialog
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }



    fun showNoCreditCardAlert() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cartão de Crédito Necessário")
            .setMessage("Você precisa cadastrar um cartão de crédito para fazer uma reserva.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // Opcional: Implementar a lógica para redirecionar o usuário para a tela de cadastro de cartão
            }
            .show()
    }


    private fun showLoginAlert() {
        // Cria um AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Login Necessário")
        builder.setMessage("Você precisa fazer login para prosseguir.")
        builder.setPositiveButton("OK") { dialog, _ ->
            // Fecha o AlertDialog
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun showExistingReservationAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reserva Existente")
            .setMessage("Você já tem uma reserva em andamento.")
            .setPositiveButton("OK", null)
            .show()
    }

    fun showSuccessDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reserva Feita com Sucesso")
            .setMessage("Sua reserva foi realizada com sucesso e o QR Code está disponível na tela inicial.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // Opcional: Navegar para a tela inicial ou atualizar a UI conforme necessário
            }
            .show()
    }
}
