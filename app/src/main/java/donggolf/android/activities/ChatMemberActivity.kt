package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import donggolf.android.R
import donggolf.android.adapters.ChatMemberAdapter
import donggolf.android.base.RootActivity
import donggolf.android.models.MutualFriendData
import kotlinx.android.synthetic.main.activity_chat_member.*

class ChatMemberActivity : RootActivity() {

    lateinit var context: Context
    var chatMemberList = ArrayList<MutualFriendData>()
    // MutualFriendData의 condition에 멤버의 권한(개설자, 권한자, 나) 데이터를 넣기로 함
    lateinit var chatMemberAdapter :ChatMemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_member)

        context = this

        //Temporary data setting(it'll be removed)
        var tmpChat1 = MutualFriendData(null, "타마모", null, "개설자")
        var tmpChat2 = MutualFriendData(null, "끠안화", null, "권한자")
        var tmpChat3 = MutualFriendData(null, "청행등", null, "나")
        chatMemberList.add(tmpChat1)
        chatMemberList.add(tmpChat2)
        chatMemberList.add(tmpChat3)

        chatMemberAdapter = ChatMemberAdapter(context, R.layout.item_chat_member_list, chatMemberList)
        joinMemberLV.adapter = chatMemberAdapter
    }
}
