package edu.buffalo.cse.phonelab.conductor.test;

import java.io.File;
import java.io.FileOutputStream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import edu.buffalo.cse.phonelab.conductor.services.IFileUploaderService;

public class FileUploaderTest extends TestWorker {
    private final String TAG = this.getClass().getSimpleName();

    private static final String FILE_NAME_BASE = FileUploaderTest.class.getSimpleName() + ".";
    private final String ACTION_UPLOAD = "edu.buffalo.cse.phonelab.conductor.services.FileUploaderService";

    private String mPackageName;
    private int mCounter;

    /* maxium file number generated during an internval */
    private static final int MAX_FILE_NUM = 5;
    private int intervalMS = 1*1000;
    private IFileUploaderService mService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i(TAG, "Connected to PhoneLab File Uploader Service");
            mService = IFileUploaderService.Stub.asInterface(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "Disconnected to PhoneLab File Uploader Service");
            mService = null;
        }
    };


    public FileUploaderTest(Context mContext) {
        super(mContext);

        mCounter = 0;
        mPackageName = mContext.getApplicationContext().getPackageName();
    }

    private void collectAndSend() {
        if (mService == null) {
            mContext.bindService(new Intent(ACTION_UPLOAD), mConnection, Context.BIND_AUTO_CREATE);
            Log.w(TAG, "Service not connected yet");
            return;
        }
        for (String fileName : mContext.fileList()) {
            try {
                String path = mContext.getFileStreamPath(fileName).getAbsolutePath();
                if (mService.isUploaded(path)) {
                    Log.v(TAG, path + " has been uploaded, delete it.");
                    (new File(path)).delete();
                }
                else {
                    mService.upload(mPackageName, path);
                }
            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }



    public void do_work() {
        int fileNumber = (int)(Math.random()*MAX_FILE_NUM) % MAX_FILE_NUM;
        if (fileNumber == 0) {
            return;
        }

        try {
            for (int i = 0; i < fileNumber; i++) {
                String fileName = FILE_NAME_BASE + mCounter;
                /* this will create a file under /data/data/$YOUR_PACKAGE_NAME/files/$FILE_NAME for you to write */
                FileOutputStream fileOutputStream = mContext.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
                fileOutputStream.write("Hello world!".getBytes());
                fileOutputStream.close();
                Log.v(TAG, "Generated file " + fileName);
                mCounter++;
            }

            collectAndSend();
            Thread.sleep(intervalMS);
        }
        catch (Exception e) {
            Log.e(TAG, "Error generating upload files", e);
        }
    }

    @Override
    public void before_work() {
        Log.v(TAG, "========== Start generating files ==========");
        mContext.bindService(new Intent(ACTION_UPLOAD), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void after_work() {
        if (mService != null) {
            mContext.unbindService(mConnection);
        }
        Log.v(TAG, "========== Stop generating files ==========");
    }

}
