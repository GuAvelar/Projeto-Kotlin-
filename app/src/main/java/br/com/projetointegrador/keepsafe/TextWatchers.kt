// TextWatchers.kt

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class CpfTextWatcher(private val editText: EditText) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        editText.removeTextChangedListener(this)
        val formattedCpf = formatCpf(s.toString())
        editText.setText(formattedCpf)
        editText.setSelection(formattedCpf.length)
        editText.addTextChangedListener(this)
    }

    private fun formatCpf(input: String): String {
        val numbers = input.replace("[^\\d]".toRegex(), "")
        val formatted = StringBuilder()
        for (i in numbers.indices) {
            formatted.append(numbers[i])
            if (i == 2 || i == 5) formatted.append('.')
            if (i == 8) formatted.append('-')
        }
        return formatted.toString().substring(0, minOf(formatted.length, 14)) // Limitando a 14 caracteres
    }
}

class DateTextWatcher(private val editText: EditText) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        editText.removeTextChangedListener(this)
        val formattedDate = formatDate(s.toString())
        editText.setText(formattedDate)
        editText.setSelection(formattedDate.length)
        editText.addTextChangedListener(this)
    }

    private fun formatDate(input: String): String {
        val numbers = input.replace("[^\\d]".toRegex(), "")
        val formatted = StringBuilder()
        for (i in numbers.indices) {
            formatted.append(numbers[i])
            if (i == 1 || i == 3) formatted.append('/')
        }
        return formatted.toString().substring(0, minOf(formatted.length, 10)) // Limitando a 10 caracteres
    }
}

class PhoneTextWatcher(private val editText: EditText) : TextWatcher {
    private var isFormatting: Boolean = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting) {
            return
        }

        isFormatting = true

        // Remove o listener para evitar chamadas recursivas
        editText.removeTextChangedListener(this)

        val digits = s.toString().replace("[^\\d]".toRegex(), "")

        val formattedPhone = StringBuilder()

        // Formatação do código de área
        if (digits.length >= 2) {
            formattedPhone.append("(").append(digits.substring(0, 2)).append(") ")
        } else if (digits.isNotEmpty()) {
            formattedPhone.append("(").append(digits)
        }

        // Formatação do restante do número
        if (digits.length > 2) {
            formattedPhone.append(" ").append(digits.substring(2))
        }

        // Limita o tamanho do número para o formato desejado: (00) 00000-0000
        if (formattedPhone.length > 15) {
            formattedPhone.delete(15, formattedPhone.length)
        }

        // Atualiza o texto no EditText
        editText.setText(formattedPhone.toString())
        editText.setSelection(formattedPhone.length)

        // Reativa o listener após a formatação
        editText.addTextChangedListener(this)

        isFormatting = false
    }
}
