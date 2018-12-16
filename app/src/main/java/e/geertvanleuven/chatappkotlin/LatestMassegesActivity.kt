package e.geertvanleuven.chatappkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import e.geertvanleuven.chatappkotlin.NewMessage.Companion.USER_KEY
import kotlinx.android.synthetic.main.activity_latest_masseges.*
import kotlinx.android.synthetic.main.latest_message_row_item.view.*

class LatestMassegesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        val TAG = "LatestMessageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_masseges)


        //SET ITEM CLICL LISTENER TO YOUR ADAPTER
        adapter.setOnItemClickListener { item, view ->

            Log.d(TAG, "Thus user clicks on the on a user")
            val Intent = Intent(this, ChatLog::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(USER_KEY, row.chatPartnerUser)
            startActivity(Intent)


        }



        RecyclerView_LatestMassages.adapter = adapter
        RecyclerView_LatestMassages.addItemDecoration(DividerItemDecoration(this,
            DividerItemDecoration.VERTICAL))

        fetchCurrentUser()

        verifyUserIsLoggedIn()

        //setpDummyRows()

        listenforLatestMessages()

    }

    val adapter = GroupAdapter<ViewHolder>()

    class LatestMessageRow(val chatMessage: ChatLog.ChatMessage): Item<ViewHolder>(){

        var chatPartnerUser: User? = null

        override fun getLayout(): Int {
            return R.layout.latest_message_row_item
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.TV_Latest_Message_Message_Row_Item.text = chatMessage.text

            val chatPartnerId: String
            if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toID
            }else{
                chatPartnerId = chatMessage.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        chatPartnerUser = p0.getValue(User::class.java)
                        viewHolder.itemView.TV_Name_Message_Row_Item.text = chatPartnerUser?.username

                        val targetImageView = viewHolder.itemView.IV_Profile_Photo_Message_Row_Item
                        Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
                    }
                })
        }
    }

    val LatestmessagesMap = HashMap<String, ChatLog.ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        LatestmessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenforLatestMessages() {

        val fromId = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLog.ChatMessage::class.java) ?: return
                LatestmessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLog.ChatMessage::class.java) ?: return
                LatestmessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onCancelled(p0: DatabaseError) {

            }


        })

    }


    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
             currentUser = p0.getValue(User::class.java)
            }


    })
}


//CHECKS IF THE USER IS LOGGED IN OR NOT
private fun verifyUserIsLoggedIn() {

    val uid = FirebaseAuth.getInstance().uid
    if (uid == null) {

        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }
}

override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {

        R.id.menu_new_message -> {

            val intent = Intent(this, NewMessage::class.java)
            startActivity(intent)

        }

        R.id.menu_sign_out -> {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }
    return super.onOptionsItemSelected(item)
}


//INFLATE THE MENU IN THE TOOLBAR
override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu, menu)
    return super.onCreateOptionsMenu(menu)
}


}
