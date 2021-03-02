package androidpro.com.mychat.mensagens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidpro.com.mychat.R
import androidpro.com.mychat.cadastrologin.CadastroActivity
import androidpro.com.mychat.cadastrologin.Usuario
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_ultimas_mensagens.*
import kotlinx.android.synthetic.main.usuario_lista_ultimas_mensagens.view.*

class UltimasMensagensActivity : AppCompatActivity() {

    companion object {
        var usuarioAtual: Usuario? = null
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ultimas_mensagens)

        recyclerview_ultimas_mensagens.adapter = adapter
        recyclerview_ultimas_mensagens.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)

            val itemMsg = item as ItemUltimasMensagens

            intent.putExtra(NovaMensagemActivity.KEY_USUARIO, itemMsg.chatUsuarioPara)
            startActivity(intent)
        }


        ultimasMensagensListener()
        atualizarUsuarioAtual()
        verificarLogin()
    }

    class ItemUltimasMensagens (val mensagem: ChatLogActivity.MensagemChat) : Item<GroupieViewHolder>(){
        var chatUsuarioPara: Usuario? = null

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.ultima_mensagem_usuario_lista_ultimas_mensagens.text = mensagem.text

            val chatUsuarioId: String = if (mensagem.deId == FirebaseAuth.getInstance().uid)
                mensagem.paraId
            else
                mensagem.deId

            val ref = FirebaseDatabase.getInstance().getReference("/usuarios/$chatUsuarioId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatUsuarioPara = snapshot.getValue(Usuario::class.java)
                    viewHolder.itemView.textview_nome_usuario_lista_ultimas_mensagens.text = chatUsuarioPara?.nome

                    Picasso.get().load(chatUsuarioPara?.imagemPerfilUrl).into(viewHolder.itemView.imageview_usuario_lista_ultimas_mensagens)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        override fun getLayout(): Int {
            return R.layout.usuario_lista_ultimas_mensagens
        }
    }

    val ultimasMensagensMap = HashMap<String, ChatLogActivity.MensagemChat>()

    private fun refreshRecyclerView(){
        adapter.clear()
        ultimasMensagensMap.values.forEach{
            adapter.add(ItemUltimasMensagens(it))
        }
    }

    private fun ultimasMensagensListener(){
        val deId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/ultimas-mensagens/$deId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val mensagem = snapshot.getValue(ChatLogActivity.MensagemChat::class.java) ?: return

                ultimasMensagensMap[snapshot.key!!] = mensagem
                refreshRecyclerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val mensagem = snapshot.getValue(ChatLogActivity.MensagemChat::class.java) ?: return

                ultimasMensagensMap[snapshot.key!!] = mensagem
                refreshRecyclerView()
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun atualizarUsuarioAtual(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/usuarios/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuarioAtual = snapshot.getValue(Usuario::class.java)
                Log.d("UltimasMensagens", "Usuario atual: ${usuarioAtual?.nome}")
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun verificarLogin(){
        val uid = FirebaseAuth.getInstance().uid

        if (uid == null){
            val intent = Intent(this, CadastroActivity:: class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_nova_mensagem -> {
                val intent = Intent(this, NovaMensagemActivity:: class.java)
                startActivity(intent)
            }

            R.id.menu_sair -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, CadastroActivity:: class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}