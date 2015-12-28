package cniao5.com.cniao5shop;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by <a href="http://www.cniao5.com">菜鸟窝</a>
 * 一个专业的Android开发在线教育平台
 */
public class CniaoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
    }
}
