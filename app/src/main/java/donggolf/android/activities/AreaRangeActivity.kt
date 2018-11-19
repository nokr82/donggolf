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
import donggolf.android.adapters.AreaRangeAdapter
import donggolf.android.adapters.AreaRangeGridAdapter
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.RootActivity
import donggolf.android.models.National
import donggolf.android.models.NationalGrid
import kotlinx.android.synthetic.main.activity_area_range.*
class AreaRangeActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : AreaRangeAdapter

    private lateinit var GridAdapter : AreaRangeGridAdapter

    private var mAuth: FirebaseAuth? = null

    val user = HashMap<String, Any>()
    private val actArea = 3

    var arealist: ArrayList<National> = ArrayList<National>()

    var areaGridList: ArrayList<NationalGrid> = ArrayList<NationalGrid>()

    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_range)

        context = this

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

        FirebaseFirestoreUtils.get("settings","national"){ success, data, exception ->

            var data:HashMap<String, Any> = data as HashMap<String, Any>
            for (i in 0..(data.size - 1)) {
                var sido:HashMap<String, Any>  = data.get(i.toString()) as HashMap<String, Any>

                val iterator = sido.entries.iterator()

                while (iterator.hasNext()){

                    var entry = iterator.next() as java.util.Map.Entry<String, ArrayList<java.util.Map.Entry<String,Long>>>

                    var datas = National(entry.key,entry.value)

                    arealist.add(datas)

                }

            }

            adapter.notifyDataSetChanged()

        }

        adapter = AreaRangeAdapter(context, R.layout.item_area_range, arealist)

        arealistLV.adapter = adapter

        arealistLV.itemsCanFocus = true
        arealistLV.setOnItemClickListener{ parent, view, position, id ->

            gridGV.visibility = View.VISIBLE

            arealistLV.visibility = View.GONE

            val item = arealist.get(position)

            println("item :========= ${item.national}")

            if(areaGridList.size > 1) {
                areaGridList.clear()
                for (i in 0..item.national.size-1){

                    var data:HashMap<String, Long> = item.national.get(i) as HashMap<String, Long>

                    val iterator = data.entries.iterator()

                    while (iterator.hasNext()){

                        var entry = iterator.next() as java.util.Map.Entry<String, Long>

                        var datas = NationalGrid(entry.key,entry.value)

                        areaGridList.add(datas)

                    }

                }
            }else {
            for (i in 0..item.national.size-1){

                var data:HashMap<String, Long> = item.national.get(i) as HashMap<String, Long>

                val iterator = data.entries.iterator()

                while (iterator.hasNext()){

                    var entry = iterator.next() as java.util.Map.Entry<String, Long>

                    var datas = NationalGrid(entry.key,entry.value)

                    areaGridList.add(datas)

                }

            }
            }

            GridAdapter.notifyDataSetChanged()


        }

        GridAdapter = AreaRangeGridAdapter(context,R.layout.item_area_range_grid,areaGridList)

        gridGV.adapter = GridAdapter

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

        finishLL.setOnClickListener {
            if(arealistLV.visibility == View.VISIBLE){
                finish()
            }

            if(gridGV.visibility == View.VISIBLE){
                arealistLV.visibility = View.VISIBLE
                gridGV.visibility = View.GONE
            }
        }
    }

    fun hideArea(view: View){
        view.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()

    }
}
