package edu.buffalo.cse.phonelab.harness.lib.examples;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.util.Log;

import edu.buffalo.cse.phonelab.harness.lib.services.IFileUploaderService;

/* Periodically generate dummy files to upload */
public class TestFileUploader extends Activity
{
    private final String TAG = this.getClass().getSimpleName();
    private final String ACTION_UPLOAD = "edu.buffalo.cse.phonelab.harness.lib.services.FileUploaderService";

    private String PACKAGE_NAME;

    private int counter;
    /* maxium file number generated during an internval */
    private final int MAX_FILE_NUM = 100;
    private int intervalMS = 1*1000;
    private IFileUploaderService fileUploaderService;

    private Worker worker;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i(TAG, "### Connected to PhoneLab File Uploader Service");
            fileUploaderService = IFileUploaderService.Stub.asInterface(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "### Disconnected to PhoneLab File Uploader Service");
            fileUploaderService = null;
        }
    };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        counter = 0;
        PACKAGE_NAME = getApplicationContext().getPackageName();

        worker = new Worker(getApplicationContext());
        (new Thread(worker)).start();
    }

    @Override
    public void onDestroy(){
        if (fileUploaderService != null) {
            unbindService(connection);
        }
        worker.running = false;
        super.onDestroy();
    }

    @SuppressLint("WorldReadableFiles")
	class Worker implements Runnable {
        private final String FILE_NAME_BASE = "TEST_FILE_UPLOAD.";
        private Context context;

        public boolean running;

        public Worker(Context context) {
            this.context = context;
            running = true;
        }

        private void collectAndSend() {
            if (fileUploaderService == null) {
                bindService(new Intent(ACTION_UPLOAD), connection, Context.BIND_AUTO_CREATE);
                Log.w(TAG, "Service not connected yet");
                return;
            }
            for (String fileName : context.fileList()) {
                try {
                    String path = context.getFileStreamPath(fileName).getAbsolutePath();
                    if (fileUploaderService.isUploaded(path)) {
                        Log.v(TAG, path + " has been uploaded, delete it.");
                        (new File(path)).delete();
                    }
                    else {
                        fileUploaderService.upload(PACKAGE_NAME, path);
                    }
                }
                catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        @Override
        public void run() {
            Log.v(TAG, "========== Start generating files ==========");

            while (running) {
                int fileNumber = (int)(Math.random()*MAX_FILE_NUM) % MAX_FILE_NUM;
                if (fileNumber == 0) {
                    continue;
                }

                try {
                    for (int i = 0; i < fileNumber; i++) {
                        String fileName = FILE_NAME_BASE + counter;
                        /* this will create a file under /data/data/$YOUR_PACKAGE_NAME/files/$FILE_NAME for you to write */
                        FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
                        fileOutputStream.write("Hello world!".getBytes());
                        fileOutputStream.close();
                        Log.v(TAG, "Generated file " + fileName);
                        counter++;
                    }

                    collectAndSend();

                    Thread.sleep(intervalMS);
                }
                catch (Exception e) {
                    Log.e(TAG, "Error generating upload files " + e);
                }
            }
            Log.v(TAG, "========== Stop generating files ==========");

        }
    }
}
