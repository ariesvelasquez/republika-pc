package ariesvelasquez.com.republikapc.ui.create.rig

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import ariesvelasquez.com.republikapc.R
import kotlinx.android.synthetic.main.activity_create_rig.*
import kotlinx.android.synthetic.main.toolbar.*

class CreateRigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_rig)

        initOnClicks()
    }

    private fun initOnClicks() {
        backButton.setOnClickListener { onBackPressed() }
        buttonCreateRig.setOnClickListener { validate() }
    }

    private fun validate() {
        // Check if Rig Name is supplied.
        if (editTextRigName.text.toString().isEmpty()) {
            Toast.makeText(this, "Choose a name first", Toast.LENGTH_SHORT).show()
            return
        }
    }
}
