package edu.buffalo.cse.phonelab.conductor.test;

import android.content.Context;

public abstract class TestWorker implements Runnable {

    protected Context mContext;
    protected boolean mRunning;

    public TestWorker(Context context) {
        mContext = context;
        mRunning = false;
    }

    public abstract void before_work();
    public abstract void do_work();
    public abstract void after_work();


    @Override
    public void run() {
        mRunning = true;
        before_work();
        while (mRunning) {
            do_work();
        }
        after_work();
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void stopAsync() {
        mRunning = false;
    }
}
