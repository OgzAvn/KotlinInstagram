package com.oguz.kotlininstagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.AppLocalesStorageHelper
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.oguz.kotlininstagram.databinding.ActivityFeedBinding
import com.oguz.kotlininstagram.databinding.ActivityMainBinding
import com.oguz.kotlininstagram.databinding.ActivityUploadBinding
import java.io.IOException
import java.util.UUID

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var activityresultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var storage : FirebaseStorage

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

    }

    fun uploadClicked(view : View){

        //Herşeyden önce STORAGE a görselimizi kaydedeceğiz oradan URL yi alıp gelip FireStora da document in altına koyacağız.

        //Univeral unique id
        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val reference = storage.reference
        val imageReference = reference.child("images").child(imageName)

        if (selectedPicture != null){

            imageReference.putFile(selectedPicture!!).addOnSuccessListener {task->

                //Download URL yi olacağız burada sonrasında Firestore a kaydedeceğiz.

                val uploadPictureReference = reference.child("images").child(imageName)

                uploadPictureReference.downloadUrl.addOnSuccessListener {

                    val downloadUrl = it.toString()

                    //Buraya kadar yukarıda dediğim storage dan URL yi aldık şimdi firestore a koyacağız

                    val postMap = hashMapOf<String , Any>()

                    postMap.put("downloadUrl",downloadUrl)
                    postMap.put("userEmail",auth.currentUser!!.email.toString())
                    postMap.put("comment",binding.uploadCommentText.text.toString())
                    postMap.put("date",Timestamp.now())

                    firestore.collection("Posts").add(postMap).addOnCompleteListener(this){documentReference ->

                        if (documentReference.isSuccessful && documentReference.isComplete){
                            finish()
                        }

                    }.addOnFailureListener(this){

                        Toast.makeText(this@UploadActivity,it.localizedMessage,Toast.LENGTH_LONG).show()
                    }

                }

            }.addOnFailureListener(this){exception->

                Toast.makeText(this,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun imageViewClicked(view : View){
        if (ContextCompat.checkSelfPermission(this@UploadActivity,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                //request
                Snackbar.make(view,"Permission needed to go to Gallery",Snackbar.LENGTH_INDEFINITE).setAction("Gibe Permission",View.OnClickListener {

                    //Request

                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }).show()
            }
            else{
                //request

                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }else{

            //Permission Granted
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityresultLauncher.launch(intentToGallery)


        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun registerLauncher(){

        activityresultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->

            if(result.resultCode == RESULT_OK){
                val intentFromresult = result.data

                if (intentFromresult != null){
                    selectedPicture = intentFromresult.data

                    try {
                        val source = ImageDecoder.createSource(
                            this@UploadActivity.contentResolver,
                            selectedPicture!!
                        )

                        selectedBitmap = ImageDecoder.decodeBitmap(source)
                        binding.uploadImageView.setImageBitmap(selectedBitmap)

                    }catch (e : IOException){
                        e.printStackTrace()
                    }
                }
            }

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result->

            if (result){
                //Permission Granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityresultLauncher.launch(intentToGallery)

            }else{
                //Permission denied
                Toast.makeText(this@UploadActivity, "Permisson needed!", Toast.LENGTH_LONG).show()
            }
        }

    }


}