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

    //비밀번호 코드
    fun getPassCode(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/join/send_pass_code.json", params, handler)
    }

    //비밀번호 재발급
    fun pass_reissue(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/join/forget_pwd.json", params, handler)
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

    fun m_update_info(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/update_info.json", params, handler)
    }

    //이미지만 가져오기
    fun get_member_img_history(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/get_member_prfimg_history.json", params, handler)
    }

    //앨범에서 이미지 추가
    fun add_img_in_album(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/add_album_img.json", params, handler)
    }

    //프사 삭제
    fun delete_profile_imgs(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/delete_profile_images.json", params, handler)
    }

    fun update_info(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/update_member_info.json", params, handler)
    }

    //내 글
    fun my_post_load(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/my_post_list_load.json", params, handler)
    }

    //친구찾기
    fun search_member(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/search_member.json", params, handler)
    }

    //문의하기
    fun inquire(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/inquire.json", params, handler)
    }

    fun regist_token(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/regist_token.json", params, handler)
    }

    // 내 알림 목록
    fun alarms(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/alarms.json", params, handler)
    }

}