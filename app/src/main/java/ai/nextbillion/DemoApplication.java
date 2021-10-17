package ai.nextbillion;

import android.app.Application;

import com.nbmap.nbmapsdk.Nbmap;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Nbmap.getInstance(this, BuildConfig.NBAI_API_KEY);
    }
}
