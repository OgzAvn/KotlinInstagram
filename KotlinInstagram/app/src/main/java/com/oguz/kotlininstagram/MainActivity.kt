package com.oguz.kotlininstagram

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.oguz.kotlininstagram.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth


        val currentUser = auth.currentUser
        if (currentUser != null){
            val intent = Intent(this@MainActivity,FeedActivity::class.java)
            startActivity(intent)
            finish() // finish yapabilirim çünkü kullanıcının bir daha buraya dönmesine gerek kalmayacak
        }



    }

    fun signInClicked(view : View){

        val useremail = binding.userEmailText.text.toString()
        val password = binding.passwordText.text.toString()

        if (!useremail.equals("") && !password.equals("")){

            auth.signInWithEmailAndPassword(useremail,password).addOnCompleteListener(this){task->

                if (task.isSuccessful){


                    Toast.makeText(this,"Welcome : ${auth.currentUser?.email.toString()}",Toast.LENGTH_LONG).show()

                    val intent = Intent(this@MainActivity,FeedActivity::class.java)
                    startActivity(intent)
                    finish()

                }

            }.addOnFailureListener(this){exception->
                Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }

    fun signUpClicked(view : View){

        val useremail = binding.userEmailText.text.toString()
        val password = binding.passwordText.text.toString()

        if (!useremail.equals("") && !password.equals("")){

            auth.createUserWithEmailAndPassword(useremail,password).addOnCompleteListener(this){task ->

                if (task.isSuccessful){

                    Log.d(TAG, "createUserWithEmail:success")

                    val intent = Intent(applicationContext,FeedActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener(this){exception ->

                Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()

            }

        }

    }
}