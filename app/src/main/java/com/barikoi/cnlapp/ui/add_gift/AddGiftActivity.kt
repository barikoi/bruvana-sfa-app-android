package com.barikoi.cnlapp.ui.add_gift

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.adapter.AdapterImagePickerView
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.ActivityAddGiftBinding
import com.barikoi.cnlapp.databinding.DialogConfirmGiftBinding
import com.barikoi.cnlapp.databinding.DialogConfirmOrderBinding
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AddGiftActivity : BaseActivity() {
    private lateinit var binding: ActivityAddGiftBinding

    private val viewModel: AddGiftViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private lateinit var adapterImage: AdapterImagePickerView


    var selectedCategory: String = ""
    var selectedCategories: MutableList<GiftDataModel> = mutableListOf()

    private lateinit var adapterGift: AdapterGift


    private var imageFilePath: File? = null
    private val position = 0


    private var imageFiles: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddGiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startGetGiftObserve()

        binding.toolbar.tvTitle.text = getString(R.string.add_gift)

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvShopName.text = intent.getStringExtra("shop_name")

        viewModel.getGifts()


        adapterImage = AdapterImagePickerView {
            imageFiles.removeAt(it)
            adapterImage.updateImages(imageFiles)
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

        binding.btnSubmit.setHapticClickListener {
            confirmAddGiftDialog()
        }

    }

    private fun startGetGiftObserve() {
        lifecycleScope.launch {
            viewModel.routeResponse.observe(this@AddGiftActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startGetGiftObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startGetGiftObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("startGetGiftObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startGetGiftObserve:: Success ${it.data}")

                    }
                }
            }
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

            imageFiles.add(imageFilePath!!.path)

            confirmAddGiftDialog()

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

    private fun confirmAddGiftDialog() {
        var count = 1
        val dialogBinding = DialogConfirmGiftBinding.inflate(LayoutInflater.from(this))

        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        if (imageFiles.isEmpty()) {
            dialogBinding.llImagePickerView.rvImage.visibility = View.GONE
        } else {
            dialogBinding.llImagePickerView.rvImage.visibility = View.VISIBLE
        }

        dialogBinding.llImagePickerView.ivPicImage.setOnClickListener {
            dialog.dismiss()
            openCameraForApplicant()
        }


        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        dialogBinding.llImagePickerView.rvImage.adapter = adapterImage
        dialogBinding.llImagePickerView.rvImage.layoutManager = layoutManager
        adapterImage.updateImages(imageFiles)


        dialogBinding.btnConfirm.setOnClickListener {

        }
        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnPlus.setHapticClickListener {
            count++
            dialogBinding.tvCount.setText(count.toString())
        }

        dialogBinding.btnMinus.setHapticClickListener {

            if (count > 1) {
                count--
            }

            dialogBinding.tvCount.setText(count.toString())
        }


        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

}