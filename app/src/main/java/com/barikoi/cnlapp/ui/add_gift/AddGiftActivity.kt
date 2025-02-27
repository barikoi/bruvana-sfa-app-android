package com.barikoi.cnlapp.ui.add_gift

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.databinding.ActivityAddGiftBinding
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AddGiftActivity : BaseActivity() {
    private lateinit var binding: ActivityAddGiftBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    var selectedCategory: String = ""
    var selectedCategories: MutableList<GiftDataModel> = mutableListOf()

    private lateinit var adapterGift: AdapterGift


    private var imageFilePath: File? = null
    private val position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddGiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.tvTitle.text = getString(R.string.add_gift)

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        adapterGift = AdapterGift(
            addImageClickListener = {
                openCameraForApplicant()
            },
            removeImageClickListener = {

            },
            removeItemClickListener = {
                selectedCategories.removeAt(it)
                adapterGift.updateList(selectedCategories)
            }
        )

        binding.rcvGift.layoutManager = LinearLayoutManager(this)
        binding.rcvGift.adapter = adapterGift


        binding.rgCat.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbGift -> {
                    initSpinner(resources.getStringArray(R.array.gift_category).toList())
                }

                R.id.rbMerchandise -> {
                    initSpinner(resources.getStringArray(R.array.merchandise_category).toList())
                }

                R.id.rbPOSMGift -> {
                    initSpinner(resources.getStringArray(R.array.posm_gift_category).toList())
                }

            }
        }

        binding.rgCat.check(R.id.rbGift)

        binding.btnAdd.setHapticClickListener {
            selectedCategories.add(
                GiftDataModel(
                    id = 0,
                    name = selectedCategory,
                    images = emptyList()
                )
            )

            adapterGift.updateList(selectedCategories)
        }

    }

    private fun openCameraForApplicant() {
        val pictureIntent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )

        if (pictureIntent.resolveActivity(packageManager) != null) {
            try {
                imageFilePath = createImageFile()
            } catch (e: IOException) {
                AppLogger.log("openCamera:: $e")
            }
            if (imageFilePath != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    imageFilePath!!
                )
                AppLogger.log("openCamera:: $photoURI")
                pictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    photoURI
                )
                startCamera.launch(pictureIntent)
            }
        }
    }

    private var startCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            AppLogger.log("startCameraNew:: $imageFilePath")
            AppLogger.log("startCameraNew:: ${result.data}")

            AppLogger.log("IMAGE_PICKER:: ${sharePrefUtils.getString(Constants.IMAGE_PICKER)}")

//            imageFiles.add(imageFilePath!!.path)

        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp =
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        return image
    }


    private fun initSpinner(data: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, data)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = data[position]
                    toast("Selected item: $selectedItem")
                    selectedCategory = selectedItem
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.btnAdd.setHapticClickListener {
            toast("Added")
        }
    }
}