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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;

import xiaofei.library.hermeseventbus.HermesEventBus;

/**
 * Created by Xiaofei on 16/6/26.
 */
public class SecondActivity extends AppCompatActivity {
    private final String TAG = SecondActivity.class.getSimpleName();
    private TextView textView;
    private int hermesCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HermesEventBus.getDefault().register(this);
        setContentView(R.layout.activity_second);
        textView = (TextView) findViewById(R.id.tv);
        findViewById(R.id.post_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                HermesEventBus.getDefault().post("This is an event from the sub-process.");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (hermesCount++ < 1000) {
                            long time = System.currentTimeMillis();
                            Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator + "bitmap.png");
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            Log.e(TAG, "compressBitmapSpend:" + (System.currentTimeMillis() - time) + "; count:" + hermesCount);
                            byte[] bitmapBase64 = Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                            Log.e(TAG, "compressToBase64Spend:" + (System.currentTimeMillis() - time));
                            bitmap.recycle();
                            HermesEventBus.getDefault().post(bitmapBase64);
                            SystemClock.sleep(500);
                        }
                    }
                }).start();
            }
        });
        findViewById(R.id.post_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HermesEventBus.getDefault().postSticky("This is a sticky event from the sub-process.");
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
                HermesEventBus.getDefault().removeStickyEvent("This is a sticky event from the sub-process.");
                Toast.makeText(getApplicationContext(), "Sticky event is removed", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.remove_get_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), HermesEventBus.getDefault().removeStickyEvent(String.class), Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.bindAidlService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == bitmapAidlInterface) {
                    bindService(new Intent(SecondActivity.this, GetParseService.class), serviceConnection, BIND_AUTO_CREATE);
                } else {
                    Toast.makeText(SecondActivity.this, "Already bind Service", Toast.LENGTH_LONG).show();
                }
            }
        });
        findViewById(R.id.kill_process).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HermesEventBus.getDefault().destroy();
                // The above statement is actually useless, for there is no enough time for disconnecting.
                // So once this button is pressed, you will see a DeadObjectException as long as you
                // send an event between processes.
                Process.killProcess(Process.myPid());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HermesEventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showText(String text) {
        textView.setText(text);
        Log.v("EricZhao", "SecondActivity receives an event: " + text);
    }

    private long aidlCount;

    private IBitmapAidlInterface bitmapAidlInterface;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bitmapAidlInterface = IBitmapAidlInterface.Stub.asInterface(service);
            Log.e(TAG, "onServiceConnected");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (aidlCount++ < 1000) {
                        if (null != bitmapAidlInterface) {
                            try {
                                long time = System.currentTimeMillis();
                                IDCardInfo idCardInfo = bitmapAidlInterface.getIDCardInfo();
                                final Bitmap bitmap = idCardInfo.getHeadBitmap();
                                if (null == bitmap) {
                                    Log.e(TAG, "bitmap is null");
                                    return;
                                }
                                Log.e(TAG, "aidlGetBitmapSpend:" + (System.currentTimeMillis() - time) + ";count:" + aidlCount);
                                Log.e(TAG, "idCardInfoSpend:name:" + idCardInfo.getName() + ";age:" + idCardInfo.getAge());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        BitmapDrawable drawable = new BitmapDrawable(SecondActivity.this.getResources(), bitmap);
                                        textView.setBackgroundDrawable(drawable);
                                    }
                                });
                                SystemClock.sleep(500);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bitmapAidlInterface = null;
            Log.e(TAG, "onServiceConnected");
        }
    };
}
