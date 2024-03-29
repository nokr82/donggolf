package donggolf.android.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
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
import donggolf.android.activities.AddPostActivity
import donggolf.android.activities.MainActivity
import donggolf.android.activities.MainDetailActivity
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.fragment_main.*
import org.json.JSONObject

open class FreeFragment : Fragment() {

    var ctx: Context? = null

    private var adapterData: ArrayList<JSONObject> = ArrayList<JSONObject>()
    private lateinit var adapter: MainAdapter
    private lateinit var editadapter: MainEditAdapter
    private var editadapterData: ArrayList<JSONObject> = ArrayList<JSONObject>()

    val user = HashMap<String, Any>()


    lateinit var main_edit_listview: ListView
    lateinit var addpostLL: LinearLayout
    lateinit var main_edit_search: EditText
    lateinit var main_listview_search: LinearLayout
    lateinit var main_edit_close: LinearLayout
    lateinit var main_listview: ListView

    private var progressDialog: ProgressDialog? = null
    lateinit var fragment: FreeFragment
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
                page = 1
                mainData("")
            }
        }
    }

    internal var reloadReciver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                page = 1
                mainData("")
            }
        }
    }

    internal var ResetPostReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                adapterData.clear()
                page = 1

                ContentAction.list(user, Pair("createAt", Query.Direction.DESCENDING), 0) { success: Boolean, data: ArrayList<Map<String, Any>?>?, exception: Exception? ->

                    if (success && data != null) {
                        data.forEach {
                            //println(it)

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
                page = 1

                ContentAction.list(user, Pair("createAt", null), 0) { success: Boolean, data: ArrayList<Map<String, Any>?>?, exception: Exception? ->

                    if (success && data != null) {
                        data.forEach {
                            //println(it)

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
        var filter3 = IntentFilter("ADD_POST")
        activity.registerReceiver(reloadReciver, filter3)


        val filter1 = IntentFilter("SAVE_POST")
        activity.registerReceiver(ResetPostReceiver, filter1)

        val filter2 = IntentFilter("DELETE_POST")
        activity.registerReceiver(DeletePostReceiver, filter2)

        adapter = MainAdapter(activity, R.layout.main_listview_item, adapterData)
        main_listview.adapter = adapter

        editadapter = MainEditAdapter(activity, R.layout.main_edit_listview_item, editadapterData, this)
        main_edit_listview.adapter = editadapter

        main_listview.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(p0: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount
                totalItemCountScroll = totalItemCount
            }

            override fun onScrollStateChanged(main_listview: AbsListView, newState: Int) {
                if (!main_listview.canScrollVertically(-1)) {
                    page = 1
                    mainData(Utils.getString(main_edit_search))
                }
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
                    userScrolled = false

                    //화면이 바닥에 닿았을때
                    if (totalPage > page) {
                        page++
                        lastcount = totalItemCountScroll

                        mainData(Utils.getString(main_edit_search))
                    }

                }


            }
        })

        main_listview.setOnItemClickListener { parent, view, position, id ->

            Utils.hideKeyboard(context)

            if (main_listview_search.visibility == View.VISIBLE) {
                main_listview_search.visibility = View.GONE
                return@setOnItemClickListener
            }

            val data = adapter.getItem(position)
            val content = data.getJSONObject("Content")
            var id = Utils.getInt(content, "id")

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
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val srchWd = main_edit_search.text.toString()
                if (srchWd != null && srchWd != "") {
                    addSearchWords(srchWd)
                    getSearchList()
//                    resetList(srchWd)
                    mainData(srchWd)
                }

                if (srchWd == null || srchWd == "") {
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
            var keyword = Utils.getString(main_edit_search)
            main_listview_search.visibility = View.GONE
            main_edit_search.isCursorVisible = false
            if (keyword == "") {
                Toast.makeText(context, "키워드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addSearchWords(keyword)
            mainData(keyword)
//            resetList(keyword)
        }


        btn_del_searchWord.setOnClickListener {
            Utils.hideKeyboard(context)

            if (Utils.getString(main_edit_search) == "") {
                return@setOnClickListener
            }
            main_edit_search.setText("")
//            resetList("")
            main_edit_search.isCursorVisible = false
        }

        main_edit_listview.setOnItemClickListener { parent, view, position, id ->

            val item = editadapterData.get(position)
            val SearchList = item.getJSONObject("SearchList")
            val content = Utils.getString(SearchList, "content")
            //println("----content$content")
            main_edit_search.setText(content)
//            resetList(content)
            mainData(content)
            main_edit_search.isCursorVisible = false
            Utils.hideKeyboard(context)
        }

        // mainData("")
        getSearchList()

    }

    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
//        progressDialog = ProgressDialog(ctx)
    }

    fun MoveAddPostActivity() {
        if (PrefUtils.getIntPreference(context, "member_id") == -1) {
            Toast.makeText(context, "비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
            return
        }

        var intent = Intent(context, AddPostActivity::class.java);
        intent.putExtra("category", 1)
        getActivity()!!.startActivityForResult(intent, RESET_DATA);
    }

    fun MoveMainDetailActivity(id: String) {
        var intent: Intent = Intent(activity, MainDetailActivity::class.java)
        intent.putExtra("id", id)
        startActivityForResult(intent, DETAIL);
    }

    fun mainData(keyWord: String) {
        val params = RequestParams()
        // var region_id = PrefUtils.getStringPreference(ctx, "region_id")
        val region_id = PrefUtils.getStringPreference(ctx, "main_region_id", "0")

        Log.d("아뒤",region_id.toString())
        params.put("member_id", member_id)
        params.put("region_id", region_id)
        params.put("searchKeyword", keyWord)

        params.put("page", page)

        PostAction.load_post(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                val result = response!!.getString("result")
                if (result == "ok") {
                    if (page == 1) {
                        adapterData.clear()
                    }

                    totalPage = response.getInt("totalPage");
                    page = response.getInt("page");

                    val list = response!!.getJSONArray("content")

                    for (i in 0..(list.length() - 1)) {
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
        params.put("member_id", member_id)
        params.put("content", content)

        PostAction.add_search(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                getSearchList()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }

        })

    }

    fun getSearchList() {
        val params = RequestParams()
        params.put("member_id", member_id)

        PostAction.search_list(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    if (editadapterData != null) {
                        editadapterData.clear()
                    }

                    var contents: ArrayList<JSONObject> = ArrayList<JSONObject>()
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
        if (reloadReciver != null) {
            context!!.unregisterReceiver(reloadReciver)
        }

        progressDialog = null
    }

    override fun onResume() {
        super.onResume()
        mainData("")
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
                    //Log.d("리절트", "야스")
                    if (data != null) {
                        //Log.d("리절트", "야스")
                        page = 1
                        mainData("")
                    }
                }

                DETAIL -> {
                    if (data!!.getStringExtra("reset") != null) {
                        if (adapterData != null) {
                            adapterData.clear()
                        }
                        page = 1
                        mainData("")
                    }
                }
            }
        }

    }

    fun deleteSearchList(searchid: String) {
        val params = RequestParams()
        params.put("searchid", searchid)

        PostAction.delete_search(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }


}