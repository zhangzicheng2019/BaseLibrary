package com.zzc.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.base.BaseApplication

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val topAct = BaseApplication.getApplication().topActivity
    }

}
