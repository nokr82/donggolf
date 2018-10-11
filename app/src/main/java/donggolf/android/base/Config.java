package donggolf.android.base;

public class Config {

    public static String serverIp = "61.82.101.208";
    public static String url = "http://" + serverIp + ":8080";
    public static String socketUrl = "http://" + serverIp + ":3000";
    public static String rtmpPublishUrl = "rtmp://" + serverIp + ":8082/live/";
    public static String rtmpPlayUrl = "rtmp://" + serverIp + ":8082/live/";

    public static String S3BucketName = "merryholidayseoul";
    public static String s3SeoulURL = "https://s3.ap-northeast-2.amazonaws.com/" + S3BucketName + "/";
    public static String background_image_url = s3SeoulURL + "mh/openRoomImage.jpg";
    public static String login_background_url = s3SeoulURL + "mh/join.mov";
    public static String share_default_image = s3SeoulURL + "mh/logo_round_1024.png";


    public static void resetServerIp(String serverIp) {

        // 개발서버
        // serverIp = "13.124.77.67";

        Config.serverIp = serverIp;
        Config.url = "http://" + serverIp + ":8080";
        Config.socketUrl = "http://" + serverIp + ":3000";
        Config.rtmpPublishUrl = "rtmp://" + serverIp + ":8082/live/";
        Config.rtmpPlayUrl = "rtmp://" + serverIp + ":8082/live/";
    }

}
