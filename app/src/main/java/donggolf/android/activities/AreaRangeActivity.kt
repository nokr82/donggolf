package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import donggolf.android.R
import donggolf.android.adapters.AreaRangeAdapter
import donggolf.android.adapters.AreaRangeGridAdapter
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.models.National
import donggolf.android.models.NationalGrid
import kotlinx.android.synthetic.main.activity_area_range.*
import kotlinx.android.synthetic.main.item_area_range_grid.view.*

class AreaRangeActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : AreaRangeAdapter

    private lateinit var GridAdapter : AreaRangeGridAdapter

    private var mAuth: FirebaseAuth? = null

    val user = HashMap<String, Any>()
    var actArea = 0
    var userRG1 = ""
    var userRG2 = ""
    var userRG3 = ""
    var rg1 = 0L
    var rg2 = 0L
    var rg3 = 0L
    var count = 0

    var arealist: ArrayList<National> = ArrayList<National>()

    var areaGridList: ArrayList<NationalGrid> = ArrayList<NationalGrid>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_range)

        context = this

        mAuth = FirebaseAuth.getInstance()

        val db = FirebaseFirestore.getInstance()
        var uid = PrefUtils.getStringPreference(context, "uid")

        adapter = AreaRangeAdapter(context, R.layout.item_area_range, arealist)

        arealistLV.adapter = adapter

        //하는게 뭔지
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
            if (success) {
                var data:HashMap<String, Any> = data as HashMap<String, Any>
                db.collection("infos")
                        .document(uid)
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {

                                var document = it.result
                                //println("값 가져오기 테스트 :: ${document.get("cont_region1")}")
                                rg1 = document.get("cont_region1").toString().toLong()
                                rg2 = document.get("cont_region2").toString().toLong()
                                rg3 = document.get("cont_region3").toString().toLong()

                            }
                            println("rg :: $rg1, $rg2, $rg3")

                            for (i in 0..(data.size - 1)) {
                                var sido:HashMap<String, Any>  = data.get(i.toString()) as HashMap<String, Any>

                                // println("sido ==============================$sido")

                                val iterator = sido.entries.iterator()

                                var j = 0

                                while (iterator.hasNext()){

                                    var entry = iterator.next() as java.util.Map.Entry<String, ArrayList<java.util.HashMap<String,Long>>>

                                    var national = National(entry.key,entry.value)
                                    national.title = entry.key
                                    national.national = entry.value

                                    arealist.add(national)

                                }

                                adapter.notifyDataSetChanged()

                            }

                            //지역 정보 출력
                            for(national in arealist) {
                                val key = national.title
                                val na = national.national

                                for(n in na) {

                                    // println("$key :: $n")

                                    val keys = n.keys.iterator()
                                    if(keys.hasNext()) {
                                        val key2 = keys.next()
                                        val value = n.get(key2)

                                        //println("$key ::: $key2 ::: $value")
                                        if (rg1 == value) {
                                            userRG1 = key2

                                            if (!userRG1.equals("")){
                                                areaLL1.visibility = View.VISIBLE
                                                area1.setText(userRG1)
                                                actArea += 1

                                                //println("actArea ===== $actArea in $key2")
                                            }
                                            //println("first region name :: $key2")
                                        } else if (rg2 == value) {
                                            userRG2 = key2
                                            if (!userRG2.equals("")){
                                                areaLL2.visibility = View.VISIBLE
                                                area2.setText(userRG2)
                                                actArea += 1
                                                //println("actArea ===== $actArea in $key2")
                                            }
                                            //println("$key2")
                                        } else if (rg3 == value) {
                                            userRG3 = key2
                                            if (!userRG3.equals("")){
                                                areaLL3.visibility = View.VISIBLE
                                                area3.setText(userRG3)
                                                actArea += 1
                                                //println("actArea ===== $actArea in $key2")
                                            }
                                            //println("$key2")
                                        }

                                        if (userRG1.equals("")){
                                            areaLL1.visibility = View.GONE
                                        }
                                        if (userRG2.equals("")){
                                            areaLL2.visibility = View.GONE
                                        }
                                        if (userRG3.equals("")){
                                            areaLL3.visibility = View.GONE
                                        }

                                        areaCnt.text = "지역 범위 설정 ($actArea/3)"

                                    }

                                }
                            }


                        }

            }
        }

        arealistLV.itemsCanFocus = true
        arealistLV.setOnItemClickListener{ parent, view, position, id ->

            gridGV.visibility = View.VISIBLE

            arealistLV.visibility = View.GONE

            val item = arealist.get(position)

            println("item :========= ${item.national}")

            if(areaGridList.size > 1) {
                areaGridList.clear()
                for (i in 0..item.national.size-1){

                    var data:HashMap<String, Long> = item.national.get(i)

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
        gridGV.setOnItemClickListener { parent, view, position, id ->

            var area = areaGridList.get(position).title

            areaGridList.get(position).isSel = !areaGridList.get(position).isSel

            println("선택된 gridGV ::: $area")
            if (actArea == 0) {
                if (areaLL1.visibility == View.GONE){
                    areaLL1.visibility = View.VISIBLE
                    area1.setText(area)
                    actArea++
                }
            }
            else if (actArea == 1) {
                if (areaLL2.visibility == View.GONE){
                    areaLL2.visibility = View.VISIBLE
                    area2.setText(area)
                    actArea++
                }
            }
            else if (actArea == 2) {
                if (areaLL3.visibility == View.GONE){
                    areaLL3.visibility = View.VISIBLE
                    area3.setText(area)
                    actArea++
                }
            }
            else
                Toast.makeText(context, "활동지역 설정은 최대 3개까지 가능합니다", Toast.LENGTH_SHORT).show()
            //actArea = count
            areaCnt.text = "지역 범위 설정 ($actArea/3)"

            GridAdapter.notifyDataSetChanged()

        }



        //활동 지역 범위 삭제
        area_deal1.setOnClickListener {
            actArea -= 1
            hideArea(area1)
            hideArea(area_deal1)
            areaCnt.text = "지역 범위 설정 ($actArea/3)"
            //파이어베이스 사용자 활동범위 제거
        }
        area_deal2.setOnClickListener {
            actArea -= 1
            hideArea(area2)
            hideArea(area_deal2)
            areaCnt.text = "지역 범위 설정 ($actArea/3)"
            //
        }
        area_deal3.setOnClickListener {
            actArea -= 1
            hideArea(area3)
            hideArea(area_deal3)
            areaCnt.text = "지역 범위 설정 ($actArea/3)"
            //
        }

        finishLL.setOnClickListener {
            if(arealistLV.visibility == View.VISIBLE){
                //여기에 db 데이터 업데이트

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
