package br.com.projetointegrador.keepsafe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class FinishLockerActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_locker)

        // Recuperar os dados da tag da intent
        val userId = intent.getStringExtra("userId")
        val userDocId = intent.getStringExtra("userDocId")

        // Verificar se os IDs do usuário e do documento são válidos
        if (userId != null && userDocId != null) {
            // Buscar os nomes dos arquivos de imagem no Firestore
            getImageFileNames(userId, userDocId)
        } else {
            Toast.makeText(this, "Dados inválidos.", Toast.LENGTH_SHORT).show()
        }

        // Adicionar listener ao botão de finalizar locação
        val finishButton = findViewById<Button>(R.id.finishButton)
        finishButton.setOnClickListener {
            if (userId != null) {
                if (userDocId != null) {
                    showConfirmationDialog(userId, userDocId)
                }
            }
        }
    }

    private fun getImageFileNames(userId: String, userDocId: String) {
        // Referência ao documento no Firestore
        val lockerRef = firestore.collection("users").document(userId).collection("lockers").document(userDocId)

        // Buscar os nomes dos arquivos de imagem
        lockerRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // O documento existe, agora vamos obter os nomes dos arquivos
                    val imageFileNames = documentSnapshot.get("imageFileNames") as? List<String>
                    if (imageFileNames != null) {
                        // Os nomes dos arquivos foram obtidos com sucesso
                        Log.d(TAG, "Nomes dos arquivos de imagem: $imageFileNames")
                        // Agora você pode fazer o que precisa com os nomes dos arquivos
                        // Por exemplo, você pode exibir os nomes em um RecyclerView
                        displayImages(imageFileNames)
                    } else {
                        Log.d(TAG, "Nenhum nome de arquivo de imagem encontrado.")
                    }
                } else {
                    Log.d(TAG, "Documento não encontrado.")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao buscar nomes dos arquivos de imagem:", e)
                // Exibir uma mensagem de erro para o usuário
                Toast.makeText(this, "Erro ao buscar nomes dos arquivos de imagem: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayImages(imageFileNames: List<String>) {
        // Referência ao contêiner de imagens no layout XML
        val imageContainer = findViewById<LinearLayout>(R.id.imageContainer)

        // Para cada nome de arquivo, exibir a imagem correspondente no ImageView
        imageFileNames.forEach { fileName ->
            // Criar uma ImageView programaticamente
            val imageView = ImageView(this)

            // Configurar a largura e a altura da ImageView para garantir que fiquem em pé
            val layoutParams = LinearLayout.LayoutParams(
                0, // largura 0dp para usar layout_weight
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f // layout_weight 1 para distribuir o espaço igualmente
            )
            layoutParams.setMargins(8, 8, 8, 8) // adicionar margens entre as imagens

            // Adicionar a ImageView ao contêiner de imagens
            imageContainer.addView(imageView, layoutParams)

            // Carregar a imagem do Firebase Storage
            val storageReference = Firebase.storage.reference.child("images/$fileName")

            Log.d(TAG, "Caminho da imagem: ${storageReference.path}")

            // Obter a URL de download da imagem
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    Log.d(TAG, "URL de download: $uri")
                    // Usar Picasso para carregar a imagem
                    Picasso.get().load(uri).rotate(90f).fit().centerInside().into(imageView)
                }
                .addOnFailureListener { exception ->
                    // Tratamento de falha
                    Log.e(TAG, "Erro ao obter URL de download: ${exception.message}")
                    exception.printStackTrace()
                }
        }
    }

    private fun showConfirmationDialog(userId: String, userDocId: String) {
        // Criar o diálogo de confirmação
        AlertDialog.Builder(this)
            .setTitle("Confirmar Finalização")
            .setMessage("Você tem certeza de que deseja finalizar a locação?")
            .setPositiveButton("Sim") { _, _ ->
                // Alterar o status para "finished"
                updateLockerStatus(userId, userDocId)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun updateLockerStatus(userId: String, userDocId: String) {
        // Referência ao documento no Firestore
        val lockerRef = firestore.collection("users").document(userId).collection("lockers").document(userDocId)

        // Atualizar o status para "finished"
        lockerRef.update("status", "finished")
            .addOnSuccessListener {
                Toast.makeText(this, "Locação finalizada com sucesso.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeGerenteActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao finalizar a locação: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val TAG = "FinishLockerActivity"
    }
}
