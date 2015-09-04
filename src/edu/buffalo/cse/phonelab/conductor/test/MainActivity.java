package edu.buffalo.cse.phonelab.conductor.test;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    private final String TAG = this.getClass().getSimpleName();

    private static final String[] TEST_CLASSES = {
        "edu.buffalo.cse.phonelab.conductor.test.FileUploaderTest",
    };

    private Map<String, TestWorker> mTestWorkers;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mTestWorkers = new HashMap<String, TestWorker>();

        for (String cls : TEST_CLASSES) {
            try {
                mTestWorkers.put(cls, (TestWorker) Class.forName(cls)
                        .getConstructor(Context.class).newInstance(this));
            }
            catch (Exception e) {
                Log.e(TAG, "Failed to start test worker " + cls, e);
            }
        }

        for (Map.Entry<String, TestWorker> entry : mTestWorkers.entrySet()) {
            Log.d(TAG, "Starting test worker " + entry.getKey());
            (new Thread(entry.getValue())).start();
        }
    }

    @Override
    public void onDestroy(){
        for (Map.Entry<String, TestWorker> entry : mTestWorkers.entrySet()) {
            Log.d(TAG, "Stopping test worker " + entry.getKey());
            entry.getValue().stopAsync();
        }
        super.onDestroy();
    }
}
