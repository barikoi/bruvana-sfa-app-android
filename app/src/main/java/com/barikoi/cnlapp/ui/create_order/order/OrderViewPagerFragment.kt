package com.barikoi.cnlapp.ui.create_order.order

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.adapter.AdapterImagePickerView
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.callback.LocationFetch
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.data.remote.models.offer.Offer
import com.barikoi.cnlapp.data.remote.models.product.Product
import com.barikoi.cnlapp.data.remote.models.request.order.ComboRequest
import com.barikoi.cnlapp.data.remote.models.request.order.OrderRequest
import com.barikoi.cnlapp.data.remote.models.request.order.toProductRequestList1
import com.barikoi.cnlapp.databinding.DialogConfirmOrderBinding
import com.barikoi.cnlapp.databinding.FragmentOrderViewPagerBinding
import com.barikoi.cnlapp.order_create.Callback.DialogListener
import com.barikoi.cnlapp.ui.create_order.order.combo.ComboOfferFragment
import com.barikoi.cnlapp.ui.create_order.order.product_selection.ProductSelectionFragment
import com.barikoi.cnlapp.ui.create_order.order.product_selection.vm.ProductSelectViewModel
import com.barikoi.cnlapp.ui.add_gift.AddGiftActivity
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.formattedDateTime
import com.barikoi.cnlapp.utils.extension.loadingDialog
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor.compress
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
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class OrderViewPagerFragment : Fragment() {
    private lateinit var binding: FragmentOrderViewPagerBinding

    private val viewModel: ProductSelectViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private lateinit var adapterImage: AdapterImagePickerView
    private var products: List<Product> = emptyList()
    private var offers: List<Offer> = emptyList()


    private var imageFilePath: File? = null
    private var imageFiles: MutableList<String> = mutableListOf()

    var orderStartTime = Calendar.getInstance().formattedDateTime()


    val builder = SpannableStringBuilder()

    private var outlet: Outlet? = null

    private lateinit var confirmOrderDialog: Dialog


    private var orderTypeNew: String = "save_order"
    private var distanceValue = 0.0


    private var startCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            AppLogger.log("startCameraNew:: $imageFilePath")
            AppLogger.log("startCameraNew:: ${result.data}")

            AppLogger.log("IMAGE_PICKER:: ${sharePrefUtils.getString(Constants.IMAGE_PICKER)}")

            imageFiles.add(imageFilePath!!.path)
            confirmDialog(
                SpannableStringBuilder(sharePrefUtils.getString(Constants.IMAGE_PICKER)),
                orderTypeNew
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().loadingDialog {
            confirmOrderDialog = it
        }

        startReverseObserve()
        startOrderObserve()

        outlet = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(Constants.SHOP, Outlet::class.java)
        } else {
            @Suppress("DEPRECATION") arguments?.getParcelable(Constants.SHOP)
        }

        if (outlet == null) {
            toast("Outlet not found")
            return
        }

        binding.minOV.text = getString(
            R.string.min_order_value, outlet!!.minimumOrder.toString()
        )

        setDistance(
            outlet!!
        )

        adapterImage = AdapterImagePickerView {
            imageFiles.removeAt(it)
            adapterImage.updateImages(imageFiles)
        }

        binding.tvShopName.text = arguments?.getString(Constants.SHOP_NAME)

        binding.imgRefresh.setHapticClickListener {
            ViewUtils.rotateAnimation(binding.imgRefresh, 0f, 380f)

            ViewUtils.getLocation(requireContext(), requireActivity(), object : LocationFetch {
                override fun onFetchSuccess(location: Location) {
                    viewModel.getReverseGeo(
                        location.latitude.toString(), location.longitude.toString()
                    )
                }

                override fun onFailure() {
                    toast("Location fetch failed")
                }
            })
        }

        val titles = arrayOf(
            getString(R.string.product_selection), getString(R.string.combo_offer)
        )

        val fragments = ArrayList<Fragment>()
        fragments.add(ProductSelectionFragment(viewModel, outlet!!))
        fragments.add(ComboOfferFragment(viewModel))
        binding.viewPager.adapter = ViewPagerAdapter(childFragmentManager, lifecycle, fragments)

        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()

        binding.viewPager.isUserInputEnabled = false

        lifecycleScope.launch {
            viewModel.products.collect {
                AppLogger.log("OrderViewPagerFragment::products ${it.size}")
                products = it

                updateData()
            }
        }

        lifecycleScope.launch {
            viewModel.startOrderTime.collect {
                AppLogger.log("OrderViewPagerFragment::startOrderTime $it")
                orderStartTime = it
            }
        }

        lifecycleScope.launch {
            viewModel.offers.collect {
                offers = it
                updateData()
            }
        }

        binding.btnSaveOrder.setHapticClickListener {
            // Check if the selected 0 qty product
            if (!products.any { product -> product.qty > 0 } && !offers.any { offer -> offer.quantity > 0 }) {
                toast("Please select at least one product")
                return@setHapticClickListener
            }

            builder.append(getString(R.string.you_are))

            distanceValue.let {
                val colorRes = if (it > 100.0) R.color.status_bounced else R.color.cnl_color_2
                builder.append(
                    ViewUtils.createColoredSpan(
                        binding.tvDistance.text.toString(), colorRes, requireContext()
                    )
                )
            }

            builder.append(getString(R.string.away_from))
            builder.append(
                ViewUtils.createColoredSpan(
                    outlet!!.outletName, R.color.cnl_color_1, requireContext()
                )
            )

            confirmDialog(
                builder, "save_order"
            )
        }

        binding.btnNoOrder.setHapticClickListener {
            builder.append(getString(R.string.you_are))

            distanceValue.let {
                val colorRes = if (it > 100.0) R.color.status_bounced else R.color.cnl_color_2
                builder.append(
                    ViewUtils.createColoredSpan(
                        binding.tvDistance.text.toString(), colorRes, requireContext()
                    )
                )
            }

            builder.append(getString(R.string.away_from))
            builder.append(
                ViewUtils.createColoredSpan(
                    outlet!!.outletName, R.color.cnl_color_1, requireContext()
                )
            )

            builder.append(SpannableString(getString(R.string.and_selecting_no_order)))
            confirmDialog(builder, "no_order")
        }
    }

    private fun saveOrder(
        orderRequest: OrderRequest, combos: List<ComboRequest>
    ) {
        AppLogger.log("saveOrder:: $orderRequest and combos $combos")
        lifecycleScope.launch {
            val requestBody: RequestBody = MultipartBody.Builder().setType(MultipartBody.Companion.FORM)
                .addFormDataPart("orders", Gson().toJson(listOf(orderRequest)))
                .addFormDataPart("combos", Gson().toJson(combos))
                .apply {
                    imageFiles.forEachIndexed { pos, image ->
                        addFormDataPart(
                            "image[$pos]", image.substring(
                                image.lastIndexOf("/")
                            ), compress(
                                requireActivity(), File(image)
                            ) {
                                resolution(
                                    AddGiftActivity.Companion.RESOLUTION_WIDTH,
                                    AddGiftActivity.Companion.RESOLUTION_HEIGHT
                                )
                                quality(AddGiftActivity.Companion.IMAGE_QUALITY)
                                format(Bitmap.CompressFormat.JPEG)
                                size(AddGiftActivity.Companion.MAX_FILE_SIZE)
                            }.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                        )
                    }
                }.build()

            viewModel.saveOrder(requestBody)
        }
    }

    private fun saveNoOrder(
        orderRequest: OrderRequest
    ) {
        lifecycleScope.launch {
            val requestBody: RequestBody = MultipartBody.Builder().setType(MultipartBody.Companion.FORM)
                .addFormDataPart("orders", Gson().toJson(listOf(orderRequest)))
                .apply {
                    imageFiles.forEachIndexed { pos, image ->
                        addFormDataPart(
                            "image[$pos]", image.substring(
                                image.lastIndexOf("/")
                            ), compress(
                                requireActivity(), File(image)
                            ) {
                                resolution(
                                    AddGiftActivity.Companion.RESOLUTION_WIDTH,
                                    AddGiftActivity.Companion.RESOLUTION_HEIGHT
                                )
                                quality(AddGiftActivity.Companion.IMAGE_QUALITY)
                                format(Bitmap.CompressFormat.JPEG)
                                size(AddGiftActivity.Companion.MAX_FILE_SIZE)
                            }.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                        )
                    }
                }.build()

            viewModel.saveNoOrder(requestBody)
        }
    }

    private fun confirmDialog(
        styleString: SpannableStringBuilder, orderType: String
    ) {
        orderTypeNew = orderType
        val dialogBinding = DialogConfirmOrderBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = Dialog(requireContext())
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
            sharePrefUtils.saveString(Constants.IMAGE_PICKER, styleString.toString())
            dialog.dismiss()
            openCameraForApplicant()
        }

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        dialogBinding.llImagePickerView.rvImage.adapter = adapterImage
        dialogBinding.llImagePickerView.rvImage.layoutManager = layoutManager
        adapterImage.updateImages(imageFiles)

        dialogBinding.tvMessage.setText(styleString, TextView.BufferType.SPANNABLE)


        dialogBinding.btnConfirm.setOnClickListener {
            if (distanceValue > 100) {
                if (imageFiles.isEmpty()) {
                    toast("Please capture an image")
                    return@setOnClickListener
                }
            }
            builder.clear()
            dialog.dismiss()

            ViewUtils.getLocation(requireContext(), requireActivity(), object : LocationFetch {
                override fun onFetchSuccess(location: Location) {
                    if (orderTypeNew == "save_order") {
                        saveOrder(
                            buildOrderRequest(outlet!!, location), buildCombo()
                        )
                    } else {
                        saveNoOrder(
                            buildNoOrderRequest(outlet!!, location)
                        )
                    }
                }

                override fun onFailure() {}
            })

        }
        dialogBinding.btnNo.setOnClickListener {
            builder.clear()
            dialog.dismiss()
        }

        if (distanceValue > 100) {

            dialogBinding.tvCaptureImage.isVisible = true
            dialogBinding.llImagePickerView.llMain.isVisible = true
        } else {
            dialogBinding.tvCaptureImage.isVisible = false
            dialogBinding.llImagePickerView.llMain.isVisible = false
        }

        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun buildOrderRequest(
        outlet: Outlet, location: Location
    ): OrderRequest {
        return OrderRequest(
            sharePrefUtils.getString(Api.USER_ID)!!,
            sharePrefUtils.getString(Api.EMPLOYEE_ID)!!,
            outlet.id.toString(),
            distanceValue.toString(),
            location.latitude.toString(),
            location.longitude.toString(),
            if (buildCombo().isNotEmpty()) "1" else "0",
            orderStartTime,
            Calendar.getInstance().formattedDateTime(),
            Calendar.getInstance().formattedDateTime(),
            products.toProductRequestList1(offers),
            (products.sumOf { product -> product.qty } + offers.sumOf
            { offer -> offer.quantity * offer.productCombinations.sumOf { com -> com.quantity } }).toString(),
            (products.sumOf { product -> product.discountedUnitPrice * product.qty }
                    + offers.sumOf { offer -> offer.comboPrice.toDouble() * offer.quantity }).toString()
        )
    }

    private fun buildNoOrderRequest(
        outlet: Outlet, location: Location
    ): OrderRequest {
        return OrderRequest(
            sharePrefUtils.getString(Api.USER_ID)!!,
            sharePrefUtils.getString(Api.EMPLOYEE_ID)!!,
            outlet.id.toString(),
            distanceValue.toString(),
            location.latitude.toString(),
            location.longitude.toString(),
            "0",
            orderStartTime,
            Calendar.getInstance().formattedDateTime(),
            Calendar.getInstance().formattedDateTime(),
            null,
            null,
            null
        )
    }

    private fun buildCombo(): List<ComboRequest> {
        return offers.filter { it.quantity > 0 }.map {
            ComboRequest(
                it.id, it.quantity
            )
        }
    }

    private fun updateData() {
        val totalQty =
            products.sumOf { product -> product.qty } + offers.sumOf { offer -> offer.quantity * offer.productCombinations.sumOf { com -> com.quantity } }
        val totalAmount = products.sumOf { product ->
            product.discountedUnitPrice * product.qty
        } + offers.sumOf { offer ->
            offer.comboPrice.toDouble() * offer.quantity
        }

        binding.tvTotalItemCount.text = getString(
            R.string.items_, totalQty.toString()
        )

        binding.tvTotalAmount.text = getString(
            R.string.total_amount, String.Companion.format(Locale.getDefault(), "%.2f", totalAmount)
        )
    }


    @SuppressLint("QueryPermissionsNeeded")
    private fun openCameraForApplicant() {
        val pictureIntent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )

        if (pictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                imageFilePath = ViewUtils.createImageFile()
            } catch (e: IOException) {
                AppLogger.log("openCamera:: $e")
            }
            if (imageFilePath != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireActivity(),
                    "${requireActivity().packageName}.fileprovider",
                    imageFilePath!!
                )
                AppLogger.log("openCamera:: $photoURI")
                pictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT, photoURI
                )
                startCamera.launch(pictureIntent)
            }
        }
    }

    private fun setDistance(
        outlet: Outlet
    ) {
        ViewUtils.getLocation(requireContext(), requireActivity(), object : LocationFetch {
            override fun onFetchSuccess(location: Location) {

                viewModel.getReverseGeo(
                    location.latitude.toString(), location.longitude.toString()
                )

                val distance = Constants.getDistance(
                    location.latitude,
                    location.longitude,
                    outlet.latitude.toDouble(),
                    outlet.longitude.toDouble()
                )

                distanceValue = distance.toDouble()

                if (distance > 100) {
                    if (distance > 1000) {
                        val distanceKM = distance / 1000
                        binding.tvDistance.text =
                            getString(R.string.km, DecimalFormat("#.##").format(distanceKM))
                    } else {
                        binding.tvDistance.text =
                            getString(R.string.meter, DecimalFormat("#.##").format(distance))
                    }

                    binding.tvDistance.setTextColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.status_bounced
                        )
                    )
                } else {
                    binding.tvDistance.setTextColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.cnl_color_2
                        )
                    )

                    binding.tvDistance.text =
                        getString(R.string.meter, DecimalFormat("#.##").format(distance))
                }
            }

            override fun onFailure() {
                toast("Location fetch failed")
            }

        })
    }

    private fun startReverseObserve() {
        lifecycleScope.launch {
            viewModel.reverseGeoResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startProductObserve::Empty")
                    }

                    is ApiState.Error -> {
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        binding.tvLocation.text = it.data!!.place.address
                    }
                }
            }
        }
    }

    private fun startOrderObserve() {
        lifecycleScope.launch {
            viewModel.orderResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startOrderObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startOrderObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("startOrderObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startOrderObserve:: Success ${it.data}")

                        if (it.data?.message == "stock out") {
                            toast("Stock out for some products")
                            return@observe
                        }

                        ViewUtils.viewDialogResponse(
                            requireContext(),
                            it.data?.message ?: "Order saved successfully",
                            object : DialogListener {
                                override fun onConfirmed() {
                                    parentFragmentManager.popBackStack()
                                }

                                override fun onCanceled() {}

                            })
                    }
                }
            }
        }
    }
}