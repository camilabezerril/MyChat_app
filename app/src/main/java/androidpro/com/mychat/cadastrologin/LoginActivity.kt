package androidpro.com.mychat.cadastrologin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidpro.com.mychat.R
import androidpro.com.mychat.mensagens.UltimasMensagensActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.button_login).setOnClickListener {
            val email = findViewById<EditText>(R.id.edittext_email_login).text.toString()
            val senha = findViewById<EditText>(R.id.edittext_senha_login).text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener {
                    val intent = Intent(this, UltimasMensagensActivity::class.java)
                    startActivity(intent)
                }
        }

        findViewById<TextView>(R.id.textview_voltar_login).setOnClickListener {
            finish()
        }
    }
}