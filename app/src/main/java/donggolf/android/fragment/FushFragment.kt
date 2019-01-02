package donggolf.android.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.R
import donggolf.android.activities.SetNoticeActivity
import donggolf.android.adapters.FushFragAdapter
import org.json.JSONObject

class FushFragment : Fragment(){

    var ctx: Context? = null

    private var mAuth: FirebaseAuth? = null

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  lateinit var  adapter : FushFragAdapter

    lateinit var finishLL : LinearLayout
    lateinit var settingLV : LinearLayout
    lateinit var noticeLV : ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)

        val ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx)
        }

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()

        val db = FirebaseFirestore.getInstance()

        return LayoutInflater.from(inflater.context).inflate(R.layout.activity_notice2,container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        finishLL = view.findViewById(R.id.finishLL)
        settingLV = view.findViewById(R.id.settingLV)
        noticeLV = view.findViewById(R.id.noticeLV)

        var dataObj : JSONObject = JSONObject();

        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)

        adapter = FushFragAdapter(ctx!!,R.layout.item_fushfrag,adapterData)

        noticeLV.adapter = adapter

        settingLV.setOnClickListener {
            var intent: Intent = Intent(activity, SetNoticeActivity::class.java)
            startActivity(intent)
        }

    }

    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
    }
}