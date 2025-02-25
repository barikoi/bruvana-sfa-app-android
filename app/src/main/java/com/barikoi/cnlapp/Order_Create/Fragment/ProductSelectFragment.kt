package com.barikoi.cnlapp.Order_Create.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.Order_Create.Adapter.ProductListAdapter
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.Order_Create.Callback.OnValueChangeListener
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.Order_Create.RoomDB.SaveOrder
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.StatisticsHome.Adapter.OutletProductAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.barikoi.cnlapp.base.adapter.AdapterImagePickerView
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.callback.LocationFetch
import com.barikoi.cnlapp.databinding.DialogConfirmOrderBinding
import com.barikoi.cnlapp.databinding.FragmentProductSelectBinding
import com.barikoi.cnlapp.ui.add_gift.AddGiftActivity
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ProductSelectFragment : Fragment(), OnValueChangeListener {
    private lateinit var binding: FragmentProductSelectBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private lateinit var adapterImage: AdapterImagePickerView

    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var token: String? = null
    var user_id: String? = null
    var sr_id: String? = null
    var selectedShop: Shops? = null
    private var selectedOrder: OrderList? = null
    private var totalAmount: String? = null
    private var grandTotalPrice: Double? = 0.0
    private var totalCount: Int? = 0
    private var shopName: String? = null
    private var outletMinOrder: String? = ""
    var shopId: String? = null
    var routeId: String? = null
    var distanceValue: Double? = null
    var listener: OnValueChangeListener? = null
    private var adapter: ProductListAdapter? = null
    var productsList: ArrayList<Products>? = ArrayList()
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    lateinit var ACTIVITY: MainActivity
    private var appDatabase: AppDatabase? = null
    private var addedProducts: ArrayList<Products>? = ArrayList()
    private var dFormat = DecimalFormat("#.##")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    private var startOrderTime: String? = null
    private var startOrderTimeA: Date? = null


    private var imageFilePath: File? = null
    private var imageFiles: MutableList<String> = mutableListOf()


    val builder = SpannableStringBuilder()

    private var orderTypeNew: String = "save_order"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductSelectBinding.inflate(inflater, container, false)

        adapterImage = AdapterImagePickerView {
            imageFiles.removeAt(it)
            adapterImage.updateImages(imageFiles)
        }

        binding.btnAddGift.setOnClickListener {
            startActivity(
                Intent(mContext, AddGiftActivity::class.java)
            )
        }

        binding.ivShort.setOnClickListener {
            val popup = PopupMenu(mContext, binding.ivShort)
            popup.menuInflater.inflate(R.menu.sort_menu_product, popup.menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.menu_ztoa -> {
                            productsList!!.sortByDescending {
                                it.product_name
                            }
                            if (productsList!!.size > 0) {
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                binding.productlist.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }

//                            binding.sortTitle.text = resources.getString(R.string.ztoa)
                        }

                        R.id.menu_atoz -> {
                            productsList!!.sortBy {
                                it.product_name
                            }
                            if (productsList!!.size > 0) {
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                binding.productlist.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
//                            binding.sortTitle.text = resources.getString(R.string.atoz)
                        }

                        R.id.menu_mostfrequent -> {
                            productsList!!.sortByDescending {
                                it.quantity_last_month
                            }
                            if (productsList!!.size > 0) {
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                binding.productlist.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }

//                            binding.sortTitle.text = resources.getString(R.string.most_frequent)
                        }

                        R.id.menu_lowstock -> {
                            productsList!!.sortBy {
                                it.stock_available
                            }
                            if (productsList!!.size > 0) {
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                binding.productlist.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
//                            binding.sortTitle.setText(resources.getString(R.string.low_stock))
                        }

                        R.id.menu_highstock -> {
                            productsList!!.sortByDescending {
                                it.stock_available
                            }
                            if (productsList!!.size > 0) {
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                binding.productlist.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
//                            binding.sortTitle.setText(resources.getString(R.string.high_stock))
                        }
                    }
                    return true
                }
            })
            popup.show()
        }

        binding.editTextSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.filter.filter(s)
                if (s!!.length == 0) {
                    getAllProducts()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.saveOrder.setOnClickListener {
            if (distanceValue != null) {
                if (addedProducts!!.size > 0) {
                    val str1 = SpannableString(getString(R.string.you_are))
                    builder.append(str1)
                    val strDistance = SpannableString(binding.tvdistance.text)
                    if (distanceValue != null) {
                        if (distanceValue!! > 100.0) {
                            strDistance.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        mContext!!,
                                        R.color.status_bounced
                                    )
                                ),
                                0,
                                strDistance.length,
                                0
                            )
                        } else {
                            strDistance.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        mContext!!,
                                        R.color.cnl_color_2
                                    )
                                ),
                                0,
                                strDistance.length,
                                0
                            )
                        }
                    }
                    builder.append(strDistance)
                    val str2 = SpannableString(getString(R.string.away_from))
                    builder.append(str2)
                    val strShop = SpannableString(shopName)
                    strShop.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                mContext!!,
                                R.color.cnl_color_1
                            )
                        ),
                        0,
                        strShop.length,
                        0
                    )
                    builder.append(strShop)
                    appDatabase!!.saveOrderDao().deleteALL()

                    confirmDialog(
                        builder,
                        "save_order"
                    )

                    return@setOnClickListener

                    ViewUtils.viewDialog(
                        mContext!!,
                        "",
                        /*"Are you sure want to save " + str1 + "'s order?"*/
                        builder,
                        object :
                            DialogListener {
                            override fun onConfirmed() {
                                binding.progressBar.visibility = View.VISIBLE
                                getLocation("save_order")
                            }

                            override fun onCanceled() {
                                getLocation("reversegeo")
                            }

                        })
                } else {
                    val builder = SpannableStringBuilder()
                    val str1 = SpannableString(getString(R.string.you_are))
                    builder.append(str1)
                    val strDistance = SpannableString(binding.tvdistance.text)
                    if (distanceValue != null) {
                        if (distanceValue!! > 100.0) {
                            strDistance.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        mContext!!,
                                        R.color.status_bounced
                                    )
                                ),
                                0,
                                strDistance.length,
                                0
                            )
                        } else {
                            strDistance.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        mContext!!,
                                        R.color.cnl_color_2
                                    )
                                ),
                                0,
                                strDistance.length,
                                0
                            )
                        }
                    }
                    builder.append(strDistance)
                    val str2 = SpannableString(getString(R.string.away_from))
                    builder.append(str2)
                    val strShop = SpannableString(shopName)
                    strShop.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                mContext!!,
                                R.color.cnl_color_1
                            )
                        ),
                        0,
                        strShop.length,
                        0
                    )
                    builder.append(strShop)
                    val str3 = SpannableString(getString(R.string.and_selecting_no_order))
                    builder.append(str3)
//                    confirmDialog(
//                        builder
//                    )

                    return@setOnClickListener
                    ViewUtils.viewDialog(
                        mContext!!,
                        "",
                        builder,
                        object :
                            DialogListener {
                            override fun onConfirmed() {
                                binding.progressBar.visibility = View.GONE
                            }

                            override fun onCanceled() {
                                getLocation("reversegeo")
                            }

                        })
                }
            } else {
                Toast.makeText(
                    mContext!!,
                    "Kindly wait for distance to be updated",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.noOrder.setOnClickListener {
            val builder = SpannableStringBuilder()
            val str1 = SpannableString(getString(R.string.you_are))
            builder.append(str1)
            val strDistance = SpannableString(binding.tvdistance.text)
            if (distanceValue != null) {
                if (distanceValue!! > 100.0) {
                    strDistance.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                mContext!!,
                                R.color.status_bounced
                            )
                        ),
                        0,
                        strDistance.length,
                        0
                    )
                } else {
                    strDistance.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                mContext!!,
                                R.color.cnl_color_2
                            )
                        ),
                        0,
                        strDistance.length,
                        0
                    )
                }
            }
            builder.append(strDistance)
            val str2 = SpannableString(getString(R.string.away_from))
            builder.append(str2)
            val strShop = SpannableString(shopName)
            strShop.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(mContext!!, R.color.cnl_color_1)),
                0,
                strShop.length,
                0
            )
            builder.append(strShop)
            val str3 = SpannableString(getString(R.string.and_selecting_no_order))
            builder.append(str3)
            appDatabase!!.saveOrderDao().deleteALL()
            if (distanceValue != null) {
                confirmDialog(builder, "no_order")
                return@setOnClickListener

                ViewUtils.viewDialog(mContext!!, "", builder, object :
                    DialogListener {
                    override fun onConfirmed() {
                        binding.progressBar.visibility = View.VISIBLE
                        getLocation("no_order")
                    }

                    override fun onCanceled() {

                    }
                })
            } else {
                Toast.makeText(
                    mContext!!,
                    getString(R.string.kindly_wait_for_distance_to_be_updated),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        return binding.root
    }


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

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCameraForApplicant() {
        val pictureIntent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )

        if (pictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                imageFilePath = createImageFile()
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
                    MediaStore.EXTRA_OUTPUT,
                    photoURI
                )
                startCamera.launch(pictureIntent)
            }
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

    private fun confirmDialog(
        styleString: SpannableStringBuilder,
        orderType: String
    ) {
        orderTypeNew = orderType
        val dialogBinding = DialogConfirmOrderBinding.inflate(LayoutInflater.from(mContext))

        val dialog = Dialog(requireContext())
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
            sharePrefUtils.saveString(Constants.IMAGE_PICKER, styleString.toString())
            dialog.dismiss()
            openCameraForApplicant()
        }


        val layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)

        dialogBinding.llImagePickerView.rvImage.adapter = adapterImage
        dialogBinding.llImagePickerView.rvImage.layoutManager = layoutManager
        adapterImage.updateImages(imageFiles)

        dialogBinding.tvMessage.setText(styleString, TextView.BufferType.SPANNABLE)


        dialogBinding.btnConfirm.setOnClickListener {
            builder.clear()
            dialog.dismiss()
            getLocation(orderType)
        }
        dialogBinding.btnNo.setOnClickListener {
            builder.clear()
            dialog.dismiss()
            getLocation("reversegeo")
        }

        if (distanceValue!! > 100) {

            dialogBinding.tvCaptureImage.isVisible = true
            dialogBinding.llImagePickerView.llMain.isVisible = true
        } else {
            dialogBinding.tvCaptureImage.isVisible = false
            dialogBinding.llImagePickerView.llMain.isVisible = false
        }

        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = this.arguments

        if (bundle != null) {
            if (bundle.containsKey("from")) {
                if (bundle.getString("from").equals("Shop")) {
                    selectedShop = bundle.getSerializable("Shop") as Shops?
                    shopName = selectedShop!!.shop_name
                    outletMinOrder = selectedShop!!.min_order
                    shopId = selectedShop!!.shop_id
                    routeId = selectedShop!!.route_code
                    addedProducts!!.clear()
                } else if (bundle.getString("from").equals("Order")) {
                    selectedOrder = bundle.getSerializable("Order") as OrderList?
                    shopName = selectedOrder!!.outletName
                    //outletMinOrder = selectedOrder!!.min_order
                    shopId = selectedOrder!!.outletId
                    routeId = selectedOrder!!.routeId
                    addedProducts!!.clear()
                    appDatabase!!.saveOrderDao().deleteALL()
                    grandTotalPrice = selectedOrder!!.grandTotal.toDouble()
                    totalCount = selectedOrder!!.totalQuantity.toInt()
                    var itemCountt = 0
                    for (i in 0 until selectedOrder!!.brands_array.size) {
                        itemCountt = itemCountt + selectedOrder!!.brands_array[i].ordered_quantity
                    }

                    if (itemCountt == 1 || itemCountt == 0) {
                        binding.totalItemCount.text =
                            getString(R.string.items_, itemCountt.toString())
                    } else {
                        binding.totalItemCount.text =
                            getString(R.string.items_, itemCountt.toString())
                    }
                    binding.totalAmount.text = getString(
                        R.string.total_,
                        dFormat.format(selectedOrder!!.grandTotal.toDouble())
                    )
                    appDatabase!!.saveOrderDao().insertAll(
                        SaveOrder(
                            null,
                            selectedOrder!!.outletId,
                            itemCountt,
                            selectedOrder!!.grandTotal.toDouble()
                        )
                    )
                }
            }

        }

        binding.selectedShop.text = shopName
        binding.minOV.text = getString(R.string.min_order_value, outletMinOrder)

        binding.imgRefresh.setOnClickListener {
            rotateAnimation(binding.imgRefresh, 0f, 380f)
            getLocation("reversegeo")
        }

        try {
            ApiServices.apiGET(
                Api.verified_shop_list + "?user_id=" + user_id + "&outlet_id=" + shopId + "&with_last_week_order=1",
                queue!!,
                token!!,
                object : ApiServiceListener {
                    override fun onResponseSuccess(response: String) {
                        getPreviousOrders(response)
                    }

                    override fun onJSONResponseSuccess(response: JSONObject) {
                        TODO("Not yet implemented")
                    }

                    override fun onNetworkResponseSuccess(response: NetworkResponse) {
                        TODO("Not yet implemented")
                    }

                    override fun onResponseFailure(error: VolleyError) {
                        ViewUtils.getErrorResponse(error, mContext!!)
                    }

                    override fun onException(e: Exception) {
                        Toast.makeText(
                            mContext!!.applicationContext,
                            e.message, Toast.LENGTH_SHORT
                        ).show()
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
            Sentry.captureException(e)
        }

        if (selectedOrder != null) {
            binding.noOrder.visibility = View.GONE
            binding.saveOrder.visibility = View.GONE
            binding.updateOrder.visibility = View.VISIBLE
        } else {
            binding.noOrder.visibility = View.VISIBLE
            binding.saveOrder.visibility = View.VISIBLE
            binding.updateOrder.visibility = View.GONE
        }

        binding.updateOrder.setOnClickListener {
            appDatabase!!.saveOrderDao().deleteALL()
            val builder = SpannableStringBuilder()
            val str1 = SpannableString("Are you sure want to update ")
            builder.append(str1)
            val str2 = SpannableString(shopName)
            str2.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.cnl_color_1)),
                0,
                str2.length,
                0
            )
            builder.append(str2)
            val str3 = SpannableString("'s order?")
            builder.append(str3)
            ViewUtils.viewDialog(
                mContext!!,
                "", builder,
                object :
                    DialogListener {
                    override fun onConfirmed() {
                        binding.progressBar.visibility = View.VISIBLE
                        getLocation("update_order")
                    }

                    override fun onCanceled() {

                    }
                })

        }
        getAllProducts()
    }

    private fun rotateAnimation(v: View, fromDegrees: Float, toDegrees: Float) {
        // Create an animation instance
        val an: Animation = RotateAnimation(
            fromDegrees, toDegrees, (v.width / 2).toFloat(),
            (v.height / 2).toFloat()
        )
        an.setDuration(500)
        an.fillAfter = true
        an.repeatMode = Animation.RESTART
        v.startAnimation(an)
    }

    private fun getPreviousOrders(response: String) {
        var lastOrderDate = ""
        var lastDeliveryDate = ""
        var orderStatus = ""
        var outletCategory = ""
        var minimum_order = ""
        var brandArray = JSONArray()
        //val productItems: ArrayList<ProductStatistics> = ArrayList()
        val obj = JSONObject(response)
        val outletArray = obj.getJSONArray("outlets")
        minimum_order = outletArray.getJSONObject(0).getString("minimum_order")
        outletCategory = outletArray.getJSONObject(0).getString("outlet_category")
        val ordersArray = outletArray.getJSONObject(0).getJSONArray("orders")
        if (ordersArray.length() > 0) {
            for (i in 0 until ordersArray.length()) {
                //productItems.clear()
                val orderObj = ordersArray.getJSONObject(i)
                brandArray = orderObj.getJSONArray("products")
                //val outletName = outletObj.getString("outlet_name")
                lastOrderDate = orderObj.getString("ordered_at")
                lastDeliveryDate = orderObj.getString("delivered_at")
                orderStatus = orderObj.getString("order_status")

            }

        }
        viewDialog(
            mContext!!,
            shopName!!,
            lastDeliveryDate,
            orderStatus,
            brandArray,
            outletCategory,
            lastOrderDate,
            minimum_order
        )


    }

    fun getLocation(choice: String) {
        ViewUtils.getLocation(mContext!!, ACTIVITY, object : LocationFetch {
            override fun onFetchSuccess(location: Location) {
                if (choice.equals("no_order")) {
                    submitNoOrder(location)
                } else if (choice.equals("update_order")) {
                    updateOrder(location)
                } else if (choice.equals("reversegeo")) {
                    reverseGeoAddress(mContext!!, location.latitude, location.longitude)
                    getDistance(
                        location.latitude,
                        location.longitude,
                        selectedShop!!.latitude,
                        selectedShop!!.longitude,
                        "foot"
                    )
                } else {
                    submitOrder(location)
                }
            }

            override fun onFailure() {
                Toast.makeText(mContext!!, "Location fetch failed", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getDistance(
        currentLatitude: Double,
        currentLongitude: Double,
        shopLatitude: Double,
        shopLongitude: Double,
        profile: String
    ) {

        val distance = Constants.getDistance(
            currentLatitude,
            currentLongitude,
            shopLatitude,
            shopLongitude
        )
        distanceValue = distance.toDouble()

        if (distance > 100) {
            if (distance > 1000) {
                val distanceKM = distance / 1000
                binding.tvdistance.text =
                    getString(R.string.km, DecimalFormat("#.##").format(distanceKM))
            } else {
                binding.tvdistance.text =
                    getString(R.string.meter, DecimalFormat("#.##").format(distance))
            }

            binding.tvdistance.setTextColor(
                ContextCompat.getColor(
                    mContext!!,
                    R.color.status_bounced
                )
            )
        } else {
            binding.tvdistance.setTextColor(
                ContextCompat.getColor(
                    mContext!!,
                    R.color.cnl_color_2
                )
            )

            binding.tvdistance.text =
                getString(R.string.meter, DecimalFormat("#.##").format(distance))
        }

        binding.buttonLayout.visibility = View.VISIBLE

        return


        ApiServices.apiGET(Api.distance + Api.APIKEY + "/" + shopLongitude + "," + shopLatitude + "/" + currentLongitude + "," + currentLatitude + "?profile=" + profile,
            queue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        AppLogger.log("response: $response")
                        val data = JSONObject(response)

                        val dist = data.getString("Distance")
                        val dformat = DecimalFormat("#.##")
                        val dis2 = dist.substring(0, dist.indexOf(' ')).toDouble()

                        distanceValue = dformat.format(dis2 * 1000).toDouble()

                        if (dis2 < 1) {
                            val distMeter = dFormat.format(dis2 * 1000).toDouble()
                            if (distMeter > 100.0) {
                                binding.tvdistance.text =
                                    getString(R.string.meter, distMeter.toString())
                                binding.tvdistance.setTextColor(
                                    ContextCompat.getColor(
                                        mContext!!,
                                        R.color.status_bounced
                                    )
                                )
                            } else {
                                binding.tvdistance.setTextColor(
                                    ContextCompat.getColor(
                                        mContext!!,
                                        R.color.cnl_color_2
                                    )
                                )
                                binding.tvdistance.text =
                                    getString(R.string.meter, distMeter.toString())
                            }
                        } else {
                            binding.tvdistance.text =
                                getString(R.string.km, dFormat.format(dis2).toString())
                        }

                        binding.buttonLayout.visibility = View.VISIBLE

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                    }
                }

                override fun onJSONResponseSuccess(response: JSONObject) {

                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {

                }

                override fun onResponseFailure(error: VolleyError) {
                    if (error.networkResponse != null) {
                        try {
                            val s = String(error.networkResponse.data)
                            Log.d("Routes", "message: $s")
                            val data = JSONObject(s)
                            if (data.has("status") && data.getString("status").equals("400")) {
                                getDistance(
                                    currentLatitude,
                                    currentLongitude,
                                    shopLatitude,
                                    shopLongitude,
                                    "car"
                                )
                            }
                        } catch (e: UnsupportedEncodingException) {
                            Sentry.captureException(e)
                            e.printStackTrace()
                        } catch (e: JSONException) {
                            Sentry.captureException(e)
                            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }
                    }
                }

                override fun onException(e: Exception) {
                    Sentry.captureException(e)
                }

            })
    }

    fun reverseGeoAddress(context: Context, lat: Double, lng: Double) {
        ApiServices.apiGET(Api.reverseGeo + "?key=" + Api.APIKEY + "&latitude=" + lat + "&longitude=" + lng,
            queue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        val data = JSONObject(response)
                        val place = JSONObject(data.getString("place"))
                        var address = ""
                        if (!place.getString("address").equals("null")) {
                            address = place.getString("address")
                        }
                        val city = place.getString("city")
                        val area = place.getString("area")
                        binding.tvLocation.text = address + ", " + area + ", " + city
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                    }
                }

                override fun onJSONResponseSuccess(response: JSONObject) {

                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {

                }

                override fun onResponseFailure(error: VolleyError) {
                    Sentry.captureException(error)
                }

                override fun onException(e: Exception) {
                    Sentry.captureException(e)
                }

            })
    }

    private fun updateOrder(location: Location) {
        if (addedProducts!!.size > 0) {
            var deliveredQuantity = 0
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val today = df.format(Calendar.getInstance().time)
            val cal = Calendar.getInstance()
            cal.time = Calendar.getInstance().time
            cal.add(Calendar.DATE, 1)
            val nextDay = df.format(cal.time)

            val obj1 = JSONObject()
            val ordersArray = JSONArray()
            val orderObj = JSONObject()
            orderObj.put("outlet_id", shopId)
            orderObj.put("order_no", selectedOrder!!.orderId)
            orderObj.put("user_id", user_id)
            orderObj.put("employee_id", sr_id)
            /*orderObj.put("ordered_at", today)
            orderObj.put("delivered_at", nextDay)*/
            orderObj.put("total_ordered_amount", grandTotalPrice.toString())
            orderObj.put("total_ordered_quantity", totalCount.toString())
            orderObj.put("longitude", location.longitude.toString())
            orderObj.put("latitude", location.latitude.toString())
            orderObj.put("order_status", "SAVED")
            val brandsArray = JSONArray()
            for (j in 0 until addedProducts!!.size) {
                val brandObj = JSONObject()
                if (addedProducts!![j].ordered_quantity > 0) {
                    brandObj.put("product_id", addedProducts!![j].product_id)
                    brandObj.put("product_code", addedProducts!![j].product_code)
                    brandObj.put("product_name", addedProducts!![j].product_name)
                    brandObj.put("sku_code", addedProducts!![j].sku_code)
                    brandObj.put("unit_id", addedProducts!![j].unit_id)
                    brandObj.put("unit_name", addedProducts!![j].unit_name)
                    brandObj.put("unit_code", addedProducts!![j].unit_code)
                    brandObj.put("unit_price", addedProducts!![j].unit_price.toString())
                    brandObj.put(
                        "discounted_unit_price",
                        addedProducts!![j].discounted_unit_price.toString()
                    )
                    brandObj.put("category_id", addedProducts!![j].category_id)
                    brandObj.put("category_name", addedProducts!![j].category_name)
                    brandObj.put("category_code", addedProducts!![j].category_code)
                    brandObj.put("ordered_quantity", addedProducts!![j].ordered_quantity.toString())
                    brandObj.put("delivered_quantity", "0")
                    brandObj.put("bounced_quantity", "0")
                    brandObj.put(
                        "ordered_amount",
                        addedProducts!![j].ordered_total_price.toString()
                    )
                    brandObj.put("delivered_amount", "0")
                    brandObj.put("bounced_amount", "0")
                    brandsArray.put(brandObj)
                    deliveredQuantity = deliveredQuantity + addedProducts!![j].ordered_quantity
                }

            }
            orderObj.put("products", brandsArray)
            ordersArray.put(orderObj)
            obj1.put("orders", ordersArray)

            if (obj1.length() > 0) {
                Log.d("ConfirmOrder", "response: " + obj1)
                if (deliveredQuantity > 0) {
                    ApiServices.apiJSONObjectPOST(
                        Api.update_saved_order,
                        queue!!,
                        token!!,
                        obj1,
                        object : ApiServiceListener {
                            override fun onResponseSuccess(response: String) {
                                TODO("Not yet implemented")
                            }

                            override fun onJSONResponseSuccess(response: JSONObject) {
                                binding.progressBar.visibility = View.GONE
                                val message = response.getString("message")
                                //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()

                                ViewUtils.viewDialogResponse(
                                    mContext!!,
                                    message,
                                    object : DialogListener {
                                        override fun onConfirmed() {
                                            CreateOrderFragment.setCurrentFragment(
                                                ShopSelectFragment(),
                                                ACTIVITY
                                            )
                                        }

                                        override fun onCanceled() {
                                            TODO("Not yet implemented")
                                        }

                                    })
                            }

                            override fun onNetworkResponseSuccess(response: NetworkResponse) {

                            }

                            override fun onResponseFailure(error: VolleyError) {
                                binding.progressBar.visibility = View.GONE
                                ViewUtils.getErrorResponse(error, mContext!!)
                            }

                            override fun onException(e: Exception) {
                                binding.progressBar.visibility = View.GONE
                            }

                        })
                } else {
                    ViewUtils.viewDialogResponse(
                        mContext!!,
                        "No products selected to order",
                        object : DialogListener {
                            override fun onConfirmed() {
                                binding.progressBar.visibility = View.GONE
                            }

                            override fun onCanceled() {

                            }

                        })
                }

            }
        }
    }

    private fun submitOrder(location: Location) {
        if (addedProducts!!.size > 0) {
            var orderedQuantity = 0
            var orderedAmount = 0.0
            val today = dateFormat.format(Calendar.getInstance().time)

            AppLogger.log("START TIME: $today")
            AppLogger.log("END TIME: $startOrderTime")

            val a = Calendar.getInstance().time.time.minus(startOrderTimeA?.time!!)
            AppLogger.log("TIME DIFF:: $a")

            val obj1 = JSONObject()
            val ordersArray = JSONArray()
            val orderObj = JSONObject()
            orderObj.put("outlet_id", shopId)
            orderObj.put("user_id", user_id)
            orderObj.put("employee_id", sr_id)
            orderObj.put("ordered_at", today)
            if (startOrderTime != null) orderObj.put("order_start_time", startOrderTime)
            orderObj.put("order_end_time", today)
            if (binding.tvdistance.text.toString().isNotEmpty()) orderObj.put(
                "distance_from_outlets",
                distanceValue.toString()
            )
            /*orderObj.put("delivered_at", nextDay)*/
            /*orderObj.put("distributor_office_code", selectedShop!!.distributor_office_code)*/

            orderObj.put("longitude", location.longitude.toString())
            orderObj.put("latitude", location.latitude.toString())
            //orderObj.put("order_status", "SAVED")
            val brandsArray = JSONArray()
            //val brandList = orderList[i].brands_array
            for (j in 0 until addedProducts!!.size) {
                val brandObj = JSONObject()
                if (addedProducts!![j].ordered_quantity > 0) {
                    brandObj.put("product_id", addedProducts!![j].product_id)
                    brandObj.put("product_code", addedProducts!![j].product_code)
                    brandObj.put("product_name", addedProducts!![j].product_name)
                    brandObj.put("sku_code", addedProducts!![j].sku_code)
                    brandObj.put("unit_id", addedProducts!![j].unit_id)
                    brandObj.put("unit_name", addedProducts!![j].unit_name)
                    brandObj.put("unit_code", addedProducts!![j].unit_code)
                    brandObj.put("unit_price", addedProducts!![j].unit_price.toString())
                    brandObj.put(
                        "discounted_unit_price",
                        addedProducts!![j].discounted_unit_price.toString()
                    )
                    brandObj.put("category_id", addedProducts!![j].category_id)
                    brandObj.put("category_name", addedProducts!![j].category_name)
                    brandObj.put("category_code", addedProducts!![j].category_code)
                    brandObj.put("ordered_quantity", addedProducts!![j].ordered_quantity.toString())
                    brandObj.put("delivered_quantity", "0")
                    brandObj.put("bounced_quantity", "0")
                    brandObj.put(
                        "ordered_amount",
                        addedProducts!![j].ordered_total_price.toString()
                    )
                    brandObj.put("delivered_amount", "0")
                    brandObj.put("bounced_amount", "0")
                    brandsArray.put(brandObj)
                    orderedQuantity = orderedQuantity + addedProducts!![j].ordered_quantity
                    orderedAmount = orderedAmount + addedProducts!![j].ordered_total_price
                }

            }

            if (orderedQuantity == totalCount) {
                orderObj.put("total_ordered_amount", grandTotalPrice.toString())
                orderObj.put("total_ordered_quantity", totalCount.toString())
            } else {
                orderObj.put("total_ordered_amount", orderedAmount.toString())
                orderObj.put("total_ordered_quantity", orderedQuantity.toString())
            }

            orderObj.put("products", brandsArray)
            ordersArray.put(orderObj)
            obj1.put("orders", ordersArray)

            AppLogger.log("ConfirmOrder:: $obj1")

            if (obj1.length() > 0) {
                Log.d("ConfirmOrder", "response: $obj1")
                if (orderedQuantity > 0) {
                    ApiServices.apiJSONObjectPOST(
                        Api.confirm_order,
                        queue!!,
                        token!!,
                        obj1,
                        object : ApiServiceListener {
                            override fun onResponseSuccess(response: String) {}

                            override fun onJSONResponseSuccess(response: JSONObject) {
                                val message = response.getString("message")
                                binding.progressBar.visibility = View.GONE
                                //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                                appDatabase!!.saveOrderDao().deleteByShop(shopId!!)
                                appDatabase!!.orderListDao().deleteByShop(shopId!!)

                                ViewUtils.viewDialogResponse(
                                    mContext!!,
                                    message,
                                    object : DialogListener {
                                        override fun onConfirmed() {
                                            CreateOrderFragment.setCurrentFragment(
                                                ShopSelectFragment(),
                                                ACTIVITY
                                            )
                                        }

                                        override fun onCanceled() {}

                                    })
                            }

                            override fun onNetworkResponseSuccess(response: NetworkResponse) {

                            }

                            override fun onResponseFailure(error: VolleyError) {
                                try {
                                    ViewUtils.getErrorResponse(error, mContext!!)
                                    binding.progressBar.visibility = View.GONE
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onException(e: Exception) {
                                try {
                                    binding.progressBar.visibility = View.GONE
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                } else {
                    ViewUtils.viewDialogResponse(
                        mContext!!,
                        getString(R.string.no_products_selected_to_order),
                        object : DialogListener {
                            override fun onConfirmed() {
                                binding.progressBar.visibility = View.GONE
                            }

                            override fun onCanceled() {}
                        })
                }

            }
        }
    }

    private fun submitNoOrder(location: Location) {
        val today = dateFormat.format(Calendar.getInstance().time)
        AppLogger.log("START TIME: $today")
        AppLogger.log("END TIME: $startOrderTime")

        val a = Calendar.getInstance().time.time.minus(startOrderTimeA?.time!!)
        AppLogger.log("TIME DIFF:: $a")

        val obj1 = JSONObject()
        val ordersArray = JSONArray()
        val orderObj = JSONObject()
        orderObj.put("outlet_id", shopId)
        orderObj.put("user_id", user_id)
        orderObj.put("employee_id", sr_id)
        if (distanceValue.toString().isNotEmpty()) orderObj.put(
            "distance_from_outlets",
            distanceValue.toString()
        )

        if (startOrderTime != null) orderObj.put("order_start_time", startOrderTime)
        orderObj.put("order_end_time", today)

        orderObj.put("longitude", location.longitude.toString())
        orderObj.put("latitude", location.latitude.toString())
        ordersArray.put(orderObj)
        obj1.put("orders", ordersArray)

        if (obj1.length() > 0) {
            Log.d("ConfirmOrder", "response: $obj1")
            ApiServices.apiJSONObjectPOST(
                Api.no_order,
                queue!!,
                token!!,
                obj1,
                object : ApiServiceListener {
                    override fun onResponseSuccess(response: String) {
                        TODO("Not yet implemented")
                    }

                    override fun onJSONResponseSuccess(response: JSONObject) {
                        val message = response.getString("message")
                        binding.progressBar.visibility = View.GONE

                        ViewUtils.viewDialogResponse(mContext!!, message, object : DialogListener {
                            override fun onConfirmed() {
                                editor!!.putString(Api.ORDERED_ROUTE_ID, selectedShop!!.route_code)
                                editor!!.commit()
                                CreateOrderFragment.setCurrentFragment(
                                    ShopSelectFragment(),
                                    ACTIVITY
                                )
                            }

                            override fun onCanceled() {
                                TODO("Not yet implemented")
                            }

                        })
                    }

                    override fun onNetworkResponseSuccess(response: NetworkResponse) {

                    }

                    override fun onResponseFailure(error: VolleyError) {
                        binding.progressBar.visibility = View.GONE
                        ViewUtils.getErrorResponse(error, mContext!!)
                    }

                    override fun onException(e: Exception) {
                        binding.progressBar.visibility = View.GONE
                    }

                })
        }
    }

    private fun getAllProducts() {
        binding.progressBar.visibility = View.VISIBLE
        val url =
            Api.all_product_list + "?with_stock=1&user_id=" + user_id + "&is_active=1"/*+ "&route_id=" + routeId*/
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    binding.progressBar.visibility = View.GONE

                    val data = JSONObject(response)
                    if (data.has("products") && !data.isNull("products")) {
                        val productArray = data.getJSONArray("products")
                        if (productArray.length() > 0) {
                            productsList!!.clear()
                            for (i in 0 until productArray.length()) {
                                var orderedQty = 0
                                var orderedTotalPrice = 0.0
                                var imageUrl = "null"
                                val productObj = productArray.getJSONObject(i)
                                val productId = productObj.getString("id")
                                val productName =
                                    if (!productObj.isNull("product_name")) productObj.getString("product_name") else ""
                                val productCode =
                                    if (!productObj.isNull("product_code")) productObj.getString("product_code") else ""
                                /*val discount = if (!productObj.isNull("discount")) productObj.getDouble("discount") else 0.0*/
                                if (productObj.has("images") && !productObj.isNull("images")) {
                                    val imageArray = productObj.getJSONArray("images")
                                    if (imageArray.length() > 0) {
                                        val imageobj = imageArray.getJSONObject(0)
                                        if (imageobj.has("image_url")) {
                                            imageUrl = imageobj.getString("image_url")
                                        }
                                    }
                                }
                                val skuCode =
                                    if (!productObj.isNull("sku_code")) productObj.getString("sku_code") else ""
                                val unitId =
                                    if (!productObj.isNull("unit_id")) productObj.getString("unit_id") else ""
                                val unitName =
                                    if (!productObj.isNull("unit_name")) productObj.getString("unit_name") else ""
                                val unitCode =
                                    if (!productObj.isNull("unit_code")) productObj.getString("unit_code") else ""
                                val categoryId =
                                    if (!productObj.isNull("category_id")) productObj.getString("category_id") else ""
                                val categoryName =
                                    if (!productObj.isNull("category_name")) productObj.getString("category_name") else ""
                                val categoryCode =
                                    if (!productObj.isNull("category_code")) productObj.getString("category_code") else ""
                                val qtyLastMonth =
                                    if (!productObj.isNull("quantity_last_month")) productObj.getInt(
                                        "quantity_last_month"
                                    ) else 0
                                val availableStock =
                                    if (!productObj.isNull("current_available_stock")) productObj.getInt(
                                        "current_available_stock"
                                    ) else 0
                                var price = 0.0
                                var discount_price = 0.0
                                if (!productObj.isNull("unit_price")) {
                                    price = productObj.getDouble("unit_price")
                                } else {
                                    price = 0.0
                                }
                                if (!productObj.isNull("discounted_unit_price")) {
                                    discount_price = productObj.getDouble("discounted_unit_price")
                                } else {
                                    discount_price = 0.0
                                }
                                if (selectedOrder != null) {
                                    //var orderlistDB = appDatabase!!.orderListDao().getOrdersDB(selectedOrder!!.outletId.toString())
                                    val exist = selectedOrder!!.brands_array.find {
                                        it.product_id == productId
                                    }

                                    if (exist != null) {
                                        orderedQty = exist.ordered_quantity
                                        orderedTotalPrice = exist.ordered_total_price
                                    }
                                } else if (addedProducts!!.size > 0) {
                                    val exist = addedProducts?.find {
                                        it.product_id == productId
                                    }

                                    if (exist != null) {
                                        orderedQty = exist.ordered_quantity
                                        orderedTotalPrice = exist.ordered_total_price
                                    }
                                }
                                val products = Products(
                                    productId,
                                    productName,
                                    productCode,
                                    price,
                                    discount_price,
                                    skuCode,
                                    imageUrl,
                                    unitId,
                                    unitName,
                                    unitCode,
                                    categoryId,
                                    categoryName,
                                    categoryCode,
                                    qtyLastMonth,
                                    availableStock,
                                    0,
                                    orderedQty,
                                    orderedTotalPrice
                                )

                                productsList!!.add(products)
                                if (orderedQty > 0) {
                                    val exist = addedProducts?.find {
                                        it.product_id == productId
                                    }

                                    if (exist == null) {
                                        addedProducts!!.add(products)
                                    }

                                }
                            }

                            if (productsList!!.size > 0) {
                                productsList!!.sortByDescending { it.stock_available }
//                                binding.sortTitle.setText(resources.getString(R.string.high_stock))
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                binding.productlist.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
                        }

                    }
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    e.printStackTrace()
                }
            },
            { error ->
                binding.progressBar.visibility = View.GONE
                if (error is TimeoutError) {
                    //mListerner.onFailure("Request timeout!! Check your internet connection or Contact Admin")
                    Toast.makeText(
                        mContext,
                        "Request timeout!! Check your internet connection or Contact Admin",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error is NoConnectionError) {
                    //mListerner.onFailure("Turn on your internet connection and Try again")
                    Toast.makeText(
                        mContext,
                        "Turn on your internet connection and Try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error != null && error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Routes", "message: $s")
                        val data = JSONObject(s)
                        //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                        //mListerner.onFailure(data.getString("message"))
                        Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG)
                            .show()
                    } catch (e: UnsupportedEncodingException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        //mListerner.onFailure(e.message)
                        Sentry.captureException(e)
                        Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            })
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue!!.add(request)
    }

    fun viewDialog(
        mContext: Context,
        outlet_name: String,
        lastDelivery: String,
        statusOrder: String,
        brands_array: JSONArray,
        outletCategory: String,
        lastOrderDate: String,
        minimum_order: String
        /*listItem: ArrayList<ProductStatistics>*/
    ) {
        val dialog = Dialog(mContext)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_previous_order_list)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val outletName = dialog.findViewById<TextView>(R.id.outletName)
        val tvoutletCategory = dialog.findViewById<TextView>(R.id.tvcategory)
        val listView = dialog.findViewById<RecyclerView>(R.id.productList)
        val tvLastOrderDate = dialog.findViewById<TextView>(R.id.lastOrderDate)
        val tvLastDeliveryDate = dialog.findViewById<TextView>(R.id.lastDeliveryDate)
        val tvItemCount = dialog.findViewById<TextView>(R.id.itemCount)
        val tvGrandTotal = dialog.findViewById<TextView>(R.id.grandTotal)
        val tvOrderStatus = dialog.findViewById<TextView>(R.id.tvOrderStatus)
        val statusLayout = dialog.findViewById<LinearLayout>(R.id.layoutStatus)
        val filterLayout = dialog.findViewById<LinearLayout>(R.id.filterLayout)
        val filterTitle = dialog.findViewById<TextView>(R.id.filterTitle)
        val tvminOrderValue = dialog.findViewById<TextView>(R.id.minOV)
        val startOrder = dialog.findViewById<AppCompatButton>(R.id.btnStartOrder)

        val productItems: ArrayList<ProductStatistics> = ArrayList()
        var dformat = DecimalFormat("#.##")
        outletName.text = outlet_name
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val df = SimpleDateFormat("dd LLL yyyy", Locale.ENGLISH)
        if (lastOrderDate.length > 0 && !lastOrderDate.equals("null")) {
            val orderDate = df.format(oldDate.parse(lastOrderDate))
            tvLastOrderDate.text =
                mContext.resources.getString(R.string.last_order_date, orderDate)
        }
        if (lastDelivery.length > 0 && !lastDelivery.equals("null")) {
            val deliveryDate = df.format(oldDate.parse(lastDelivery))
            tvLastDeliveryDate.text =
                mContext.resources.getString(R.string.last_delivery_date) + deliveryDate
        }
        if (!outletCategory.equals("null")) {
            tvoutletCategory.text = outletCategory
        }
        if (!minimum_order.equals("null")) {
            tvminOrderValue.text = getString(R.string.min_order_value, minimum_order)
        } else {
            tvminOrderValue.text = getString(R.string.min_order_value, "N/A")
        }

        startOrder.setOnClickListener {
            startOrderTimeA = Calendar.getInstance().time
            startOrderTime = dateFormat.format(Calendar.getInstance().time)
            getLocation("reversegeo")
            dialog.dismiss()
        }

        if (statusOrder.equals("DELIVERED")) {
            filterLayout.visibility = View.VISIBLE
            filterLayout.setOnClickListener {
                val popup = PopupMenu(mContext, filterTitle)
                popup.menuInflater.inflate(R.menu.filter_menu_orderstatus, popup.menu)
                popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        when (item.itemId) {
                            R.id.menu_delivered_product -> {
                                productItems.clear()
                                filterTitle.setText(mContext.resources.getString(R.string.delivered))
                                if (brands_array.length() > 0) {
                                    for (j in 0 until brands_array.length()) {
                                        val brandObj = brands_array.getJSONObject(j)
                                        if (brandObj.getInt("delivered_quantity") > 0) {
                                            productItems.add(
                                                ProductStatistics(
                                                    brandObj.getString("product_id"),
                                                    brandObj.getString("product_name"),
                                                    brandObj.getString("product_code"),
                                                    brandObj.getString("sku_code"),
                                                    brandObj.getString("category_code"),
                                                    brandObj.getString("category_name"),
                                                    brandObj.getString("category_id"),
                                                    brandObj.getString("unit_name"),
                                                    brandObj.getString("unit_id"),
                                                    brandObj.getString("unit_code"),
                                                    brandObj.getDouble("unit_price"),
                                                    brandObj.getDouble("discounted_unit_price"),
                                                    brandObj.getDouble("ordered_amount"),
                                                    brandObj.getInt("ordered_quantity"),
                                                    brandObj.getInt("delivered_quantity"),
                                                    brandObj.getInt("bounced_quantity"),
                                                    brandObj.getDouble("delivered_amount")
                                                )
                                            )
                                        }
                                    }
                                }
                                tvItemCount.text = productItems.size.toString() + mContext.resources
                                    .getString(R.string.items)

                                var grandTotal = 0.0
                                if (productItems.size > 0) {
                                    for (i in 0 until productItems.size) {
                                        grandTotal = grandTotal + productItems[i].total_price
                                    }

                                }
                                val adapter = OutletProductAdapter(productItems)
                                listView.adapter = adapter
                                adapter.notifyDataSetChanged()

                                tvGrandTotal.text = dformat.format(grandTotal).toString()
                            }

                            R.id.menu_bounced_product -> {
                                productItems.clear()
                                filterTitle.text = mContext.resources.getString(R.string.bounced)
                                if (brands_array.length() > 0) {
                                    for (j in 0 until brands_array.length()) {
                                        val brandObj = brands_array.getJSONObject(j)
                                        if (brandObj.getInt("bounced_quantity") > 0) {
                                            productItems.add(
                                                ProductStatistics(
                                                    brandObj.getString("product_id"),
                                                    brandObj.getString("product_name"),
                                                    brandObj.getString("product_code"),
                                                    brandObj.getString("sku_code"),
                                                    brandObj.getString("category_code"),
                                                    brandObj.getString("category_name"),
                                                    brandObj.getString("category_id"),
                                                    brandObj.getString("unit_name"),
                                                    brandObj.getString("unit_id"),
                                                    brandObj.getString("unit_code"),
                                                    brandObj.getDouble("unit_price"),
                                                    brandObj.getDouble("discounted_unit_price"),
                                                    brandObj.getDouble("ordered_amount"),
                                                    brandObj.getInt("ordered_quantity"),
                                                    brandObj.getInt("bounced_quantity"),
                                                    brandObj.getInt("bounced_quantity"),
                                                    brandObj.getDouble("bounced_amount")
                                                )
                                            )
                                        }
                                    }
                                }
                                tvItemCount.text = productItems.size.toString() + mContext.resources
                                    .getString(R.string.items)

                                var grandTotal = 0.0
                                if (productItems.size > 0) {
                                    for (i in 0 until productItems.size) {
                                        grandTotal = grandTotal + productItems[i].total_price
                                    }

                                }
                                val adapter = OutletProductAdapter(productItems)
                                listView.adapter = adapter
                                adapter.notifyDataSetChanged()

                                tvGrandTotal.text = dformat.format(grandTotal).toString()
                            }
                        }
                        return true
                    }


                })
                popup.show()
            }
            productItems.clear()
            filterTitle.text = mContext.resources.getString(R.string.delivered)
            if (brands_array.length() > 0) {
                for (j in 0 until brands_array.length()) {
                    val brandObj = brands_array.getJSONObject(j)
                    if (brandObj.getInt("delivered_quantity") > 0) {
                        productItems.add(
                            ProductStatistics(
                                brandObj.getString("product_id"),
                                brandObj.getString("product_name"),
                                brandObj.getString("product_code"),
                                brandObj.getString("sku_code"),
                                brandObj.getString("category_code"),
                                brandObj.getString("category_name"),
                                brandObj.getString("category_id"),
                                brandObj.getString("unit_name"),
                                brandObj.getString("unit_id"),
                                brandObj.getString("unit_code"),
                                brandObj.getDouble("unit_price"),
                                brandObj.getDouble("discounted_unit_price"),
                                brandObj.getDouble("ordered_amount"),
                                brandObj.getInt("ordered_quantity"),
                                brandObj.getInt("delivered_quantity"),
                                brandObj.getInt("bounced_quantity"),
                                brandObj.getDouble("delivered_amount")
                            )
                        )
                    }
                }
            }
            tvItemCount.text = productItems.size.toString() + mContext.resources
                .getString(R.string.items)

            var grandTotal = 0.0
            if (productItems.size > 0) {
                for (i in 0 until productItems.size) {
                    grandTotal = grandTotal + productItems[i].total_price
                }

            }
            val adapter = OutletProductAdapter(productItems)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()

            tvGrandTotal.text = dformat.format(grandTotal).toString()
        } else {
            filterLayout.visibility = View.GONE

            productItems.clear()
            if (brands_array.length() > 0) {
                for (j in 0 until brands_array.length()) {
                    val brandObj = brands_array.getJSONObject(j)
                    if (statusOrder.equals("PENDING")) {
                        if (brandObj.getInt("ordered_quantity") > 0) {
                            productItems.add(
                                ProductStatistics(
                                    brandObj.getString("product_id"),
                                    brandObj.getString("product_name"),
                                    brandObj.getString("product_code"),
                                    brandObj.getString("sku_code"),
                                    brandObj.getString("category_code"),
                                    brandObj.getString("category_name"),
                                    brandObj.getString("category_id"),
                                    brandObj.getString("unit_name"),
                                    brandObj.getString("unit_id"),
                                    brandObj.getString("unit_code"),
                                    brandObj.getDouble("unit_price"),
                                    brandObj.getDouble("discounted_unit_price"),
                                    brandObj.getDouble("ordered_amount"),
                                    brandObj.getInt("ordered_quantity"),
                                    brandObj.getInt("ordered_quantity"),
                                    brandObj.getInt("bounced_quantity"),
                                    brandObj.getDouble("ordered_amount")
                                )
                            )
                        }
                    } else if (statusOrder.equals("CANCELLED")) {
                        if (brandObj.getInt("bounced_quantity") > 0) {
                            productItems.add(
                                ProductStatistics(
                                    brandObj.getString("product_id"),
                                    brandObj.getString("product_name"),
                                    brandObj.getString("product_code"),
                                    brandObj.getString("sku_code"),
                                    brandObj.getString("category_code"),
                                    brandObj.getString("category_name"),
                                    brandObj.getString("category_id"),
                                    brandObj.getString("unit_name"),
                                    brandObj.getString("unit_id"),
                                    brandObj.getString("unit_code"),
                                    brandObj.getDouble("unit_price"),
                                    brandObj.getDouble("discounted_unit_price"),
                                    brandObj.getDouble("ordered_amount"),
                                    brandObj.getInt("ordered_quantity"),
                                    brandObj.getInt("bounced_quantity"),
                                    brandObj.getInt("bounced_quantity"),
                                    brandObj.getDouble("bounced_amount")
                                )
                            )
                        }
                    }
                }
            }
            tvItemCount.text =
                productItems.size.toString() + mContext.resources.getString(R.string.items)

            var grandTotal = 0.0
            if (productItems.size > 0) {
                for (i in 0 until productItems.size) {
                    grandTotal = grandTotal + productItems[i].total_price
                }

            }
            val adapter = OutletProductAdapter(productItems)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()

            tvGrandTotal.text = dformat.format(grandTotal).toString()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
            requireActivity().onBackPressed()
        }

        if (!statusOrder.equals("null")) {
            if (statusOrder.equals("PENDING")) {
                tvOrderStatus.text = mContext.resources.getString(R.string.pending)
                statusLayout.background.setTint(mContext.resources.getColor(R.color.status_pending_stroke))
                val gd = GradientDrawable()
                gd.setColor(mContext.resources.getColor(R.color.status_pending))
                gd.cornerRadius = 5f
                gd.setStroke(2, mContext.resources.getColor(R.color.white))
                tvOrderStatus.setBackgroundDrawable(gd)
            } else if (statusOrder.equals("DELIVERED")) {
                tvOrderStatus.text = mContext.resources.getString(R.string.delivered)
                statusLayout.background.setTint(mContext.resources.getColor(R.color.status_delivered_stroke))
                val gd = GradientDrawable()
                gd.setColor(mContext.resources.getColor(R.color.status_delivered))
                gd.cornerRadius = 5f
                gd.setStroke(2, mContext.resources.getColor(R.color.white))
                tvOrderStatus.setBackgroundDrawable(gd)
            } else if (statusOrder.equals("CANCELLED")) {
                tvOrderStatus.text = mContext.resources.getString(R.string.bounced)
                statusLayout.background.setTint(mContext.resources.getColor(R.color.status_bounced_stroke))
                val gd = GradientDrawable()
                gd.setColor(mContext.resources.getColor(R.color.status_bounced))
                gd.cornerRadius = 5f
                gd.setStroke(2, mContext.resources.getColor(R.color.white))
                tvOrderStatus.setBackgroundDrawable(gd)
            } else {
                statusLayout.visibility = View.GONE
            }
        }

        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        sr_id = prefs!!.getString(Api.EMPLOYEE_ID, "")
        appDatabase = AppDatabase.getInstance(context)
        mContext = context
        listener = this

        ACTIVITY = context as MainActivity
    }

    override fun onValueChanged(products: Any, position: Int) {
        products as Products
        var itemCount = 0
        var grandTotal = 0.0
        val prodList =
            appDatabase!!.saveOrderDao().getOrdersDB(prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!)
        itemCount = prodList!![0].itemsCount
        grandTotal = prodList[0].totalPrice
        if (itemCount == 1 || itemCount == 0) {
            binding.totalItemCount.text = getString(R.string.items_, itemCount.toString())
        } else {
            binding.totalItemCount.text = getString(R.string.items_, itemCount.toString())
        }
        try {
            Log.d("Product", "addedProducts size: " + addedProducts!!.size)
            //val exists = products.product_id in arrayOf(addedProducts)
            val exists = addedProducts?.find {
                it.product_id == products.product_id
            }
            Log.d("Product", "addedProducts size: $exists")
            if (exists != null) {
                addedProducts!!.remove(exists)
                addedProducts!!.add(
                    Products(
                        products.product_id,
                        products.product_name,
                        products.product_code,
                        products.unit_price,
                        products.discounted_unit_price,
                        products.sku_code,
                        products.imageUrl,
                        products.unit_id,
                        products.unit_name,
                        products.unit_code,
                        products.category_id,
                        products.category_name,
                        products.category_code,
                        products.quantity_last_month,
                        products.stock_available,
                        products.bounced_quantity,
                        products.ordered_quantity,
                        products.ordered_total_price
                    )
                )
                Log.d("Product", "addedProducts size 2: " + addedProducts!!.size)
            } else {
                addedProducts!!.add(
                    Products(
                        products.product_id,
                        products.product_name,
                        products.product_code,
                        products.unit_price,
                        products.discounted_unit_price,
                        products.sku_code,
                        products.imageUrl,
                        products.unit_id,
                        products.unit_name,
                        products.unit_code,
                        products.category_id,
                        products.category_name,
                        products.category_code,
                        products.quantity_last_month,
                        products.stock_available,
                        products.bounced_quantity,
                        products.ordered_quantity,
                        products.ordered_total_price
                    )
                )
                Log.d("Product", "addedProducts size 3: " + addedProducts!!.size)
            }
        } catch (e: Exception) {
            Log.d("Product", "exception 2: " + e.message + " " + position)
            e.printStackTrace()
        }
        binding.totalAmount.text = getString(R.string.total_, dFormat.format(grandTotal).toString())
        totalAmount = dFormat.format(grandTotal).toString()
        grandTotalPrice = grandTotal
        totalCount = itemCount

    }

}