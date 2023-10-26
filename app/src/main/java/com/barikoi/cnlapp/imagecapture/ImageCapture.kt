package com.barikoi.cnlapp.imagecapture

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.preference.PreferenceManager
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.imagecapture.Model.ImageList
import com.barikoi.cnlapp.imagecapture.RoomDb.ImageDatabase
import com.barikoi.cnlapp.imagecapture.Utils.ApiCall
import io.sentry.Sentry
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageCapture(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs){
    private val GALLERY = 3
    var CAMERA = 4
    private val mContext: ImageCapture
    private val im1: ImageView? = null
    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private val layoutImagePick: LinearLayout
    private val layoutImageAdd: LinearLayout
    private val layoutImageScroller: LinearLayout
    private val fmap: HashMap<Int, String>
    private var photoURI: Uri? = null
    private val add: ImageButton
    private var mCurrentPhotoPath = ""
    private val imagePath = ""
    var taskId: String? = null
    var REQUEST_CHECK_PERMISSION = 0x1
    private val imageTitle: String? = null
    private var titleTakeImage: TextView? = null
    private val prefs: SharedPreferences
    private val editor: SharedPreferences.Editor
    private var appDatabase: ImageDatabase? = null
    private var imageRecyclerAdapter: ImageRecyclerAdapter? = null
    private val mRecyclerView: RecyclerView
    private val imageItems: ArrayList<ImageList> = ArrayList<ImageList>()
    private var startCAMERA : ActivityResultLauncher<Intent>? = null

    /**
     * SHows an AlertDialog whether to pick the image from gallery or take from camera
     */
    /*fun addpicdialog() {
        val pictureDialog = AlertDialog.Builder(activity)
        pictureDialog.setTitle(R.string.selectAction)
        val pictureDialogItems = arrayOf(
            activity!!.getString(R.string.selectgallery),
            activity!!.getString(R.string.capture_camera)
        )
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> checkGalleryPermission()
                1 -> checksCameraPermission()
            }
        }
        pictureDialog.show()
    }*/

    /**
     * Opens the gallery to pick an image
     */
    fun takeImageFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        if (activity != null) activity!!.startActivityForResult(galleryIntent, GALLERY)
    }

    /**
     * Opens the camera to take a picture
     */
    fun takePicture() {
        try {
            layoutImagePick.visibility = GONE
            layoutImageAdd.visibility = VISIBLE
            layoutImageScroller.visibility = VISIBLE
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(activity!!.packageManager) != null) {
                var photoFile: File? = null
                try {
                    taskId = taskId
                    Log.d("ImagePicker", "TaskId 2: $taskId")
                    photoFile = createImageFile(taskId)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e("fileException", ex.message!!)
                } catch (ex: Exception) {
                    Sentry.captureException(ex)
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(
                        activity!!,
                        context.packageName + ".fileprovider",
                        photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    getCameraLauncher().launch(takePictureIntent)
                }
                //activity.startActivityForResult(takePictureIntent, CAMERA);
            }
        }catch (e: Exception){
            e.printStackTrace()
            Sentry.captureException(e)
        }

    }

    fun takePictureFragment() {
        try {
        layoutImagePick.visibility = GONE
        layoutImageAdd.visibility = VISIBLE
        layoutImageScroller.visibility = VISIBLE
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(fragment!!.requireActivity().packageManager) != null) {
            var photoFile: File? = null
            try {
                taskId = taskId
                Log.d("ImagePicker", "TaskId 2: $taskId")
                photoFile = createImageFile(taskId)
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.e("fileException", ex.message!!)
            } catch (ex: Exception) {
                Sentry.captureException(ex)
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(
                    fragment!!.requireContext(),
                    context.packageName + ".fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                //fragment!!.startActivityForResult(takePictureIntent, CAMERA)
                getCameraLauncher().launch(takePictureIntent)
            }
        }
        }catch (e: Exception){
            e.printStackTrace()
            Sentry.captureException(e)
        }
    }

    fun AddNewImage(imageReturnedIntent: Intent?, source: Int, position: Int, type: String, image_path: String) {
        var bitmap: Bitmap? = null
        try {
            bitmap = getRotateImage(image_path)
            //Sentry.captureMessage("Add Image Clicked pos: "+position+" "+bitmap)
            val lt = ImageList(bitmap, prefs.getString(ApiCall.IMAGE_PATH, "")!!, position,
                type, prefs.getString(ApiCall.IMAGE_PATH, "")!!)
            imageItems.add(lt)
            imageRecyclerAdapter = ImageRecyclerAdapter(imageItems, taskId!!)
            mRecyclerView.adapter = imageRecyclerAdapter
            fmap[position] = image_path
            layoutImagePick.visibility = GONE
            layoutImageAdd.visibility = VISIBLE
            layoutImageScroller.visibility = VISIBLE
        } catch (e: IOException) {
            e.printStackTrace()
            Sentry.captureException(e)
        }
    }
    fun removeImages(){
        for (i in 0 until imageRecyclerAdapter!!.itemCount){
            imageRecyclerAdapter!!.removeAt(0)
        }

        layoutImagePick.visibility = VISIBLE
        layoutImageAdd.visibility = GONE
        layoutImageScroller.visibility = GONE
    }

    fun setLocalImage(bitmap: Bitmap?, path: String?, position: Int, filename: String?, type: String) {
        layoutImagePick.visibility = GONE
        layoutImageAdd.visibility = VISIBLE
        layoutImageScroller.visibility = VISIBLE
        val lt = ImageList(bitmap!!, path!!, position, type, filename!!)
        imageItems.add(lt)
        imageRecyclerAdapter = ImageRecyclerAdapter(imageItems, taskId!!)
        mRecyclerView.adapter = imageRecyclerAdapter
    }

    private fun ChangeVisible(
        image: ImageView,
        imageReturnedIntent: Intent,
        position: Int,
        source: Int
    ) {
        val myBitmap: Bitmap? = null
        val picUri: Uri?
        if (source == CAMERA) {

            //image_path = imageList.get(0).filePath;
            Log.d("Imagepos", "imagelist: $mCurrentPhotoPath")
            var bitmap: Bitmap? = null
            try {
                bitmap = getRotateImage(mCurrentPhotoPath)
                Log.d("Imagepos", "fmap: $bitmap")
                Log.d("Imagepos", "fmap: $photoURI")
                //image.setImageURI(photoURI);
                image.setImageBitmap(bitmap)
                image.visibility = VISIBLE
                fmap[position] = mCurrentPhotoPath
                //Log.d("Imagepos", "fmap: " +fmap.get(position));
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (source == GALLERY) {
            picUri = imageReturnedIntent.data
            mCurrentPhotoPath = getRealPathFromURI(picUri)
            image.setImageURI(picUri)
            image.visibility = VISIBLE
            fmap[position] = mCurrentPhotoPath

        }
        Log.d("imagePicker", mCurrentPhotoPath)
    }

    @Throws(IOException::class)
    fun getRotateImage(photoPath: String): Bitmap {
        Log.d("Imagepos", "imagelist: $photoPath")
        val bmOptions = BitmapFactory.Options()
        bmOptions.inSampleSize = 8
        val imageFile = File(photoPath)
        var bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, bmOptions)
        var rotate = 0
        try {
            //context.getContentResolver().notifyChange(photoURI, null);
            val exif = ExifInterface(photoPath)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            Log.d("Imagepos", "orientation: $orientation")
            Log.d("Imagepos", "orientation: " + ExifInterface.ORIENTATION_ROTATE_270)
            Log.d("Imagepos", "orientation: " + ExifInterface.ORIENTATION_ROTATE_180)
            Log.d("Imagepos", "orientation: " + ExifInterface.ORIENTATION_ROTATE_90)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
            /****** Image rotation  */
            val matrix = Matrix()
            matrix.postRotate(rotate.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            Log.d("Imagepos", "fmap: $bitmap")

            /*get lat lon from image*/
            /*val latLong = FloatArray(2)
            val hasLatLong: Boolean = exif.getLatLong(latLong)
            if (hasLatLong) {
                Log.d("Imagepos", "Longitude: $latLong")
                Log.d("Imagepos", "Latitude: " + latLong[0])
                Log.d("Imagepos", "Longitude: " + latLong[1])
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /* public HashMap<String,String> getparams(Place place){
        Map<String, String> parameters = new HashMap<>();
        parameters.put("longitude", place.getLon());
        parameters.put("latitude", place.getLat());
        parameters.put("Address", place.getAddress());
        //parameters.put("device_ID", android_id);
        parameters.put("city", place.getCity());
        parameters.put("area", place.getArea());
        parameters.put("postCode", place.getPostalcode());
        parameters.put("pType", place.getType());
        parameters.put("subType", place.getSubType());
        */
    /*  parameters.put("road_details",roadType);*/ /*
        parameters.put("flag","1");

        if(place.getPhoneNumber().length()>0)parameters.put("contact_person_phone",place.getPhoneNumber());
        if(place.getContactName().length()>0)parameters.put("contact_person_name",place.getContactName());

        if(place.getImglink().length()>0){
            parameters.put("images[0]",place.getImglink());

        }
        if(place.getTags().length()>0)
            parameters.put("tags",place.getTags());


        if (!ishouse(comp) && !isroad(comp)){
            parameters.put("name",comp);
        }
        if (ishouse(comp))
            parameters.put("holding_no", comp);
        if (isroad(comp))
            parameters.put("road_name_number",comp);

        if (ishouse(comp) && !d.containsKey("holding_no"))
            parameters.put("holding_no", comp);

        if (isroad(comp))
            parameters.put("road_name_number",comp);

        if (addresscomponents.length > i + 1)
            parameters.put("super_sub_area",comp);

        else
            d.put("sub_area",comp);


    }*/
    fun deleteFileLocal(filepath: String?) {
        if (filepath != null) {
            File(filepath).delete()
        }
    }

    fun getImagefilepath(pos: Int): String? {
        return fmap[pos]
    }

    fun getimage(): Bitmap {
        return im1!!.drawingCache
    }

    val imageCount: Int
        get() = fmap.size

    fun setMainactivity(mainactivity: Activity?) {
        activity = mainactivity
    }

    fun setFragmetnt(fragmnt: Fragment) {
        fragment = fragmnt
    }
    fun getCameraLauncher(): ActivityResultLauncher<Intent> {
        return startCAMERA!!
    }
    fun setCameraLauncher(startCam: ActivityResultLauncher<Intent>) {
        startCAMERA = startCam
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        return if (maxSize > width && maxSize > height) {
            image
        } else {
            val bitmapRatio = width.toFloat() / height.toFloat()
            if (bitmapRatio > 1) {
                width = maxSize
                height = (width / bitmapRatio).toInt()
            } else {
                height = maxSize
                width = (height * bitmapRatio).toInt()
            }
            Bitmap.createScaledBitmap(image, width, height, true)
        }
    }

    fun checksCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("MyApp", "SDK >= 23")
            if (activity!!.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyApp", "Request permission")
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.CAMERA)) {
                    AlertDialog.Builder(activity)
                        .setMessage("Barikoi needs permission to take photos from your camera")
                        .setPositiveButton(R.string.ok,
                            DialogInterface.OnClickListener { dialog, which ->
                                ActivityCompat.requestPermissions(
                                    activity!!, arrayOf(Manifest.permission.CAMERA),
                                    PERMISSION_CAMERA
                                )
                            })
                        .setNegativeButton(R.string.cancel,
                            DialogInterface.OnClickListener { dialog, which -> })
                        .create()
                        .show()
                } else {
                    ActivityCompat.requestPermissions(
                        activity!!, arrayOf(Manifest.permission.CAMERA),
                        PERMISSION_CAMERA
                    )
                }
            } else {
                Log.d("MyApp", "SDK = "+Build.VERSION.SDK_INT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (fragment != null){
                        takePictureFragment()
                    }else{
                        takePicture();
                    }
                }else{
                    if (activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(
                            activity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            0)
                    }else{
                        if (activity!!.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(
                                activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                            )
                        }else{
                            if (fragment != null){
                                takePictureFragment()
                            }else{
                                takePicture();
                            }
                        }
                    }

                }


            }
        } else {
            Log.d("MyApp", "Android < 6.0")
            Log.d("MyApp", "Permission granted: taking pic")
            if (fragment != null){
                takePictureFragment()
            }else{
                takePicture();
            }

        }
    }

    fun checkGalleryPermission() {
        // Should we show an explanation?
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(activity)
                    .setMessage("Barikoi needs external storage read permission to access gallery photos")
                    .setPositiveButton(R.string.ok,
                        DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(
                                activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                            )
                        })
                    .setNegativeButton(R.string.cancel,
                        DialogInterface.OnClickListener { dialog, which -> })
                    .create()
                    .show()
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            takeImageFromGallery()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(task_id: String?): File {
        // Create an image file name
        Log.d("ImagePicker", "TaskId 3: $task_id")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        //        String imageFileName = "JPEG_" + timeStamp + "_";
        val imageFileName = "Task" + task_id + "_" + timeStamp + "_"
        /*val image = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "CNL_$timeStamp.jpg"
        )*/
        var baseFolder = ""
        if (fragment != null) {
            baseFolder = fragment!!.requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
        }else{
            baseFolder = activity!!.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
        }
        val image = File(baseFolder +File.separator+"CNL Documents"+File.separator+"CNL_$timeStamp.jpg")
        Log.d("ImagePicker", "Image: $image")
        image.parentFile.mkdirs()
        mCurrentPhotoPath = image.absolutePath
        editor.putString(ApiCall.IMAGE_PATH, mCurrentPhotoPath)
        editor.apply()
        Log.d("ImagePicker", mCurrentPhotoPath)
        return image
    }

    @SuppressLint("Range")
    fun getRealPathFromURI(contentUri: Uri?): String {
        var cursor = activity!!.contentResolver.query(contentUri!!, null, null, null, null)
        cursor!!.moveToFirst()
        var document_id = cursor.getString(0)
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
        cursor.close()
        cursor = activity!!.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Images.Media._ID + " = ? ",
            arrayOf(document_id),
            null
        )
        cursor!!.moveToFirst()
        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor.close()
        return path
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5
        private const val PERMISSION_CAMERA = 6
    }

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.imagecapture, this)
        mContext = this
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext())
        editor = prefs.edit()
        layoutImageAdd = findViewById(R.id.layoutImageAdd)
        layoutImagePick = findViewById(R.id.layoutImagePick)
        layoutImageScroller = findViewById(R.id.imageScrollerPick)
        titleTakeImage = findViewById(R.id.titleTakeImage)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView = findViewById(R.id.recyclerImage)
        mRecyclerView.layoutManager = layoutManager
        appDatabase = ImageDatabase.getInstance(context!!)
        layoutImagePick.setOnClickListener {
            checksCameraPermission()
        }
        add = findViewById<View>(R.id.imageButtonadd) as ImageButton
        add.setOnClickListener { //addpicdialog();
            checksCameraPermission()
        }
        fmap = HashMap()
    }
}