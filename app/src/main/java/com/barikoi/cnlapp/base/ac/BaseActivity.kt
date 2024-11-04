package com.barikoi.cnlapp.base.ac

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.let { DefaultLocaleHelper.getInstance(it).onAttach() }
        )
    }
}