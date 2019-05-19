package com.kotlintask

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_button.setOnClickListener{
            val userName=user_name_edit_text.text.toString()
            val password=password_edit_text.text.toString()
            if(!userName.isEmpty()&&!password.isEmpty()){
                val intent = Intent(applicationContext, LocationActivity::class.java)
                intent.putExtra("userName",userName)
                intent.putExtra("password",password)
                startActivity(intent)
            }else{
                Toast.makeText(applicationContext,"Enter Valid Name and Password",Toast.LENGTH_SHORT).show()
            }

        }
    }
}
