import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.com.projetointegrador.keepsafe.MainActivity
import br.com.projetointegrador.keepsafe.RegisterCardActivity
import br.com.projetointegrador.keepsafe.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.progressBar.visibility = View.VISIBLE
        binding.contentView.visibility = View.GONE
        binding.contentViewUserNotSigned.visibility = View.GONE
        binding.cardUser.visibility = View.GONE

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnUserHomeToLogin.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        binding.btnNewCard.setOnClickListener {
            val intent = Intent(requireContext(), RegisterCardActivity::class.java)
            startActivity(intent)
        }

        if (auth.currentUser != null) {
            // Usuário está logado, verificar se ele tem um documento na coleção
            val userId = auth.currentUser!!.uid
            val docRef = firestore.collection("cardCreditUsers").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Documento encontrado, obter os dados
                        val cardNumber = document.getString("cardNumber") ?: ""
                        val cvv = document.getString("cvv") ?: ""
                        val expirationDate = document.getString("expirationDate") ?: ""

                        // Definir os valores nos campos do layout usando Data Binding
                        binding.tvCardNumber.text = "$cardNumber"
                        binding.tvCVV.text = "CVV: $cvv"
                        binding.tvExpirationDate.text = "Data: $expirationDate"

                        // Exibir o conteúdo
                        binding.cardUser.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    } else {
                        // Documento não encontrado, ocultar conteúdo e exibir mensagem
                        binding.contentView.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        // Exibir mensagem informando que o usuário não tem um documento na coleção
                    }
                }
                .addOnFailureListener { e ->
                    // Erro ao acessar o Firestore, lidar com o erro conforme necessário
                }
        } else {
            // Usuário não está logado, ocultar conteúdo e redirecionar para tela de login (exemplo)
            binding.contentView.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.contentViewUserNotSigned.visibility = View.VISIBLE
        }

        // Verificar se o usuário tem algum armário com status pendente
        checkPendingLockers()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPendingLockers() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = firestore.collection("users").document(userId)
            userRef.collection("lockers").whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val placeName = document.getString("placeName")
                        val lockerId = document.id
                        Log.d(
                            "Lockers",
                            "Document found - Place Name: $placeName, Locker ID: $lockerId"
                        )
                        val user = FirebaseAuth.getInstance().currentUser
                        user?.let {
                            val userId = user.uid
                            generateQRCode(userId)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Tratar falha na obtenção dos lockers pendentes
                }
        }
    }

    private fun generateQRCode(userId: String?) {
        if (userId != null) {
            val multiFormatWriter = MultiFormatWriter()
            try {
                val text = userId
                val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                // Exibir o QR Code, substituindo o nome do ImageView abaixo pelo seu ID real
                binding.qrCodeImageView.setImageBitmap(bitmap)
                binding.cardQRCode.visibility = View.VISIBLE // Mostrar o cartão
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
