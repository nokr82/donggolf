package donggolf.android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_add_goods.*
import java.util.ArrayList

class AddGoodsActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goods)

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
}
