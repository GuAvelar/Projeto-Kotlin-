package br.com.projetointegrador.keepsafe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar se o usuário está logado no Firebase
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // O usuário está logado, obter o Firestore
            val db = FirebaseFirestore.getInstance()
            val userId = user.uid

            // Buscar o documento do usuário para obter o userType
            val userDocRef = db.collection("users").document(userId)
            userDocRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userType = document.getString("userType")
                    if (userType == "admin") {
                        // Redirecionar para a tela de administrador
                        val intent = Intent(this, HomeGerenteActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Redirecionar para a tela de usuário
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    // Documento do usuário não encontrado, redirecionar para a tela de login
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                // Finalizar a atividade SplashScreen
                finish()
            }.addOnFailureListener {
                // Falha ao obter o documento do usuário, redirecionar para a tela de login
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                // Finalizar a atividade SplashScreen
                finish()
            }
        } else {
            // O usuário não está logado, redirecionar para a tela de login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Finalizar a atividade SplashScreen
            finish()
        }
    }
}
