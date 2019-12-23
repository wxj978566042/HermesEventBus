package xiaofei.library.hermeseventbustest;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

/**
 * Create by : 王行骏
 * Create Time : 20:38
 * Create Description:
 */
public class GetParseService extends Service {
    final String TAG = GetParseService.class.getSimpleName();

    private int count;

    private IBitmapAidlInterface.Stub binder = new IBitmapAidlInterface.Stub() {
        @Override
        public Bitmap getBitmap() {
            return BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator + "bitmap.png");
        }

        @Override
        public IDCardInfo getIDCardInfo() {
            count++;
            return new IDCardInfo(getBitmap(), "小王同学" + count, 26 + count);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind...");
        return binder;
    }
}
