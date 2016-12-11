package io.derl.helium3;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.derl.log.Log;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = Log.makeLogTag(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, this);
        NullPointerException exception = new NullPointerException("null");
//        Log.e(TAG, exception, "onCreate");
        Log.e(TAG, null);
        Log.setLogDir(Environment.getExternalStorageDirectory() + "/helium3");
        Log.f(TAG, "hello, this is log");
        Log.f(TAG, exception, "This is a exception:");
    }
}
