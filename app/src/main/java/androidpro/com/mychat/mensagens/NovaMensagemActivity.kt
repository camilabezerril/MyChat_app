package androidpro.com.mychat.mensagens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidpro.com.mychat.R
import androidpro.com.mychat.cadastrologin.Usuario
import androidpro.com.mychat.mensagens.ChatLogActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class NovaMensagemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nova_mensagem)

        supportActionBar?.title = "Selecione um usuário"

        atualizarListaUsuarios()
    }

    companion object {
        val KEY_USUARIO = "KEY_USUARIO"
    }

    private fun atualizarListaUsuarios(){
        val ref = FirebaseDatabase.getInstance().getReference("/usuarios")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_nova_mensagem)

                snapshot.children.forEach {
                    val usuario = it.getValue(Usuario:: class.java)

                    if(usuario != null)
                        adapter.add(UserItem(usuario))
                }

                adapter.setOnItemClickListener { item, view ->
                    val usuarioItem = item as UserItem

                    val intent = Intent(view.context, ChatLogActivity:: class.java)
                    intent.putExtra(KEY_USUARIO, usuarioItem.usuario)
                    startActivity(intent)

                    finish()
                }

                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

class UserItem(val usuario : Usuario) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_usuario_lista_nova_mensagem).text = usuario.nome

        Picasso.get().load(usuario.imagemPerfilUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.circle_image_usuario_lista_nova_mensagem))

    }

    override fun getLayout(): Int {
        return R.layout.usuario_lista_nova_mensagem
    }
}

