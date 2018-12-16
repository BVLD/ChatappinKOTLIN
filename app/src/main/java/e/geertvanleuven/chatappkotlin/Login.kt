package e.geertvanleuven.chatappkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import e.geertvanleuven.chatappkotlin.Register
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)





        //SETONCLICKLISTENERS
        Btn_Login_Login.setOnClickListener {

           performLogin()

        }

        TV_Register_Login.setOnClickListener {

            val intent = Intent(this, Register::class.java)
            startActivity(intent)

        }
    }




    //LOGIN PART
    private fun performLogin() {

        val email = ET_Email_Login.text.toString()
        val password = ET_Password_Login.text.toString()

        if (email.isEmpty() || password.isEmpty()){

            Toast.makeText(this, "Please fill in al the fields", Toast.LENGTH_LONG).show()
        }


        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                val intent = Intent(this, LatestMassegesActivity::class.java)
                startActivity(intent)


            }.addOnFailureListener {

                Toast.makeText(this, it.message.toString(), Toast.LENGTH_LONG).show()

            }


    }
}
