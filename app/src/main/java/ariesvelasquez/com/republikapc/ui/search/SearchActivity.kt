package ariesvelasquez.com.republikapc.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.core.content.ContextCompat
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.utils.extensions.onQuerySubmit
import kotlinx.android.synthetic.main.search_toolbar.*
import timber.log.Timber


class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Focus to search edit text
        initSearchView()
        initOnClicks()
    }

    private fun initSearchView() {
        val searchViewEditText = editTextSearch.findViewById<EditText>(
            resources.getIdentifier("android:id/search_src_text", null, null))
        searchViewEditText.setTextColor((ContextCompat.getColor(this, R.color.text_helper_dark)))
        searchViewEditText.setHintTextColor((ContextCompat.getColor(this, R.color.colorLightGray)))

        editTextSearch.requestFocus()
        editTextSearch.onQuerySubmit {
            // Handle Submit change
            Timber.e("asdfadsf $it")
        }
    }

    private fun initOnClicks() {
        backButton.setOnClickListener { onBackPressed() }
    }
}
