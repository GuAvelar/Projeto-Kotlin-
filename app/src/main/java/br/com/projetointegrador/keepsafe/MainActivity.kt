package br.com.projetointegrador.keepsafe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

import androidx.appcompat.app.AppCompatActivity
import br.com.projetointegrador.keepsafe.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding?.signInButton?.setOnClickListener{
            val email: String = binding?.etLoginEmail?.text.toString()
            val password: String = binding?.etLoginPassword?.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                closeKeyboard(this)
                signInWithEmailAndPassword(email, password)
            } else {
                Toast.makeText(this@MainActivity, "Por favor, preencha os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        binding?.signUpLinkButton?.setOnClickListener{
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding?.signInAnonymous?.setOnClickListener{
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
        }

        binding?.forgotPasswordButton?.setOnClickListener{
            val intent = Intent(this@MainActivity, ForgotPassword::class.java)
            startActivity(intent)
        }

    }
    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Login concluido com sucesso!")
                val user = auth.currentUser
                if (user != null) {
                    checkUserType(user.uid)
                }
            } else {
                Log.w(TAG, "Erro na hora de entrar na sua conta.", task.exception)
                Toast.makeText(baseContext, "Authentication Failure", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkUserType(userId: String) {
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                val userType = document.getString("userType")
                if (userType == "admin") {
                    // Redirect to HomeGerenteActivity for admin users
                    val intent = Intent(this@MainActivity, HomeGerenteActivity::class.java)
                    startActivity(intent)
                } else {
                    // Redirect to HomeActivity for regular users
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                }
                finish()
            } else {
                Log.w(TAG, "Documento do usuário não encontrado.")
                Toast.makeText(baseContext, "Erro ao obter informações do usuário.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Erro ao acessar o Firestore.", exception)
            Toast.makeText(baseContext, "Erro ao acessar o Firestore.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private var TAG = "EmailAndPassword"
    }

    override fun onDestroy(){
        super.onDestroy()
        binding = null
    }

    fun closeKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}
