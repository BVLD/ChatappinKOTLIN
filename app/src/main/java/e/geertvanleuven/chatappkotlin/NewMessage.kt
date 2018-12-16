package e.geertvanleuven.chatappkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_masseges.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_item_new_message.view.*

class NewMessage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)



        //TOOLBAR
        supportActionBar?.title = "Select user"


        val adapter = GroupAdapter<ViewHolder>()




        //RECYCLERVIEW
        RecyclerView_New_Message.adapter = adapter
        RecyclerView_New_Message.layoutManager = LinearLayoutManager(this)
        RecyclerView_New_Message.addItemDecoration(
            DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL))



        fetchUser()


    }

    companion object {

        val USER_KEY = "USER_KEY"

    }

    private fun fetchUser() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {

                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach{

                    val user = it.getValue(User::class.java)

                    if (user != null){
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatLog::class.java)
                    //intent.putExtra(USER_KEY, userItem.user.username)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()

                }

                RecyclerView_New_Message.adapter = adapter
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}

class UserItem(val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.TV_Name_Item_New_Message.text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.CI_Profile_Photo_Item_New_Message)

    }

    override fun getLayout(): Int {

        return R.layout.user_item_new_message

    }

}
