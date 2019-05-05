package donggolf.android.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.provider.MediaStore
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import donggolf.android.R
import donggolf.android.base.Config
import donggolf.android.base.Utils
import org.json.JSONObject

class GoodsImageAdapter(private val context: Context, private var pictures: ArrayList<JSONObject>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class PictureItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pictureIV = itemView.findViewById<View>(R.id.addedImgIV) as ImageView
        var pictureDelIV = itemView.findViewById<View>(R.id.delIV) as ImageView
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.item_addgoods, parent, false) as View

        itemView.layoutParams.height = Utils.dpToPx(86f).toInt()

        return PictureItemHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val holder = holder as PictureItemHolder

        if(position >= pictures.size) {

            holder.pictureIV.setImageBitmap(null)
            holder.pictureIV.visibility = View.GONE
            holder.pictureDelIV.visibility = View.GONE

            return
        }

        val picture = pictures.get(position)

        val image_uri = Utils.getString(picture, "image_uri")
        val id = Utils.getInt(picture, "id")
        val path = Utils.getString(picture, "path")
        val mediaType = Utils.getInt(picture, "mediaType")

        when {
            mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE && path != null && path != "" -> {
                val bitmap = Utils.getImage(context.contentResolver, path)
                holder.pictureIV.setImageBitmap(bitmap)
            }
            image_uri.isNotEmpty() || image_uri != "" -> {
                val bitmap = Config.url + image_uri
                ImageLoader.getInstance().displayImage(bitmap, holder.pictureIV, Utils.UILoptions)
            }
        }

        holder.pictureDelIV.tag = position
        holder.pictureDelIV.setOnClickListener {

            val tag = it.tag as Int

            val builder = AlertDialog.Builder(context)
            builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        pictures.removeAt(tag)

                        notifyDataSetChanged()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        holder.pictureIV.visibility = View.VISIBLE
        holder.pictureDelIV.visibility = View.VISIBLE
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = 9

    override fun getItemViewType(position: Int): Int {
        return position
    }
}