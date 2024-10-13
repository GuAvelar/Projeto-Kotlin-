import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.com.projetointegrador.keepsafe.MainActivity
import br.com.projetointegrador.keepsafe.R
import br.com.projetointegrador.keepsafe.RegisterActivity
import br.com.projetointegrador.keepsafe.RegisterCardActivity
import br.com.projetointegrador.keepsafe.databinding.FragmentConfigBinding
import br.com.projetointegrador.keepsafe.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConfigFragment : Fragment() {
    private var _binding: FragmentConfigBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.progressBar.visibility = View.VISIBLE
        binding.signOutView.visibility = View.GONE
        binding.contentViewUserNotSigned.visibility = View.GONE

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.signOut.setOnClickListener{
            signOutUser();
        }

        if (auth.currentUser != null) {
            binding.signOutView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        } else {
            // Usuário não está logado, ocultar conteúdo e redirecionar para tela de login (exemplo)
            binding.signOutView.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.contentViewUserNotSigned.visibility = View.VISIBLE
        }


        return view
    }

    private fun signOutUser() {
        try {
            FirebaseAuth.getInstance().signOut();
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ocorreu um erro ao deslogar.Tente novamente!", Toast.LENGTH_SHORT).show()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
