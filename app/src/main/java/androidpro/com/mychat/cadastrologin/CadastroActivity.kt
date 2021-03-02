package androidpro.com.mychat.cadastrologin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidpro.com.mychat.R
import androidpro.com.mychat.mensagens.UltimasMensagensActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CadastroActivity : AppCompatActivity() {

    private val TAG = "CadastroActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        findViewById<Button>(R.id.button_cadastro).setOnClickListener {
            cadastrarUsuario()
        }

        findViewById<TextView>(R.id.text_pergunta_conta_cadastro).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_selecionar_imagem_cadastro).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d(TAG, "Foto selecionada")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            findViewById<ImageView>(R.id.imageview_foto_cadastro).setImageBitmap(bitmap)

            findViewById<Button>(R.id.button_selecionar_imagem_cadastro).alpha = 0f
        }
    }

    private fun cadastrarUsuario(){
        val email = findViewById<EditText>(R.id.edittext_email_cadastro).text.toString()
        val senha = findViewById<EditText>(R.id.edittext_senha_cadastro).text.toString()

        if (email.isEmpty() || senha.isEmpty()){
            Toast.makeText(this, "Há campos vazios!", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener {
                if (!it.isSuccessful)
                    return@addOnCompleteListener

                Log.d(TAG, "Cadastro feito com sucesso. UID: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d(TAG, "Falha ao criar o usuário: ${it.message}")
                Toast.makeText(this, "Falha ao criar o usuário: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage(){
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d(TAG, "Imagem armazenada no Firebase com sucesso!")

                    ref.downloadUrl.addOnSuccessListener {

                        saveUserFirebaseDatabase(it.toString())
                    }
                }
    }

    private fun saveUserFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/usuarios/$uid")

        val user = Usuario(uid, findViewById<EditText>(R.id.edittext_usuario_cadastro).text.toString(), profileImageUrl)

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(TAG, "Usuário salvo no Firebase com sucesso!")

                    val intent = Intent(this, UltimasMensagensActivity:: class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
    }
}

