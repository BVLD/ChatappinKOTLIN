package e.geertvanleuven.chatappkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row_item.view.*
import kotlinx.android.synthetic.main.chat_to_row_item.view.*

class ChatLog : AppCompatActivity() {

    companion object {

        val TAG = "CHATLOG"

    }

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        RecyclerView_ChatLog.adapter = adapter


        //TOOLBAR
        toUser = intent.getParcelableExtra<User>(NewMessage.USER_KEY)

        supportActionBar?.title = toUser?.username


        //SETONCLICKLISTENERS
        Btn_Send_Chat_Log.setOnClickListener {
            Log.d(TAG, "Attempt to send massage")
            performSendMessage()

        }

        listenForMessages()


    }

    private fun listenForMessages() {

        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)


                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser =
                            LatestMassegesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }

                RecyclerView_ChatLog.scrollToPosition(adapter.itemCount - 1)
            }


            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }


    class ChatMessage(
        val id: String, val text: String, val fromId: String, val toID: String,
        val timestamp: Long
    ) {

        constructor() : this("", "", "", "", -1)

    }

    //SEND A MESSAFE FUNCTION
    private fun performSendMessage() {

        val user = intent.getParcelableExtra<User>(NewMessage.USER_KEY)


        //GET THE STRING
        val text = ET_Enter_Message_Chat_Log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user.uid

        if (fromId == null) return


        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
            .push()


        val toreference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId")
            .push()


        val chatMessage = ChatMessage(
            reference.key!!,
            text,
            fromId!!,
            toId,
            System.currentTimeMillis() / 1000
        )

        reference.setValue(chatMessage)
            .addOnSuccessListener {

                Log.d(TAG, "Saved our chat message ${reference.key}")
                ET_Enter_Message_Chat_Log.text.clear()
                RecyclerView_ChatLog.scrollToPosition(adapter.itemCount - 1)

            }

        toreference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$fromId/$toId")

        latestMessageRef.setValue(chatMessage)


        val TolatestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$toId/$fromId")

        TolatestMessageRef.setValue(chatMessage)


    }
}

class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.TV_Massage_From_Row_Item.text = text

        //LOAD THE USER IMAGE
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.IV_Profile_Photo_From_Row_Item
        Picasso.get().load(uri).into(targetImageView)


    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row_item
    }

}


class ChatToItem(val text: String, val toUser: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.TV_Massage_To_Row_Item.text = text

        //LOAD THE USER IMAGE
        val uri = toUser.profileImageUrl
        val targetImageView = viewHolder.itemView.IV_Profile_Photo_To_Row_Item
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row_item
    }
}
