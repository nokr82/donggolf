package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object MemberAction {

    //회원가입
    fun join_member(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/join/submit.json", params, handler)
    }

    //중복 id?
    fun is_duplicated_id(params: RequestParams, handler: JsonHttpResponseHandler){
        HttpClient.post("/join/id_check.json", params, handler)
    }

    //로그인
    fun member_login(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/login/login.json", params, handler)
    }

    //아이디 찾기
    fun find_id(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/login/find_id.json", params, handler)
    }

    //회원정보 가져오기
    fun get_member_info(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/member_info.json", params, handler)
    }

    fun update_info(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/update_member_info.json", params, handler)
    }



}