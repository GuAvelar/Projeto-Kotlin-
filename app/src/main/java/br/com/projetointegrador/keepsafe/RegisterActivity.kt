package br.com.projetointegrador.keepsafe


import CpfTextWatcher
import DateTextWatcher
import PhoneTextWatcher
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import br.com.projetointegrador.keepsafe.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityRegisterBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.etRegisterCpf?.addTextChangedListener(CpfTextWatcher(binding?.etRegisterCpf!!))
        binding?.etRegisterPhone?.addTextChangedListener(PhoneTextWatcher(binding?.etRegisterPhone!!))
        binding?.etRegisterBirthday?.addTextChangedListener(DateTextWatcher(binding?.etRegisterBirthday!!))



        auth = Firebase.auth

        binding?.signUpButton?.setOnClickListener{
            val name: String = binding?.etRegisterName?.text.toString()
            val cpf: String = binding?.etRegisterCpf?.text.toString()
            val phone: String = binding?.etRegisterPhone?.text.toString()
            val birthday: String = binding?.etRegisterBirthday?.text.toString()
            val email: String = binding?.etRegisterEmail?.text.toString()
            val password: String = binding?.etRegisterPassword?.text.toString()
            val confirm_password = binding?.etRegisterPasswordConfirm?.text.toString()


            if(email.isNotEmpty() && password.isNotEmpty() && confirm_password.isNotEmpty() && name.isNotEmpty() && cpf.isNotEmpty() && phone.isNotEmpty() && birthday.isNotEmpty()){
                if(password == confirm_password){
                    closeKeyboard(this)
                    createUser(name,cpf,phone,birthday,email,password)
                } else {
                    Toast.makeText(this@RegisterActivity, "Sua senha esta imcopativel.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@RegisterActivity, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        binding?.signInLinkButton?.setOnClickListener{
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createUser(name: String, cpf: String, phone: String, birthday: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(name) // Define o nome do usuário
                    .build()

                user?.updateProfile(userProfileChangeRequest)?.addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        // Criação do perfil do usuário bem-sucedida
                        Log.d(TAG, "Perfil do usuário atualizado com sucesso!")
                        Toast.makeText(baseContext, "Perfil do usuário atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        // Salvar outras informações do usuário no Firestore
                        val newUser = hashMapOf(
                            "name" to name,
                            "cpf" to cpf,
                            "phone" to phone,
                            "birthday" to birthday,
                            "email" to email,
                            "userType" to "user"
                        )
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(user.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                Log.d(TAG, "Informações adicionais do usuário salvas com sucesso!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Erro ao salvar informações adicionais do usuário", e)
                            }
                        // Navegar para a tela principal após o registro bem-sucedido
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Falha ao atualizar o perfil do usuário
                        Log.w(TAG, "Erro ao atualizar o perfil do usuário.", updateTask.exception)
                        Toast.makeText(baseContext, "Falha ao atualizar o perfil do usuário", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Falha na criação de usuário
                Log.w(TAG, "Erro ao criar conta.", task.exception)
                Toast.makeText(baseContext, "Falha ao criar conta", Toast.LENGTH_SHORT).show()
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

    fun closeKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}
