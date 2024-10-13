package br.com.projetointegrador.keepsafe

import android.content.Intent
import br.com.projetointegrador.keepsafe.databinding.ActivityRegisterCardBinding
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterCardActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityRegisterCardBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterCardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.btnRegisterCard?.setOnClickListener{
            val cardNumber: String = binding?.etCardNumber?.text.toString()
            val cvv: String = binding?.etCVV?.text.toString()
            val expirationDate: String = binding?.etExpirationDate?.text.toString()
            val name: String = binding?.etFullName?.text.toString()

            if(cardNumber.isNotEmpty() && cvv.isNotEmpty() && expirationDate.isNotEmpty() && name.isNotEmpty()){
                closeKeyboard(this)
                createCard(cardNumber,cvv,expirationDate,name)
            } else {
                Toast.makeText(this@RegisterCardActivity, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun createCard(cardNumber: String, cvv: String, expirationDate: String, name: String) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            val cardData = hashMapOf(
                "cardNumber" to cardNumber,
                "cvv" to cvv,
                "expirationDate" to expirationDate,
                "name" to name
            )

            db.collection("cardCreditUsers")
                .document(userId)
                .set(cardData)
                .addOnSuccessListener {
                    val intent = Intent(this@RegisterCardActivity, HomeActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(baseContext, "Cartão registrado com sucesso.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(baseContext, "Erro ao adicionar cartão: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(baseContext, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    fun closeKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    companion object {
        private var TAG = "CreditCard"
    }

    override fun onDestroy(){
        super.onDestroy()
        binding = null
    }
}
