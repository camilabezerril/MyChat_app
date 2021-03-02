package androidpro.com.mychat.mensagens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidpro.com.mychat.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import androidpro.com.mychat.cadastrologin.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_de_item_chat_log.view.*
import kotlinx.android.synthetic.main.chat_para_item_chat_log.view.*


class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    var usuario: Usuario?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat.adapter = adapter

        usuario = intent.getParcelableExtra(NovaMensagemActivity.KEY_USUARIO)
        supportActionBar?.title = usuario?.nome

        mensagensListener()

        button_enviar_mensagem_chat.setOnClickListener {
            enviarMensagem()
        }

    }

    private fun mensagensListener(){
        val deId = FirebaseAuth.getInstance().uid
        val paraId = usuario?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/usuario-mensagens/$deId/$paraId")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val mensagemChat = snapshot.getValue(MensagemChat:: class.java)

                if (mensagemChat != null) {
                    if (mensagemChat.deId == FirebaseAuth.getInstance().uid) {
                        val usuarioAtual = UltimasMensagensActivity.usuarioAtual
                        adapter.add(ChatParaItem(mensagemChat.text, usuarioAtual!!))
                    } else {
                        adapter.add(ChatDeItem(mensagemChat.text, usuario!!))
                    }
                }

                recyclerview_chat.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    class MensagemChat (val id: String, val deId: String, val paraId: String, val text: String, val tempo: Long) {
        constructor() : this("","","", "",-1)
    }

    private fun enviarMensagem(){
        val text = edittext_digite_uma_mensagem_chat.text.toString()
        edittext_digite_uma_mensagem_chat.text.clear() //reiniciar edit text

        val deId = FirebaseAuth.getInstance().uid
        val paraId = usuario?.uid

        if (deId == null) return

        val deRef = FirebaseDatabase.getInstance().getReference("/usuario-mensagens/$deId/$paraId").push()
        val paraRef = FirebaseDatabase.getInstance().getReference("/usuario-mensagens/$paraId/$deId").push()

        val mensagemChat = MensagemChat(deRef.key!!, deId, paraId!!, text, System.currentTimeMillis()/1000)
        deRef.setValue(mensagemChat)
                .addOnSuccessListener {
                    Log.d("ChatLog", "Mensagem salva no Firebase com sucesso! id: ${deRef.key}")
                    recyclerview_chat.scrollToPosition(adapter.itemCount - 1)
                }
        paraRef.setValue(mensagemChat)

        val ultimaMsgRefDe = FirebaseDatabase.getInstance().getReference("/ultimas-mensagens/$deId/$paraId")
        ultimaMsgRefDe.setValue(mensagemChat)

        val ultimaMsgRefPara = FirebaseDatabase.getInstance().getReference("/ultimas-mensagens/$paraId/$deId")
        ultimaMsgRefPara.setValue(mensagemChat)
    }
}

class ChatDeItem (val text : String, val usuario: Usuario): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_mensagem_chat_de_item_chat_log.text = text
        Picasso.get().load(usuario.imagemPerfilUrl).into(viewHolder.itemView.imageview_chat_de_item_chat_log)
    }

    override fun getLayout(): Int {
        return R.layout.chat_de_item_chat_log
    }
}

class ChatParaItem (val text : String, val usuario: Usuario): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_mensagem_chat_para_item_chat_log.text = text
        Picasso.get().load(usuario.imagemPerfilUrl).into(viewHolder.itemView.imageview_chat_para_item_chat_log)
    }

    override fun getLayout(): Int {
        return R.layout.chat_para_item_chat_log
    }
}