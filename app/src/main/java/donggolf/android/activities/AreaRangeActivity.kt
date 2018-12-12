package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.adapters.AreaRangeAdapter
import donggolf.android.adapters.AreaRangeGridAdapter
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.National
import donggolf.android.models.NationalGrid
import kotlinx.android.synthetic.main.activity_area_range.*
import kotlinx.android.synthetic.main.item_area.view.*
import org.json.JSONException
import org.json.JSONObject

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

    lateinit var type :String

    var arealist: ArrayList<National> = ArrayList<National>()

    var areaGridList: ArrayList<NationalGrid> = ArrayList<NationalGrid>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_range)

        context = this

        mAuth = FirebaseAuth.getInstance()

        adapter = AreaRangeAdapter(context, R.layout.item_area_range, arealist)

        intent = getIntent()
        type = intent.getStringExtra("region_type")//content_filter

        arealistLV.adapter = adapter


        FirebaseFirestoreUtils.get("settings","national"){ success, data, exception ->
            if (success) {

                for (i in 0..(data!!.size - 1)) {
                    var sido: HashMap<String, Any> = data?.get(i.toString()) as HashMap<String, Any>

                    // println("sido ==============================$sido")

                    val iterator = sido.entries.iterator()

                    var j = 0

                    while (iterator.hasNext()) {

                        var entry = iterator.next() as java.util.Map.Entry<String, ArrayList<java.util.HashMap<String, Long>>>

                        var national = National(entry.key, entry.value)
                        national.title = entry.key
                        national.national = entry.value


                        arealist.add(national)

                    }

                    adapter.notifyDataSetChanged()
                }

                if (type == "my_profile") {
                    tempMyActRegion()
                }

                /*var data:HashMap<String, Any> = data as HashMap<String, Any>
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


                        }*/

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

        GridAdapter = AreaRangeGridAdapter(context, R.layout.item_area_range_grid, areaGridList)
        gridGV.adapter = GridAdapter
        gridGV.setOnItemClickListener { parent, view, position, id ->

            /*val j = areaGridList.get(position)

            println(j)*/

            var area = areaGridList[position].title

            println("선택된 gridGV ::: $area")

            if (actArea < 3) {
                areaGridList[position].isSel = !areaGridList[position].isSel
                actArea ++
                val regionView = View.inflate(context, R.layout.item_area,null)
                regionView.regionNameTV.text = area

                when (actArea) {
                    1 -> userRG1 = area.toString()
                    2 -> userRG2 = area.toString()
                    3 -> userRG3 = area.toString()
                }

                regionView.regionDelIV.setOnClickListener {
                    when (actArea) {
                        1 -> userRG1 = ""
                        2 -> userRG2 = ""
                        3 -> userRG3 = ""
                    }
                    actArea--

                    areaCnt.text = "지역 범위 설정 ($actArea/3)"
                    tmpRegionLL.removeView(regionView)
                }

                tmpRegionLL.addView(regionView)
                        //actArea = count
                //actArea = count
                areaCnt.text = "지역 범위 설정 ($actArea/3)"

            } else {
                Toast.makeText(context, "활동지역 설정은 최대 3개까지 가능합니다", Toast.LENGTH_SHORT).show()
            }

            GridAdapter.notifyDataSetChanged()

        }

        finishLL.setOnClickListener {
            if(arealistLV.visibility == View.VISIBLE){
                //여기에 db 데이터 업데이트
                val params = RequestParams()
                params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
                params.put("type", "region")
                params.put("region1", userRG1)
                params.put("region2", userRG2)
                params.put("region3", userRG3)


                MemberAction.update_info(params, object : JsonHttpResponseHandler(){
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        try {
                            val result = response!!.getString("result")
                            println("AreaRangeActivity save changed data :: $response")
                            if (result == "ok") {
                                Toast.makeText(context, "활동지역 정보를 성공적으로 변경했습니다.", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK,intent)
                                finish()
                            }
                        } catch (e : JSONException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                        Toast.makeText(context, "지역 변경 실패", Toast.LENGTH_SHORT).show()
                    }
                })

            }

            if(gridGV.visibility == View.VISIBLE){
                arealistLV.visibility = View.VISIBLE
                gridGV.visibility = View.GONE
            }
        }
    }

    fun tempMyActRegion() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val member = response.getJSONObject("Member")
                        tmpRegionLL.removeAllViews()

                        val region1 = Utils.getString(member,"region1")
                        if (!region1.isEmpty()) {
                            val regionView = View.inflate(context, R.layout.item_area,null)
                            regionView.regionNameTV.text = region1
                            userRG1 = region1
                            actArea++

                            regionView.regionDelIV.setOnClickListener {
                                userRG1 = ""
                                actArea--
                                println("userRG1 : $userRG1, actArea : $actArea")
                                areaCnt.text = "지역 범위 설정 ($actArea/3)"
                                tmpRegionLL.removeView(regionView)
                            }

                            tmpRegionLL.addView(regionView)
                        }

                        val region2 = Utils.getString(member,"region2")
                        if (!region2.isEmpty()) {
                            val regionView = View.inflate(context, R.layout.item_area,null)
                            regionView.regionNameTV.text = region2
                            userRG2 = region2
                            actArea++

                            regionView.regionDelIV.setOnClickListener {
                                userRG2 = ""
                                actArea--
                                println("userRG2 : $userRG2, actArea : $actArea")
                                areaCnt.text = "지역 범위 설정 ($actArea/3)"
                                tmpRegionLL.removeView(regionView)
                            }

                            tmpRegionLL.addView(regionView)
                        }

                        val region3 = Utils.getString(member,"region3")
                        if (!region3.isEmpty()) {
                            val regionView = View.inflate(context, R.layout.item_area,null)
                            regionView.regionNameTV.text = region3
                            userRG3 = region3
                            actArea++

                            regionView.regionDelIV.setOnClickListener {
                                userRG3 = ""
                                actArea--
                                println("userRG3 : $userRG3, actArea : $actArea")
                                areaCnt.text = "지역 범위 설정 ($actArea/3)"
                                tmpRegionLL.removeView(regionView)
                            }

                            tmpRegionLL.addView(regionView)
                        }

                        areaCnt.text = "지역 범위 설정 ($actArea/3)"

                    } else {

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

            }
        })
    }
}
