package br.com.projetointegrador.keepsafe


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.projetointegrador.keepsafe.databinding.ActivityHomeGerenteBinding

class HomeGerenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeGerenteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeGerenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar o clique do botão Gerenciar Locações
        binding.manageRentalsButton.setOnClickListener {
            // Implementar lógica para gerenciar locações
            val intent = Intent(this@HomeGerenteActivity, QrCodeScannerActivity::class.java)
            startActivity(intent)
        }

        // Configurar o clique do botão Criar Nova Locação
        binding.createNewRentalButton.setOnClickListener {
            val intent = Intent(this, NfcReadActivity::class.java) // Substitua pelo nome da sua activity
            startActivity(intent)
        }
    }
}
