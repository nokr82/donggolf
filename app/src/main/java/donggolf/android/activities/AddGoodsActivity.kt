package donggolf.android.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import donggolf.android.R
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_add_goods.*
import java.util.ArrayList

class AddGoodsActivity : RootActivity() {

    private val GALLERY = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goods)

        addpictureLL.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            startActivityForResult(galleryIntent, GALLERY)
        }

        finishLL.setOnClickListener {
            finish()
        }

        finishaLL.setOnClickListener {
            choiceLL.visibility = View.GONE
        }

        selectedTV.setOnClickListener {
            choiceLL.visibility = View.GONE
        }

        goodsinfoLL.setOnClickListener {
            choiceLL.visibility = View.VISIBLE
        }

        brandLL.setOnClickListener {
            choiceLL.visibility = View.VISIBLE
        }

        configRV.setOnClickListener {
            choiceLL.visibility = View.VISIBLE
        }

        areaRL.setOnClickListener {
            choiceLL.visibility = View.VISIBLE
        }

        dellRL.setOnClickListener {
            choiceLL.visibility = View.VISIBLE
        }




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                GALLERY -> {

                }

            }
        }
    }
}
