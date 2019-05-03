package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_area.view.*
import org.json.JSONArray
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

        titleTV.text = "동네탐방하기"

        accTV.visibility = View.GONE

        myTV.visibility = View.VISIBLE

        getBigCity()

        myTV.setOnClickListener {
            intent.putExtra("region_id", "no")
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        adapter = AreaRangeAdapter(context,R.layout.item_dlg_market_sel_op,bigcitylist)
        arealistLV.adapter = adapter
        arealistLV.setOnItemClickListener { parent, view, position, id ->
            val item = bigcitylist.get(position)
            var type = item.getJSONObject("Regions")
            var id  = Utils.getString(type,"id")
            var name:String = Utils.getString(type,"name")
            myTV.visibility = View.GONE

            if (areacount == 0){
                sidotype = Utils.getString(type,"name")
            } else if (areacount == 1){
                sidotype2 = Utils.getString(type,"name")
            }

            if (Utils.getString(type,"name") == "세종특별자치시") {
                intent.putExtra("sidotype", sidotype)
                intent.putExtra("goguntype", sidotype)
                intent.putExtra("region_id", id)
                intent.action = "SET_REGION"
                sendBroadcast(intent)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else if (Utils.getString(type,"name") == "전국"){
                var intent = Intent();
                intent.putExtra("sidotype", sidotype)
                intent.putExtra("goguntype", sidotype)
                intent.putExtra("region_id", id)
                intent.action = "SET_REGION"
                sendBroadcast(intent)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                getGugun(id.toInt())
                arealistLV.visibility = View.GONE
                gridGV.visibility = View.VISIBLE
            }


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

                Toast.makeText(context, "활동지역 정보를 성공적으로 변경했습니다.", Toast.LENGTH_SHORT).show()
                var intent = Intent();
                intent.putExtra("sidotype", sidotype)
                intent.putExtra("goguntype", goguntype)
                intent.putExtra("region_id", region_id)
//                PrefUtils.setPreference(context, "region_id", region_id)
                intent.action = "SET_REGION"
                sendBroadcast(intent)
                setResult(Activity.RESULT_OK, intent)
                finish()

//                Toast.makeText(context, "지역하나를 더 선택해주세요.", Toast.LENGTH_SHORT).show()
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
//                intent.putExtra("sidotype2", sidotype2)
                intent.putExtra("goguntype", goguntype)
//                intent.putExtra("goguntype2", goguntype2)
                intent.putExtra("region_id", region_id)
//                intent.putExtra("region_id2", region_id2)
                intent.action = "SET_REGION"
                sendBroadcast(intent)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

        }

        finishLL.setOnClickListener {
            if (gridGV.visibility == View.VISIBLE){
                myTV.visibility = View.VISIBLE
                arealistLV.visibility = View.VISIBLE
                gridGV.visibility = View.GONE
            } else {
                finish()
            }

        }
        my_member_cnt()
    }



    fun getBigCity(){

        val params = RequestParams()

        RegionAction.api_sido(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val datalist = response!!.getJSONArray("sido")
                Log.d("리스트",response.toString())
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

                Log.d("리스트2",response.toString())
//                tmpSV.visibility = View.VISIBLE

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
    //지역별멤버수
    fun my_member_cnt() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MemberAction.my_membercnt(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    Log.d("지역멤버",response.toString())
                    val result = response!!.getString("result")




                    if (result == "ok") {


                        val region1 = response.getJSONObject("region1")
                        val region2 = response.getJSONObject("region2")
                        val region3 = response.getJSONObject("region3")

                        var r_name1 = Utils.getString(region1,"name")
                        var r_name2 = Utils.getString(region2,"name")
                        var r_name3 = Utils.getString(region3,"name")

                        var region1_name = Utils.getString(region1,"region_name")
                        var region2_name = Utils.getString(region2,"region_name")
                        var region3_name = Utils.getString(region3,"region_name")
                        var region = ""

                        if (r_name1 != null && r_name1 != "") {
                            if (region1_name.contains("시")){
                                region += region1_name+"<"+r_name1
                            }else{
                                region += r_name1
                            }
                        }

                        if (r_name2 != null && r_name2 != "") {
                            if (region2_name.contains("시")){
                                region += ","+region2_name+"<"+r_name2
                            }else{
                                region +=","+ r_name2
                            }
                        }

                        if (r_name3 != null && r_name3 != "") {
                            if (region3_name.contains("시")){
                                region += ","+region3_name+"<"+r_name3
                            }else{
                                region +=","+ r_name3
                            }
                        }

                        if (r_name1 == "전국") {
                            region = "전국"
                        }

                        var membercnt = response!!.getString("membercnt")
                        myTV.text = "우리동네"+"("+membercnt+")"+"   "+region
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
//                println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONArray?) {
//                println(errorResponse)
            }
        })
    }

}
