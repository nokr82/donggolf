package donggolf.android.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.EventsAction
import donggolf.android.actions.MemberAction
import donggolf.android.adapters.EventAdapter
import donggolf.android.adapters.FushFragAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_events.*
import org.json.JSONArray
import org.json.JSONObject

class EventsActivity : RootActivity(), AbsListView.OnScrollListener {

    private lateinit var context: Context
    private var progressDialog: ProgressDialog? = null

    private var member_id = -1;
    private var page = 1;
    private var totalPage = 1;

    private var userScrolled = false
    private var lastItemVisibleFlag = false
    private var totalItemCountScroll = 0
    private val itemCount = 0
    private val totalItemCount = 0
    private var lastcount = 0
    private val visibleThreshold = 10

    private var adapterData = ArrayList<JSONObject>()
    private lateinit var adapter : EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        context = this
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        member_id = PrefUtils.getIntPreference(context, "member_id")

        finishaBT.setOnClickListener {
            finish()
        }

        adapter = EventAdapter(context, R.layout.item_event, adapterData)
        eventLV.adapter = adapter
        eventLV.setOnScrollListener(this)
        eventLV.setOnItemClickListener { parent, view, position, id ->

            var data = adapterData.get(position)

            var event = data.getJSONObject("Event")

            var intent = Intent(context, EventDetailActivity::class.java)
            intent.putExtra("event_id", Utils.getInt(event, "id"))
            startActivity(intent)

        }

        loadData()

    }

    fun loadData() {

        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("page", page)

        EventsAction.index(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                val result = response!!.getString("result")

                if (result == "ok") {
                    noDataTV.visibility = View.GONE
                    eventLV.visibility = View.VISIBLE

                    page = Utils.getInt(response, "page")
                    totalPage = Utils.getInt(response, "totalPage")

                    if(page == 1) {
                        adapterData.clear()
                    }

                    var list = response.getJSONArray("list")

                    for (i in 0 until list.length()) {
                        adapterData.add(list.get(i) as JSONObject)
                    }

                    adapter.notifyDataSetChanged()

                } else if(result == "empty") {
                    noDataTV.visibility = View.VISIBLE
                    eventLV.visibility = View.GONE
                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

//                 System.out.println(responseString);

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

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
            }

        })

    }

    override fun onDestroy() {
        super.onDestroy()

        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }

        progressDialog = null

    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        // 현재 가장 처음에 보이는 셀번호와 보여지는 셀번호를 더한값이
        // 전체의 숫자와 동일해지면 가장 아래로 스크롤 되었다고 가정합니다.
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            userScrolled = true
        } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
            userScrolled = false
            //화면이 바닥에 닿았을때
            if (totalItemCount > itemCount) {
                page++
                lastcount = totalItemCountScroll
                loadData()
            }
        }

    }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (userScrolled && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold && itemCount < this.totalItemCount && this.totalItemCount > 0) {
            if (this.totalItemCount > itemCount) {
                //                page++;
                //                adapterData.clear();
                //                searchList();
            }
        }

        //현재 화면에 보이는 첫번째 리스트 아이템의 번호(firstVisibleItem)
        // + 현재 화면에 보이는 리스트 아이템의갯수(visibleItemCount)가
        // 리스트 전체의 갯수(totalOtemCount)-1 보다 크거나 같을때
        lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount
        totalItemCountScroll = totalItemCount
    }

}
