package br.com.projetointegrador.keepsafe

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.projetointegrador.keepsafe.databinding.ActivityPreviewBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileInputStream

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtenção dos dados da Intent
        val userId = intent.getStringExtra("userId")
        val userDocId = intent.getStringExtra("docID")
        val imagePaths = intent.getStringArrayListExtra("imagePaths")

        // Verificação e exibição das imagens
        if (imagePaths != null) {
            if (imagePaths.size >= 1) {
                binding.imageContainer.visibility = View.VISIBLE

                if (imagePaths.size == 1) {
                    binding.imageView1.visibility = View.VISIBLE
                    binding.imageView1.setImageURI(Uri.parse(imagePaths[0]))
                } else if (imagePaths.size >= 2) {
                    binding.imageView1.visibility = View.VISIBLE
                    binding.imageView1.setImageURI(Uri.parse(imagePaths[0]))
                    binding.imageView2.visibility = View.VISIBLE
                    binding.imageView2.setImageURI(Uri.parse(imagePaths[1]))

                }
            }
        }

        fun saveImagesToFirestoreAndStorage(userId: String, userDocId: String, imagePaths: List<String>) {
            // Verificar se todos os dados necessários estão disponíveis
            if (userId != null && userDocId != null && imagePaths != null) {
                // Acesso ao Firestore
                val db = FirebaseFirestore.getInstance()

                // Referência ao documento
                val lockerRef = db.collection("users").document(userId).collection("lockers").document(userDocId)

                // Extrair os nomes dos arquivos dos caminhos das imagens
                val imageFileNames = imagePaths.map { imagePath ->
                    val fileName = File(imagePath).name // Extrair apenas o nome do arquivo
                    // Adicionar o ID do usuário ao nome do arquivo para torná-lo único
                    "$userId-$fileName"
                }

                // Atualizar os campos de nomes de arquivos no Firestore
                lockerRef.update("imageFileNames", imageFileNames)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nomes dos arquivos salvos com sucesso no Firestore!", Toast.LENGTH_SHORT).show()
                        // Aqui você pode navegar para a próxima tela ou realizar outra ação após salvar os nomes dos arquivos no Firestore
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao salvar nomes dos arquivos no Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                // Salvar as imagens no Firebase Storage
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference

                for ((index, imagePath) in imagePaths.withIndex()) {
                    val imageFile = File(imagePath)
                    val fileName = imageFile.name // Obtém o nome do arquivo
                    val uniqueFileName = "$userId-$fileName" // Adicionar o ID do usuário ao nome do arquivo
                    val imageRef = storageRef.child("images/$uniqueFileName")

                    // Ler o arquivo de imagem como bytes
                    val fileStream = FileInputStream(imageFile)
                    val buffer = ByteArray(fileStream.available())
                    fileStream.read(buffer)
                    fileStream.close()

                    // Fazer upload dos bytes para o Cloud Storage
                    val uploadTask = imageRef.putBytes(buffer)
                    uploadTask.addOnFailureListener { exception ->
                        // Lidar com falhas no upload
                    }.addOnSuccessListener { taskSnapshot ->
                        // Lidar com sucesso no upload
                        // ...
                        if (index == imagePaths.size - 1) {
                            // Este bloco será executado apenas uma vez, após o término do upload de todas as imagens

                            // Obter userId e userDocId dos extras da Intent
                            val userId = intent.getStringExtra("userId")
                            val userDocId = intent.getStringExtra("docID")

                            // Criar uma Intent para iniciar a atividade NfcActivity
                            val intent = Intent(this, NfcActivity::class.java).apply {
                                putExtra("docID", userDocId)
                                putExtra("userId", userId)
                            }

                            // Iniciar a atividade NfcActivity
                            startActivity(intent)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Dados insuficientes para salvar as imagens.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configuração do botão de salvar
        binding.saveButton.setOnClickListener {
            if (userId != null && userDocId != null && imagePaths != null) {
                saveImagesToFirestoreAndStorage(userId, userDocId, imagePaths)
            } else {
                Toast.makeText(this, "Dados insuficientes para salvar as imagens.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
