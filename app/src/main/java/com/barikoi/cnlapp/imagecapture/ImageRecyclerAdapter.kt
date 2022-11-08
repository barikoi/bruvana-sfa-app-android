package com.barikoi.cnlapp.imagecapture

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.imagecapture.Model.ImageList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.imagecapture.RoomDb.AppDatabase
import com.barikoi.cnlapp.imagecapture.Utils.ApiCall
import com.barikoi.cnlapp.imagecapture.RoomDb.Images
import java.io.File

class ImageRecyclerAdapter(private val  mValues: ArrayList<ImageList>, private val taskId: String): RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder>(){

    lateinit  var prefs: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.single_image_view, parent, false)

        Log.d("ImageAdapter", "mvalues 1: " +mValues.size)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mValues.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        prefs = PreferenceManager.getDefaultSharedPreferences(holder.mView.context)
        editor = prefs.edit()

        val item = mValues[position]
        Log.d("ImageAdapter", "selected pos 1: " +item.selectedPos)
        Log.d("ImageAdapter", "selected filename 1: " +item.selectedFileName)
        holder.imageView.setImageBitmap(item.selectedBitmap)
        holder.imageView.setOnClickListener { view ->
            val nagDialog = Dialog(holder.mView.context, android.R.style.Theme_NoTitleBar_Fullscreen)
            nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            nagDialog.setCancelable(false)
            nagDialog.setContentView(R.layout.preview_image_withdelete)
            val btnClose: Button = nagDialog.findViewById(R.id.btnIvClose)
            val btnDelete: Button = nagDialog.findViewById(R.id.btnIvDelete)
            val tvFileName: TextView = nagDialog.findViewById(R.id.tvFilename)
            tvFileName.text = item.selectedFileName
            tvFileName.visibility = View.GONE
            val ivPreview: ImageView = nagDialog.findViewById(R.id.ic_preview_image)
            ivPreview.setImageBitmap(item.selectedBitmap)
            btnClose.setOnClickListener { view1: View? -> nagDialog.dismiss() }
            btnDelete.setOnClickListener { view1: View? ->
                removeAt(holder.adapterPosition)
                File(item.selectedPath).delete()
                editor.putString(ApiCall.IMAGE_PATH, "")
                //editor.putString(Api.FILE_NAME, "")
                editor.apply()
                Log.d("ImageAdapter", "selected pos: " +item.selectedPos)
                AppDatabase.getInstance(holder.mView.context)!!.imagesDao()!!.deleteImage(item.selectedPos)
                updateAt(holder.mView.context, item.selectedPos)

                nagDialog.dismiss()
                holder.itemView.visibility = View.GONE

            }
            nagDialog.show()
        }

    }

    fun updateAt(context: Context, position: Int){
        val imagesList: List<Images> = AppDatabase.getInstance(context)!!.imagesDao()!!.getImageDBPos(position) as List<Images>
        Log.d("ImageAdapter", "update pos: " + imagesList.size)
        if ( imagesList.size > 0) {
            for (i in 0 until  imagesList.size) {
                Log.d("ImageAdapter", "update pos i: " +i)
                AppDatabase.getInstance(context)!!.imagesDao()!!.updatePosition(imagesList[i].filePath, i + 1)
                if (i + 1 ==  imagesList.size) {
                    editor.putInt(ApiCall.IMAGE_POSITION, i + 1)
                    editor.apply()
                }
            }
        } else {
            editor.putInt(ApiCall.IMAGE_POSITION, 0)
            editor.apply()
        }

    }

    fun removeAt(position: Int) {
        mValues.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mValues.size)
    }

    inner class ViewHolder(internal val mview: View): RecyclerView.ViewHolder(mview) {
        internal val mView : View
        internal val imageView: ImageView
        //internal val tvFileName: TextView
        init {
            mView = mview
            imageView = mview.findViewById(R.id.imageView)
            //tvFileName = mview.findViewById(R.id.tvFilename)
        }
    }
}