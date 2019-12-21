package com.winds.smartimagepicker.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.winds.smartimagepicker.R

class ActivitySecond : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        supportFragmentManager.beginTransaction().replace(R.id.fram,ImagePickerFragment()).commit()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragments = supportFragmentManager.fragments
        if (fragments != null) {
            for (f in fragments) {
                (f as? ImagePickerFragment)?.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
