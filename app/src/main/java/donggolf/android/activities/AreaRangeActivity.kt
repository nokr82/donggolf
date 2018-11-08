package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import donggolf.android.R
import donggolf.android.actions.NationalAction
import donggolf.android.adapters.AreaRangeAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_area_range.*
import org.json.JSONObject

class AreaRangeActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : AreaRangeAdapter

    private  var adapterData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()

    private var mAuth: FirebaseAuth? = null

    val user = HashMap<String, Any>()
    private val actArea = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_range)

        context = this

        finishBT.setOnClickListener {
            finish()
        }

        var dataObj: JSONObject = JSONObject()//파이어베이스 사용시 필수

        mAuth = FirebaseAuth.getInstance()

        val db = FirebaseFirestore.getInstance()

        db.collection("users")
                .get()
                .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                    override fun onComplete(task: Task<QuerySnapshot>) {
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                Log.d(MainActivity.TAG, document.getId() + " => " + document.getData())
                            }
                        } else {
                            Log.w(MainActivity.TAG, "Error getting documents.", task.exception)
                        }
                    }
                })

        NationalAction.list(user,Pair("createAt",null),0) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->

            if(success && data != null) {
                data.forEach {
                    println(it)
                    println("====================================================")

                    if (it != null) {
                        adapterData.add(it)
                    }

                }

                adapter.notifyDataSetChanged()

            }

        }

        adapter = AreaRangeAdapter(context, R.layout.item_area_range, adapterData)

        listLV.adapter = adapter


        //활동 지역 범위 삭제
        area_deal1.setOnClickListener {
            hideArea(area1)
            hideArea(area_deal1)
            areaCnt.text = "지역 범위 설정 ($actArea/3)"
            //파이어베이스 사용자 활동범위 제거
        }
        area_deal2.setOnClickListener {
            hideArea(area2)
            hideArea(area_deal2)
            areaCnt.text = "지역 범위 설정 ($actArea/3)"
            //
        }
        area_deal1.setOnClickListener {
            hideArea(area3)
            hideArea(area_deal3)
            areaCnt.text = "지역 범위 설정 ($actArea/3)"
            //
        }
    }

    fun hideArea(view: View){
        view.visibility = View.GONE
    }
}
