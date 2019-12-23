/**
 * Copyright 2016 Xiaofei
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xiaofei.library.hermeseventbustest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.Tag;
import android.os.Environment;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
/**
 *
 * Copyright 2016 Xiaofei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import xiaofei.library.hermes.Hermes;
import xiaofei.library.hermeseventbus.HermesEventBus;

/**
 * Created by Xiaofei on 16/6/26.
 */
public class MainActivity extends AppCompatActivity {

    private TextView textView;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HermesEventBus.getDefault().register(this);
        //Hermes.getVersion();
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SecondActivity.class));
            }
        });
        findViewById(R.id.post_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.v("EricZhao", "post");
                        HermesEventBus.getDefault().post("This is an event from the main process.");
                        bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator + "bitmap.png");
                        HermesEventBus.getDefault().post(bitmap);
                    }
                }.start();
            }
        });
        findViewById(R.id.post_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.v("EricZhao", "post sticky");
                        HermesEventBus.getDefault().postSticky("This is a sticky event from the main process");
                    }
                }.start();
            }
        });
        findViewById(R.id.get_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), HermesEventBus.getDefault().getStickyEvent(String.class), Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.remove_all_sticky_events).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HermesEventBus.getDefault().removeAllStickyEvents();
                Toast.makeText(getApplicationContext(), "All sticky events are removed", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.remove_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HermesEventBus.getDefault().removeStickyEvent("This is a sticky event from the main process");
                Toast.makeText(getApplicationContext(), "Sticky event is removed", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.remove_get_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), HermesEventBus.getDefault().removeStickyEvent(String.class), Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(getApplicationContext(), MyService.class));
            }
        });
//        findViewById(R.id.exception).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                throw new RuntimeException("This is an exception.");
//            }
//        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showText(String text) {
        textView.setText(text);
        Log.v("EricZhao", "MainActivity receives an event: " + text);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveBitmap(Bitmap bitmap) {
        Log.d("wxj", "onReceiveBitmap:" + bitmap + ";isRecycled:" + bitmap.isRecycled());
        if (bitmap.isRecycled()) {
            Log.d("wxj", "onReceiveBitmap: isRecycled true");
            return;
        }
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
//        textView.setBackgroundDrawable(bitmapDrawable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveBase64(byte[] base64Bitmap) {
        long time = System.currentTimeMillis();
        byte[] bitmapBytes = Base64.decode(base64Bitmap, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        textView.setBackgroundDrawable(bitmapDrawable);
        Log.e("wxj", "decodeBase64Spend:" + (System.currentTimeMillis() - time));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HermesEventBus.getDefault().unregister(this);
        // Process.killProcess(Process.myPid());
    }
}
