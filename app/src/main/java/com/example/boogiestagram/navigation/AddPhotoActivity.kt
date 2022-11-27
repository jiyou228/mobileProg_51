package com.example.boogiestagram.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.example.boogiestagram.R
import com.example.boogiestagram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //Initiate 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        //Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        //startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        startForResult.launch(photoPickerIntent)


        //toolbar_enabled
        setSupportActionBar(my_toolbar)
        val actionbar = supportActionBar!!
        actionbar.setDisplayShowCustomEnabled(true)
        actionbar.setDisplayShowTitleEnabled(false)
        actionbar.setDisplayHomeAsUpEnabled(true) //뒤로가기 버튼 만들기
        //actionbar.setCustomView(R.id.my_toolbar)

        //글자수 세는 이벤트
        addphoto_edit_explain.addTextChangedListener(object: TextWatcher {
            // 입력하기 전에 호출되는 API
            @SuppressLint("SetTextI18n")
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                wordcount.text = "0 / 140"
            }
            // EditText에 변화가 있을때, 변화와 동시에 처리
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var userinput = addphoto_edit_explain.text.toString()
                wordcount.text = userinput.length.toString() + " / 140"
                if(userinput.length > 140){
                    wordcount.setTextColor(Color.RED)
                }else {
                    wordcount.setTextColor(Color.BLACK)
                }
            }
            //입력이 끝났을때 처리
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                var userinput = addphoto_edit_explain.text.toString()
                wordcount.text = userinput.length.toString() + " / 140"
            }

        })

    }

    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            photoUri = it.data?.data
            addphoto_image.setImageURI(photoUri)
        }else{
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_photo_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var id = item.itemId
        var userinput = addphoto_edit_explain.text.toString()
        when (id){
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.upload_btn ->{
                if(userinput.length <= 140){
                    contentUpload()
                }else{
                    Toast.makeText(this, "140자가 넘어갑니다",Toast.LENGTH_LONG).show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_FROM_ALBUM){
//            if(resultCode == RESULT_OK){
//                //This is path to the selected image
//                photoUri = data?.data
//                addphoto_image.setImageURI(photoUri)
//            }else{
//                finish()
//            }
//        }
//    }

    //업로드
    @SuppressLint("SimpleDateFormat")
    fun contentUpload(){
        //Make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("imagers")?.child(imageFileName)

        //Promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //Insert download of image
            contentDTO.imageUrl = uri.toString()

            //Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userId
            contentDTO.userId = auth?.currentUser?.email

            //Insert explain of content
            contentDTO.explain = addphoto_edit_explain.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis().toString()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(RESULT_OK)

            finish()
        }

        //Callback method
        /*storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

                //Insert download of image
                contentDTO.imageUrl = uri.toString()

                //Insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                //Insert userId
                contentDTO.userId = auth?.currentUser?.email

                //Insert explain of content
                contentDTO.explain = addphoto_edit_explain.text.toString()

                //Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis().toString()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()

            }
        }*/

    }
}