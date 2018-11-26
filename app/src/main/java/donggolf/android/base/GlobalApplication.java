package donggolf.android.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class GlobalApplication extends Application {
    private static volatile GlobalApplication instance = null;
    private static volatile Activity currentActivity = null;

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Activity currentActivity) {
        GlobalApplication.currentActivity = currentActivity;
    }

    /**
     * singleton 애플리케이션 객체를 얻는다.
     * @return singleton 애플리케이션 객체
     */
    public static GlobalApplication getGlobalApplicationContext() {
        if(instance == null)
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        return instance;
    }

    /**
     * 이미지 로더, 이미지 캐시, 요청 큐를 초기화한다.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        FirebaseApp.initializeApp(this);

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));

    }

    /**
     * 애플리케이션 종료시 singleton 어플리케이션 객체 초기화한다.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

}