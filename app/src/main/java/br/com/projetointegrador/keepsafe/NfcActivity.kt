package br.com.projetointegrador.keepsafe
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.com.projetointegrador.keepsafe.databinding.ActivityNfcBinding
import com.google.android.material.snackbar.Snackbar
import java.io.IOException

class NfcActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var binding: ActivityNfcBinding
    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        // Enable NFC reader mode
        nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A, null)
        // Update NFC status
        updateNfcStatus("Aguardando operação NFC...")
    }

    override fun onPause() {
        super.onPause()
        // Disable NFC reader mode
        nfcAdapter.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        tag?.let {
            val userId = intent.getStringExtra("userId")
            val userDocId = intent.getStringExtra("docID")

            val dataToSave = "userId:$userId;userDocId:$userDocId"
            writeTag(it, dataToSave)
        }
    }

    private fun writeTag(tag: Tag, data: String) {
        val ndefRecord = NdefRecord.createTextRecord("en", data)
        val ndefMessage = NdefMessage(arrayOf(ndefRecord))

        try {
            val ndef = Ndef.get(tag)
            ndef?.let {
                it.connect()
                it.writeNdefMessage(ndefMessage)
                it.close()

                // Show success Snackbar
                showSnackbar("Dados gravados na tag NFC com sucesso. Remova sua pulseira de perto")

                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, HomeGerenteActivity::class.java)
                    startActivity(intent)
                }, 3000)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Show error Snackbar
            showSnackbar("Erro ao gravar dados na tag NFC.")
        } catch (e: Exception) {
            e.printStackTrace()
            // Show unexpected error Snackbar
            showSnackbar("Erro inesperado ao gravar dados na tag NFC.")
        }
    }

    private fun updateNfcStatus(status: String) {
        binding.textNfcStatus.text = "Status NFC: $status"
    }

    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackbar.show()
    }
}
