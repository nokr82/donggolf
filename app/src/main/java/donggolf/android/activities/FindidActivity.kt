package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.InfoAction
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_findid.*

class FindidActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findid)

        context = this

        mAuth = FirebaseAuth.getInstance()

        finishBT.setOnClickListener {
            finish()
        }

        findBT.setOnClickListener {
            findid()
        }

    }

    fun findid(){

        val phone = Utils.getString(phoneET)
        if (phone.isEmpty()) {
            Utils.alert(context, "빈칸은 입력하실 수 없습니다.")
            return
        }


        val params = HashMap<String, Any>()
         params.put("phone", phone)

        InfoAction.list(params, Pair("phone",  Query.Direction.DESCENDING),1){ success: Boolean, data: ArrayList<Map<String, Any>?>?, exception: Exception? ->
            if(success && data != null) {
                data.forEach {
                    println(it)
                }
            }


        }

                InfoAction.findId("phone", params){ success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                    if(success) {
                        println("data : $data")
                        useridTV.text = data.toString()

                    } else {

                    }

        }


    }

}
