package br.com.projetointegrador.keepsafe
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.projetointegrador.keepsafe.databinding.ActivityNfcBinding
import br.com.projetointegrador.keepsafe.databinding.ActivityNfcReadBinding

class NfcReadActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var binding: ActivityNfcReadBinding
    private lateinit var nfcAdapter: NfcAdapter
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNfcReadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o adaptador NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Cria um PendingIntent para lidar com a detecção de NFC
        pendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onResume() {
        super.onResume()
        // Habilita a detecção de NFC com o PendingIntent
        nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A, null)
    }

    override fun onPause() {
        super.onPause()
        // Desabilita a detecção de NFC
        nfcAdapter.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        tag?.let {
            val data = readTag(it)
        }
    }

    private fun readTag(tag: Tag): Pair<String, String> {
        val ndef = Ndef.get(tag)
        ndef?.let {
            it.connect()
            val ndefMessage = it.ndefMessage
            val payload = ndefMessage.records[0].payload
            it.close()
            val payloadString = String(payload)

            // Extrair o userId e o userDocId da mensagem
            val userIdStartIndex = payloadString.indexOf("userId:") + "userId:".length
            val userIdEndIndex = payloadString.indexOf(";userDocId:")
            val userId = payloadString.substring(userIdStartIndex, userIdEndIndex)

            val userDocIdStartIndex = userIdEndIndex + ";userDocId:".length
            val userDocId = payloadString.substring(userDocIdStartIndex)

            val intent = Intent(this, FinishLockerActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("userDocId", userDocId)
            startActivity(intent)

            return Pair(userId, userDocId)
        }
        return Pair("", "")
    }

    private fun showDataDialog(data: Pair<String, String>) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Dados NFC")
            builder.setMessage("Dados lidos da tag NFC:\n$data")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
}
