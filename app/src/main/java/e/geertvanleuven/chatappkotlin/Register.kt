package e.geertvanleuven.chatappkotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.register.*
import java.util.*

class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)






       //SET ON CLICK LISTENER


        //GO TO LOGIN ACTIVITY
        TV_Log_In_Main.setOnClickListener {

            val intent = Intent(this, Login::class.java)
            startActivity(intent)

        }



        //REGISTER USER
        Btn_Register_Main.setOnClickListener {

            performRegister()

        }



        //PROFILE PHOTO
        Btn_Profile_Photo_Main.setOnClickListener {

            Log.d("register", "Try to show photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }


    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){

            Log.d("register", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            IV_Profile_Photo_Register.setImageBitmap(bitmap)

            Btn_Profile_Photo_Main.alpha = 0f
            //val bitMapDrawable = BitmapDrawable(bitmap)
            //Btn_Profile_Photo_Main.setBackgroundDrawable(bitMapDrawable)

        }

    }



    //REGISTER PART

    private fun performRegister() {


        //EMAIL AND PASSWORD
        var email = ET_Email_Main.text.toString()
        var password = ET_Password_Main.text.toString()

        if (email.isEmpty() || password.isEmpty()){

            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_LONG).show()
            return
        }


        //REGISTER PART
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener


                //UPLOAD THE PROFILE PHOTO TO FIREBASE STORAGE
                uploadImageToFirebaseStorage()
            }.addOnFailureListener {

                Toast.makeText(this, it.message.toString(), Toast.LENGTH_LONG).show()

            }


    }



    //UPLOAD THE PROFILE PHOTO TO FIREBASE STORAGE
    private fun uploadImageToFirebaseStorage() {

        if (selectedPhotoUri == null) return


        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {

                Log.d("register", "Succesfully uploaded image ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {



                    saveUserToFirebaseDatabase(it.toString())

                }

            }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {

        val uid = FirebaseAuth.getInstance().uid ?: ""

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")


        val user = User(uid, ET_UserName_Main.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {

                Log.d("register", "Finally we save the user to firebase database")


                val intent = Intent(this, LatestMassegesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
    }
}

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String): Parcelable{
    constructor() : this("", "", "")

}
