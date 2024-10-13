package br.com.projetointegrador.keepsafe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.projetointegrador.keepsafe.databinding.ActivityQrCodeScannerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QrCodeScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeScannerBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Configura o botão de scan
        binding.btnScan.setOnClickListener {
            val integrator = IntentIntegrator(this).apply {
                setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                setPrompt("Scan a QR Code")
                setCameraId(0) // Use a câmera traseira
                setBeepEnabled(true)
                setBarcodeImageEnabled(true)
            }
            integrator.initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // Escaneamento bem-sucedido, vamos procurar o usuário
                val userId = result.contents
                searchUserAndCheckLockers(userId)
            } else {
                binding.etFeedback.text = "Escaneamento cancelado"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun searchUserAndCheckLockers(userId: String) {
        // Referência para a coleção "users"
        val usersCollectionRef = db.collection("users")

        // Consulta para buscar o documento do usuário com base no ID fornecido
        usersCollectionRef.document(userId).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    // O usuário foi encontrado, agora vamos buscar os lockers
                    // Referência para a coleção "lockers" dentro do documento do usuário
                    val lockersCollectionRef = usersCollectionRef.document(userId).collection("lockers")

                    // Consulta para buscar os lockers pendentes associados a este usuário
                    lockersCollectionRef.whereEqualTo("status", "pending").get()
                        .addOnSuccessListener { lockersQuerySnapshot ->
                            if (!lockersQuerySnapshot.isEmpty) {
                                // Existe pelo menos um locker com status "pending" associado ao usuário
                                // Redirecionar para a tela de tirar fotos e passar o ID do usuário
                                lockersQuerySnapshot.forEach { documentSnapshot ->
                                    val lockerId = documentSnapshot.id
                                    val intent = Intent(this, TakePhotosActivity::class.java)
                                    intent.putExtra("docID", lockerId)
                                    intent.putExtra("userId", userId)
                                    startActivity(intent)
                                }
                            } else {
                                // Não existem lockers pendentes para este usuário
                                binding.etFeedback.text = "Usuário não tem nenhuma reserva pendente."
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Erro ao buscar lockers
                            binding.etFeedback.text = "Erro ao buscar lockers: ${exception.message}"
                        }
                } else {
                    // Usuário não encontrado
                    binding.etFeedback.text = "Usuário não encontrado."
                }
            }
            .addOnFailureListener { exception ->
                // Erro ao buscar usuário
                binding.etFeedback.text = "Erro ao buscar usuário: ${exception.message}"
            }
    }
}