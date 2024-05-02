package com.oguz.kotlininstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.oguz.kotlininstagram.databinding.ActivityFeedBinding
import com.oguz.kotlininstagram.databinding.ActivityUploadBinding

class FeedActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFeedBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var userArrayList: ArrayList<Post>
    private lateinit var fireStore : FirebaseFirestore
    var adapter : FeedRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        fireStore = Firebase.firestore

        userArrayList = ArrayList<Post>()

        getDataFromFireStore()

        binding.recyclerView.layoutManager = LinearLayoutManager(this@FeedActivity)
        adapter = FeedRecyclerAdapter(userArrayList)
        binding.recyclerView.adapter = adapter


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.add_post){
            //upload Activity
            val intent = Intent(this@FeedActivity,UploadActivity::class.java)
            startActivity(intent)
        }else if (item.itemId == R.id.logout){

            auth.signOut()
            val intent = Intent(this@FeedActivity,MainActivity::class.java)
            startActivity(intent)
            finish()

        }
        return super.onOptionsItemSelected(item)
    }

    fun getDataFromFireStore(){

        fireStore.collection("Posts").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener{value,exception->

            if (exception != null){
                Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }else{

                if (value != null){
                    if (!value.isEmpty){

                        userArrayList.clear()

                        val documents = value.documents

                        for(document in documents){
                            val comment = document.get("comment") as String
                            val useremail = document.get("userEmail") as String
                            val downloadUrl = document.get("downloadUrl") as String

                            val post = Post(useremail,comment,downloadUrl)

                            userArrayList.add(post)

                        }


                    }
                }
            }
        }

    }
}