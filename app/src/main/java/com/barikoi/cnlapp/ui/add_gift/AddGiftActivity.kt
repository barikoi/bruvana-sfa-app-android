package com.barikoi.cnlapp.ui.add_gift

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.adapter.AdapterImagePickerView
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.Gift
import com.barikoi.cnlapp.data.remote.models.GiftModel
import com.barikoi.cnlapp.data.remote.models.giftModelList
import com.barikoi.cnlapp.databinding.ActivityAddGiftBinding
import com.barikoi.cnlapp.databinding.DialogConfirmGiftBinding
import com.barikoi.cnlapp.ui.add_gift.adapter.AdapterMainGift
import com.barikoi.cnlapp.ui.add_gift.vm.AddGiftViewModel
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.isViewEnable
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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


    private lateinit var adapterGift: AdapterMainGift

    var outletId: String? = null


    private var imageFilePath: File? = null

    private var posMain = 0
    private var posChild = 0

    private var giftData: MutableList<GiftModel> = mutableListOf()


    private var imageFiles: MutableList<String> = mutableListOf()


    var count = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddGiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startGetGiftObserve()
        startSaveGiftObserve()

        binding.toolbar.tvTitle.text = getString(R.string.add_gift)

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        outletId = intent.getStringExtra("outlet_id")


        binding.tvShopName.text = intent.getStringExtra("shop_name")




        adapterImage = AdapterImagePickerView {
            imageFiles.removeAt(it)
            adapterImage.updateImages(imageFiles)
        }

        adapterGift = AdapterMainGift(
            addClickListener = { posMain, posChild ->
                this.posMain = posMain
                this.posChild = posChild

                imageFiles.clear()
                confirmAddGiftDialog()
            },
            removeItemClickListener = { posMain, posChild ->
                giftData[posMain].gifts[posChild].qty = 0
                giftData[posMain].gifts[posChild].images = mutableListOf()
                adapterGift.updateList(giftData)

                binding.tvCount.text = giftData.flatMap { it.gifts }.sumOf { it.qty }.toString()
            },
            incrementClickListener = { posMain, posChild ->
                giftData[posMain].gifts[posChild].qty += 1
                adapterGift.updateList(giftData)

                binding.tvCount.text = giftData.flatMap { it.gifts }.sumOf { it.qty }.toString()
            },
            decrementClickListener = { posMain, posChild ->
                if (giftData[posMain].gifts[posChild].qty > 0) {
                    giftData[posMain].gifts[posChild].qty -= 1
                    adapterGift.updateList(giftData)
                }

                binding.tvCount.text = giftData.flatMap { it.gifts }.sumOf { it.qty }.toString()
            }
        )

        binding.rcvGift.layoutManager = LinearLayoutManager(this)
        binding.rcvGift.adapter = adapterGift

        binding.btnSubmit.setHapticClickListener {

            val gifts: List<Gift> = giftData
                .flatMap { it.gifts } // Flatten all Gift lists
                .filter { it.images?.isNotEmpty() == true } // Filter only Gifts with images

            if (gifts.isEmpty()) {
                toast(getString(R.string.please_add_gift))
                return@setHapticClickListener
            }

            saveGift(gifts)
        }

        val giftDataJson = intent.getStringExtra("gift_data")
        if (!giftDataJson.isNullOrEmpty()) {
            giftData = giftDataJson.giftModelList().toMutableList()
            adapterGift.updateList(giftData)

            binding.toolbar.tvTitle.text = getString(R.string.edit_gift)

            binding.tvCount.text = giftData.flatMap { it.gifts }.sumOf { it.qty }.toString()
            return
        }

        viewModel.getGifts()
    }

    private fun startGetGiftObserve() {
        lifecycleScope.launch {
            viewModel.giftResponse.observe(this@AddGiftActivity) {
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

                        giftData = processData(it.data?.gifts!!).toMutableList()
                        adapterGift.updateList(giftData)

                    }
                }
            }
        }
    }

    private fun startSaveGiftObserve() {
        lifecycleScope.launch {
            viewModel.saveGiftResponse.observe(this@AddGiftActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        binding.btnSubmit.isViewEnable(true)
                        AppLogger.log("startSaveGiftObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.btnSubmit.isViewEnable(true)
                        AppLogger.log("startSaveGiftObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.btnSubmit.isViewEnable(false)
                        AppLogger.log("startSaveGiftObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.btnSubmit.isViewEnable(false)
                        AppLogger.log("startSaveGiftObserve:: Success ${it.data}")

                        toast(it.data?.message!!)

                        setResult(
                            RESULT_OK,
                            Intent().putExtra("gift_result", Gson().toJson(giftData))
                        )
                        finish()
                    }
                }
            }
        }
    }

    private fun processData(data: List<Gift>): List<GiftModel> {
        val groupedData = data.groupBy { it.categoryId }

        return groupedData.map { (_, gifts) ->
            GiftModel(
                title = gifts.first().category.name, // Using category name as title
                gifts = gifts
            )
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
        if (result.resultCode == RESULT_OK) {

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
        val dialogBinding = DialogConfirmGiftBinding.inflate(LayoutInflater.from(this))

        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
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

        dialogBinding.tvCount.setText(count.toString())


        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        dialogBinding.llImagePickerView.rvImage.adapter = adapterImage
        dialogBinding.llImagePickerView.rvImage.layoutManager = layoutManager
        adapterImage.updateImages(imageFiles)

        dialogBinding.btnConfirm.setOnClickListener {
            if (imageFiles.isEmpty()) {
                toast(getString(R.string.please_add_image))
                return@setOnClickListener
            }

            dialog.dismiss()

            giftData[posMain].gifts[posChild].qty = count
            giftData[posMain].gifts[posChild].images = imageFiles.toMutableList()
            adapterGift.updateList(giftData)

            binding.tvCount.text = giftData.flatMap { it.gifts }.sumOf { it.qty }.toString()
            count = 1

        }
        dialogBinding.btnNo.setOnClickListener {

            dialog.dismiss()
            count = 1
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

    private fun saveGift(data: List<Gift>) {
        lifecycleScope.launch {
            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("outlet_id", outletId!!)
                .addFormDataPart("for_update", "1")

                .apply {
                    data.forEachIndexed { i, gift ->
                        addFormDataPart("gift_details[$i][type_id]", gift.id.toString())
                    }
                }

                .apply {
                    data.forEachIndexed { i, gift ->
                        addFormDataPart("gift_details[$i][qty]", gift.qty.toString())
                    }
                }

                .apply {
                    data.forEachIndexed { mainPos, gift ->
                        gift.images!!.forEachIndexed { childPos, image ->
                            addFormDataPart(
                                "gift_details[$mainPos][images][$childPos]",
                                "gift_details[$mainPos][images][$childPos]",
                                Compressor.compress(
                                    this@AddGiftActivity,
                                    File(image)
                                ) {
                                    resolution(RESOLUTION_WIDTH, RESOLUTION_HEIGHT)
                                    quality(IMAGE_QUALITY)
                                    format(Bitmap.CompressFormat.JPEG)
                                    size(MAX_FILE_SIZE)
                                }.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                            )
                        }
                    }
                }
                .build()

            viewModel.saveGift(requestBody)
        }
    }

    companion object {
        // Image compress config...
        const val MAX_FILE_SIZE: Long = 1_048_576 //bytes
        const val RESOLUTION_WIDTH: Int = 1280
        const val RESOLUTION_HEIGHT: Int = 720
        const val IMAGE_QUALITY: Int = 80
    }

}