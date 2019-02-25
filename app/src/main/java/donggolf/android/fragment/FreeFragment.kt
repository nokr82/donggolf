package donggolf.android.fragment

import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.inputmethod.EditorInfo
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.item_custom_gallery_folder.*
import kotlinx.android.synthetic.main.main_edit_listview_item.view.*
import org.json.JSONException
import org.json.JSONObject

open class FreeFragment : Fragment() , AbsListView.OnScrollListener{
    override fun onScroll(p0: AbsListView?, p1: Int, p2: Int, p3: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var ctx: Context? = null

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  lateinit var  adapter : MainAdapter
    private  lateinit var  editadapter : MainEditAdapter
    private  var editadapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    val user = HashMap<String, Any>()

    lateinit var main_edit_listview: ListView
    lateinit var addpostLL : LinearLayout
    lateinit var main_edit_search : EditText
    lateinit var main_listview_search : LinearLayout
    lateinit var main_edit_close : LinearLayout
    lateinit var main_listview:ListView

    private var progressDialog: ProgressDialog? = null

    lateinit var activity: MainActivity
    var tabType = 1
    var member_id = -1
    private val SELECT_PICTURE: Int = 101

    val RESET_DATA = 1000
    val DETAIL = 1001

    private var page = 1
    private var totalPage = 0
    private val visibleThreshold = 10
    private var userScrolled = false
    private var lastItemVisibleFlag = false
    private var lastcount = 0
    private var totalItemCountScroll = 0


    lateinit var vpPage: ViewPager
    internal var MsgReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                adapterData.clear()
                mainData("")
            }
        }
    }



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
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)
        ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx!!)
        }

        mainData("")

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

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity = getActivity() as MainActivity

        main_edit_search.isCursorVisible = false

        //메시지보내기
        var filter = IntentFilter("MSG_NEXT")
        activity.registerReceiver(MsgReceiver, filter)

        val filter1 = IntentFilter("SAVE_POST")
        activity.registerReceiver(ResetPostReceiver, filter1)

        val filter2 = IntentFilter("DELETE_POST")
        activity.registerReceiver(DeletePostReceiver, filter2)

        adapter = MainAdapter(activity,R.layout.main_listview_item,adapterData)
        main_listview.adapter = adapter

        editadapter = MainEditAdapter(activity, R.layout.main_edit_listview_item,editadapterData,this)
        main_edit_listview.adapter = editadapter

        main_listview.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(p0: AbsListView?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onScrollStateChanged(main_listview:AbsListView, newState: Int) {

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
//                    activity.maintitleLL.visibility=View.GONE
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    userScrolled = false
//                    activity.maintitleLL.visibility=View.VISIBLE
                }

                if (!main_listview.canScrollVertically(-1)) {
                    page=1
                    var keyword = main_edit_search.text.toString()
                    if (keyword == null || keyword == "") {
                        mainData("")
                    } else {
                        mainData(keyword)
                    }
                } else if (!main_listview.canScrollVertically(1)) {
                    if (totalPage > page) {
                        page++
                        lastcount = totalItemCountScroll
                        var keyword = main_edit_search.text.toString()
                        if (keyword == null || keyword == "") {
                            mainData("")
                        } else {
                            mainData(keyword)
                        }
                    }
                }
            }
        })



        main_listview.setOnItemClickListener { parent, view, position, id ->

            Utils.hideKeyboard(context)

            if(main_listview_search.visibility == View.VISIBLE) {
                main_listview_search.visibility = View.GONE
                return@setOnItemClickListener
            }

            val data = adapter.getItem(position)
            val content = data.getJSONObject("Content")
            var id = Utils.getInt(content,"id")

            MoveMainDetailActivity(id.toString())
        }

        addpostLL.setOnClickListener {
            MoveAddPostActivity()
        }

        member_id = PrefUtils.getIntPreference(context, "member_id")

        main_edit_search.setOnClickListener {
            main_listview_search.visibility = View.VISIBLE
            main_edit_search.isCursorVisible = true
        }

        main_edit_search.setOnEditorActionListener() { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                val srchWd = main_edit_search.text.toString()
                if (srchWd != null && srchWd != "") {
                    addSearchWords(srchWd)
                    getSearchList()
//                    resetList(srchWd)
                    mainData(srchWd)
                }

                if (srchWd == null || srchWd == ""){
//                    resetList(srchWd)
                    mainData(srchWd)
                }
                main_listview_search.visibility = View.GONE

                Utils.hideKeyboard(context)
//                main_edit_search.setText("")
            } else {
            }
            false
        }

        main_edit_close.setOnClickListener {
            main_listview_search.visibility = View.GONE
            Utils.hideKeyboard(context)
            main_edit_search.isCursorVisible = false
        }

        iconsearchIV.setOnClickListener {
           var keyword =   Utils.getString(main_edit_search)
            main_listview_search.visibility = View.GONE
            main_edit_search.isCursorVisible = false
            if (keyword==""){
                Toast.makeText(context,"키워드를 입력해주세요.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addSearchWords(keyword)
            mainData(keyword)
//            resetList(keyword)
        }


        btn_del_searchWord.setOnClickListener {
            Utils.hideKeyboard(context)

            if(Utils.getString(main_edit_search) == "") {
                return@setOnClickListener
            }
            main_edit_search.setText("")
//            resetList("")
            main_edit_search.isCursorVisible = false
        }

        main_edit_listview.setOnItemClickListener { parent, view, position, id ->

            val item = editadapterData.get(position)
            val SearchList = item.getJSONObject("SearchList")
            val content = Utils.getString(SearchList,"content")
            println("----content$content")
            main_edit_search.setText(content)
//            resetList(content)
            mainData(content)
            main_edit_search.isCursorVisible = false
            Utils.hideKeyboard(context)
        }

        mainData("")
        getSearchList()

    }

    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
//        progressDialog = ProgressDialog(ctx)
    }

    fun MoveAddPostActivity(){
        if (PrefUtils.getIntPreference(context, "member_id") == -1){
            Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
            return
        }

        var intent = Intent(context, AddPostActivity::class.java);
        intent.putExtra("category",1)
        startActivityForResult(intent, RESET_DATA);
    }

    fun MoveMainDetailActivity(id : String){
        var intent: Intent = Intent(activity, MainDetailActivity::class.java)
        intent.putExtra("id",id)
        startActivityForResult(intent, DETAIL);
    }


    fun mainData(keyWord: String) {
        val params = RequestParams()
        var sidotype = PrefUtils.getStringPreference(ctx, "sidotype")
        var goguntype  =PrefUtils.getStringPreference(ctx, "goguntype")
        var goguntype2  =PrefUtils.getStringPreference(ctx, "goguntype2")
        var region_id = PrefUtils.getStringPreference(ctx,"region_id")
        var region_id2 = PrefUtils.getStringPreference(ctx,"region_id2")

        params.put("member_id",member_id)
        params.put("goguntype",goguntype)
        params.put("sidotype",sidotype)
        params.put("region_id",region_id)
        params.put("searchKeyword",keyWord)
        if (region_id2 != ""){
            params.put("region_id2", region_id2)
        }
        params.put("page", page)

        PostAction.load_post(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                val result = response!!.getString("result")
                if (result == "ok") {
                    if (page == 1){
                        adapterData.clear()
                    }

                    totalPage = response.getInt("totalPage");
                    page = response.getInt("page");

                    println("-------page $page")
                    println("-------totalpage $totalPage")

                    val list = response!!.getJSONArray("content")

                    for (i in 0..(list.length()-1)){
                        val Content = list.get(i) as JSONObject
                        adapterData.add(Content)
                    }

                    adapter.notifyDataSetChanged()
                    main_listview_search.visibility = View.GONE

                }

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

    fun addSearchWords(content: String) {

        val params = RequestParams()
        params.put("member_id",member_id)
        params.put("content",content)

        PostAction.add_search(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                getSearchList()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }

        })

    }

    fun getSearchList(){
        val params = RequestParams()
        params.put("member_id",member_id)

        PostAction.search_list(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    if (editadapterData != null){
                        editadapterData.clear()
                    }

                    var contents:ArrayList<JSONObject> = ArrayList<JSONObject>()
                    val data = response.getJSONArray("SearchList")
                    for (i in 0..data.length() - 1) {
                        var json = data.get(i) as JSONObject
                        editadapterData.add(json)
                    }
                    editadapter.notifyDataSetChanged()
                    main_listview_search.visibility = View.GONE
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }

        })
    }

    fun resetList(keyWord : String){

        if (keyWord == null || keyWord == ""){
            mainData("")
        } else {
            val params = RequestParams()
            params.put("searchKeyword",keyWord)

            PostAction.load_post(params,object : JsonHttpResponseHandler(){

                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        try {
                            val list = response!!.getJSONArray("content")

                            if(adapterData != null){
                                adapterData.clear()
                                adapter.notifyDataSetChanged()
                            }

                            Log.d("리스트",list.toString())
                            println("-------------------------")
                            for (i in 0..(list.length()-1)){
                                val Content = list.get(i) as JSONObject
                                adapterData.add(Content as JSONObject)
                            }
                            adapter.notifyDataSetChanged()
                            main_listview_search.visibility = View.GONE
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

                }

            })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (MsgReceiver != null) {
            context!!.unregisterReceiver(MsgReceiver)
        }
        if (ResetPostReceiver != null) {
            context!!.unregisterReceiver(ResetPostReceiver)
        }
        if (DeletePostReceiver != null) {
            context!!.unregisterReceiver(DeletePostReceiver)
        }
    }






    override fun onPause() {
        super.onPause()
//        page = 1
//        mainData("")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                RESET_DATA -> {
                    if (data!!.getStringExtra("reset") != null) {
                        if (adapterData != null){
                            adapterData.clear()
                        }
                        page=1
                        mainData("")
                    }
                }

                DETAIL -> {
                    if (data!!.getStringExtra("reset") != null) {
                        if (adapterData != null){
                            adapterData.clear()
                        }
                        page=1
                        mainData("")
                    }
                }
            }
        }

    }

    fun deleteSearchList(searchid:String){
        val params = RequestParams()
        params.put("searchid",searchid)

        PostAction.delete_search(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }
}