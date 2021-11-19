package com.sharewithme.swm.declare

import com.sharewithme.swm.utils.FireBaseAuth
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sharewithme.swm.R
import com.sharewithme.swm.board.BoardEditActivity
import com.sharewithme.swm.databinding.ActivityDeclareInsideBinding
import com.sharewithme.swm.utils.FireBaseRef

class DeclareInsideActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDeclareInsideBinding
    private lateinit var key : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_declare_inside)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_declare_inside)

        binding.declareSettingIcon.setOnClickListener{
            showDialog()
        }

        // key값(=UID)을 받아와서 각각의 데이터를 가져오는 방법이다
        key = intent.getStringExtra("key").toString()
        getDeclareData(key)
        getImageData(key)
    }
    private fun getImageData(key : String) {

        val storageReference = Firebase.storage.reference.child("$key.png")
        val imageViewFromFB = findViewById<ImageView>(R.id.getImageArea)

        storageReference.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)
            }
            else{

            }

        }
    }
    private fun showDialog(){

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("고객센터글 수정 / 삭제")

        val alertDialog = mBuilder.show()
        alertDialog.findViewById<Button>(R.id.editBtn)?.setOnClickListener {
            val intent = Intent(this, BoardEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
            finish()
        }
        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener {
            FireBaseRef.DeclareRef.child(key).removeValue()
            finish()
        }

    }

    private fun getDeclareData(key : String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try{
                    val dataModel = dataSnapshot.getValue(DeclareModel::class.java)

                    binding.titleArea.text = dataModel!!.title
                    binding.textArea.text = dataModel!!.content

                    val myUid = FireBaseAuth.getUid()
                    val writerUid = dataModel.uid

                    //Uid가 일치할때만 수정이 가능하도록
                    if(myUid.equals(writerUid)){
                        binding.declareSettingIcon.isVisible = true
                    }
                } catch (e : Exception){
                    Log.d(TAG, "삭제완료")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FireBaseRef.DeclareRef.child(key).addValueEventListener(postListener) // uid안의 데이터를 쓰기때문에 child(key)라고 적는다
    }
}