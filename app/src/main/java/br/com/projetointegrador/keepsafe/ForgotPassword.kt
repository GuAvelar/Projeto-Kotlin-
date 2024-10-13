package br.com.projetointegrador.keepsafe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import br.com.projetointegrador.keepsafe.databinding.ActivityForgotPasswordBinding
import br.com.projetointegrador.keepsafe.databinding.ActivityMainBinding
import br.com.projetointegrador.keepsafe.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityForgotPasswordBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.confirmButtonForgotPassword?.setOnClickListener{
            val email: String = binding?.etForgotpasswordEmail?.text.toString()
            if (email.isNotEmpty()) {
                forgotPassword(email)
            } else {
                Toast.makeText(this@ForgotPassword, "Por favor, preencha os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        binding?.backLoginButton?.setOnClickListener{
            val intent = Intent(this@ForgotPassword, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun forgotPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email de recuperação de senha enviado com sucesso
                    Toast.makeText(baseContext, "Email de recuperação enviado com sucesso.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ForgotPassword, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Falha ao enviar o email de recuperação de senha
                    Toast.makeText(baseContext, "Falha ao enviar o email de recuperação.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private var TAG = "EmailAndPassword"
    }

    override fun onDestroy(){
        super.onDestroy()
        binding = null
    }
}
