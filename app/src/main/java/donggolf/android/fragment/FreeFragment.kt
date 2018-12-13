package donggolf.android.fragment

import android.os.Bundle
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.firestore.Query
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.PostAction
import donggolf.android.activities.*
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.fragment_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

open class FreeFragment : Fragment() {

    var ctx: Context? = null

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  lateinit var  adapter : MainAdapter
    private  lateinit var  editadapter : MainEditAdapter
    private  var editadapterData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()

    val user = HashMap<String, Any>()

    lateinit var main_edit_listview: ListView
    lateinit var addpostLL : LinearLayout
    lateinit var main_edit_search : EditText
    lateinit var main_listview_search : LinearLayout
    lateinit var main_edit_close : TextView
    lateinit var main_listview:ListView

    private var progressDialog: ProgressDialog? = null

    lateinit var activity: MainActivity
    var tabType = 1
    var member_id = -1
    private val SELECT_PICTURE: Int = 101

    lateinit var vpPage: ViewPager

    internal var ResetPostReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                adapterData.clear()

                ContentAction.list(user,Pair("createAt", Query.Direction.DESCENDING),0) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->

                    if(success && data != null) {
                        data.forEach {
                            println(it)

                            if (it != null) {
//                                adapterData.add(it)
                            }

                        }

                        adapter.notifyDataSetChanged()

                    }

                }
            }
        }
    }

    internal var DeletePostReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                adapterData.clear()

                ContentAction.list(user, Pair("createAt", null), 0) { success: Boolean, data: ArrayList<Map<String, Any>?>?, exception: Exception? ->

                    if (success && data != null) {
                        data.forEach {
                            println(it)

                            if (it != null) {
//                                adapterData.add(it)
                            }

                        }

                        adapter.notifyDataSetChanged()

                    }

                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        progressDialog = ProgressDialog(context)

        val ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx)
        }

        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main_edit_search = view.findViewById(R.id.main_edit_search)
        addpostLL = view.findViewById(R.id.addpostLL)
        main_listview_search = view.findViewById(R.id.main_listview_search)
        main_edit_close = view.findViewById(R.id.main_edit_close)
        main_edit_listview = view.findViewById(R.id.main_edit_listview)
        main_listview = view.findViewById(R.id.main_listview)

        main_edit_search.setOnClickListener {

        }

        main_edit_search.setOnEditorActionListener { v, actionId, event ->
            false
        }

        btn_del_searchWord.setOnClickListener {

        }

        val params = RequestParams()
        params.put("member_id",member_id)

        PostAction.load_post(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {

                try {

                    val result = response!!.getString("result")

                    if ("ok" == result) {

                        val data = response!!.getJSONArray("content")
                        for (i in 0 until data.length()){
                            adapterData.add(data[i] as JSONObject)
                        }
                        adapter.notifyDataSetChanged()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println("---------------------------------failure")
            }

        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity = getActivity() as MainActivity

        val filter1 = IntentFilter("SAVE_POST")
        activity.registerReceiver(ResetPostReceiver, filter1)

        val filter2 = IntentFilter("DELETE_POST")
        activity.registerReceiver(DeletePostReceiver, filter2)

        adapter = MainAdapter(activity,R.layout.main_listview_item,adapterData)
        main_listview.adapter = adapter

        editadapter = MainEditAdapter(activity, R.layout.main_edit_listview_item,editadapterData)
        main_edit_listview.adapter = editadapter

        main_listview.setOnItemClickListener { parent, view, position, id ->

        }

        addpostLL.setOnClickListener {
            MoveAddPostActivity()
        }

        member_id = PrefUtils.getIntPreference(context, "member_id")

        mainData()

    }

    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
        progressDialog = ProgressDialog(ctx)
    }

    fun MoveAddPostActivity(){
        var intent = Intent(context, AddPostActivity::class.java);
        intent.putExtra("category",1)
        startActivityForResult(intent, SELECT_PICTURE);
    }

    fun MoveMainDetailActivity(id : String){
        var intent: Intent = Intent(activity, MainDetailActivity::class.java)
        intent.putExtra("id",id)
        startActivity(intent)
    }

    fun MoveAreaRangeActivity(){
        var intent: Intent = Intent(activity, AreaRangeActivity::class.java)
        startActivity(intent)
    }

    fun MoveMarketMainActivity(){
        var intent: Intent = Intent(activity, MarketMainActivity::class.java)
        startActivity(intent)
    }

    fun loadPost(){
        val params = RequestParams()

        PostAction.load_post(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {

                try {
                    val data = response!!.getJSONArray("content")
                    val result = response!!.getString("result")

                    println("data.size ${data.length()}  result $result")

                    if ("ok" == result) {
                        for (i in 0 until data.length()){
                            adapterData.add(data[i] as JSONObject)
                        }
                        adapter.notifyDataSetChanged()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println("---------------------------------failure")
            }

        })
    }


    fun mainData() {
        val params = RequestParams()

        PostAction.load_post(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                try {
                    val list = response!!.getJSONArray("content")

                    Log.d("리스트",list.toString())
                    println("-------------------------")
                    for (i in 0..(list.length()-1)){
                        adapterData.add(list[i] as JSONObject)
                    }
                    adapter.notifyDataSetChanged()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONArray?) {
                super.onSuccess(statusCode, headers, response)
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseString: String?) {

                // System.out.println(responseString);
            }

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                // System.out.println(responseString);

                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONArray?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onStart() {
                // show dialog
                if (progressDialog != null) {

                    progressDialog!!.show()
                }
            }

            override fun onFinish() {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
            }
        })
    }
}