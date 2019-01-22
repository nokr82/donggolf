package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.RegionAction
import donggolf.android.adapters.AreaRangeAdapter
import donggolf.android.adapters.AreaRangeGridAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
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

    var sidotype = ""
    var sidotype2 = ""
    var goguntype = ""
    var goguntype2 = ""
    var region_id = ""
    var region_id2 = ""

    lateinit var type :String

    var areacount = 0

    var bigcitylist: ArrayList<JSONObject> = ArrayList<JSONObject>()

    var gugunList: ArrayList<JSONObject> = ArrayList<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_range)

        context = this

        mAuth = FirebaseAuth.getInstance()

        intent = getIntent()
        type = intent.getStringExtra("region_type")//content_filter

        getBigCity()

        adapter = AreaRangeAdapter(context,R.layout.item_dlg_market_sel_op,bigcitylist)
        arealistLV.adapter = adapter
        arealistLV.setOnItemClickListener { parent, view, position, id ->
            val item = bigcitylist.get(position)
            var type = item.getJSONObject("Regions")
            var id  = Utils.getString(type,"id")

            if (areacount == 0){
                sidotype = Utils.getString(type,"name")
            } else if (areacount == 1){
                sidotype2 = Utils.getString(type,"name")
            }

            getGugun(id.toInt())
            arealistLV.visibility = View.GONE
            gridGV.visibility = View.VISIBLE
//
//            if (Utils.getString(type,"name") == "세종특별자치시") {
//                var intent = Intent();
//                intent.putExtra("sidotype", sidotype)
//                intent.putExtra("goguntype", sidotype)
//                intent.putExtra("region_id", id)
//                setResult(Activity.RESULT_OK, intent)
//                finish()
//            } else if (Utils.getString(type,"name") == "전국"){
//                var intent = Intent();
//                intent.putExtra("sidotype", sidotype)
//                intent.putExtra("goguntype", sidotype)
//                intent.putExtra("region_id", id)
//                setResult(Activity.RESULT_OK, intent)
//                finish()
//            }
        }

        GridAdapter = AreaRangeGridAdapter(context, R.layout.item_area_range_grid, gugunList)
        gridGV.adapter = GridAdapter
        gridGV.setOnItemClickListener { parent, view, position, id ->
            val item = gugunList.get(position)
            var type = item.getJSONObject("Regions")
            var name:String = Utils.getString(type,"name")
            var index = areaCnt.text.toString().toInt()
            var nowIndex = index + 1
            if (areacount == 0){
                region_id = Utils.getString(type,"id")
                goguntype = name
                Toast.makeText(context, "지역하나를 더 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else if (areacount == 1){
                region_id2 = Utils.getString(type,"id")
                goguntype2 = name
            }
            areacount++

            arealistLV.visibility = View.VISIBLE
            gridGV.visibility = View.GONE

            if (areacount == 2) {
                Toast.makeText(context, "활동지역 정보를 성공적으로 변경했습니다.", Toast.LENGTH_SHORT).show()
                var intent = Intent();
                intent.putExtra("sidotype", sidotype)
                intent.putExtra("sidotype2", sidotype2)
                intent.putExtra("goguntype", goguntype)
                intent.putExtra("goguntype2", goguntype2)
                intent.putExtra("region_id", region_id)
                intent.putExtra("region_id2", region_id2)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

        }

        finishLL.setOnClickListener {
        /*    if(arealistLV.visibility == View.VISIBLE){
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
            }*/
finish()

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


    fun getBigCity(){

        val params = RequestParams()

        RegionAction.api_sido(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val datalist = response!!.getJSONArray("sido")

                if (datalist.length() > 0 && datalist != null){
                    for (i in 0 until datalist.length()){
                        bigcitylist.add(datalist.get(i) as JSONObject)
                        bigcitylist[i].put("isSelectedOp",false)
                    }

                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
            }

        })

    }

    fun getGugun(position: Int){
        if (gugunList != null){
            gugunList.clear()
        }

        val params = RequestParams()
        params.put("sido",position)

        RegionAction.api_gugun(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                var datalist = response!!.getJSONArray("gugun")

                tmpSV.visibility = View.VISIBLE

                if (datalist.length() > 0 && datalist != null){
                    for (i in 0 until datalist.length()){
                        gugunList.add(datalist.get(i) as JSONObject)
                        gugunList[i].put("isSelectedOp",false)
                    }
                    GridAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                Toast.makeText(context, "불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
