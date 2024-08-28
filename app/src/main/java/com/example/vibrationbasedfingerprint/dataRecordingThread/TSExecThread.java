package com.example.vibrationbasedfingerprint.touchScreen;

import android.content.Context;

import android.view.MotionEvent;
import android.view.View;

import java.lang.Thread;
import java.lang.Exception;

import android.util.Log;



// may need get the event recorder instance to record motor time stamp

// TODO: apply for vibrate permission
public class TSExecThread extends Thread {
    // get instance
    // then start
    // notify

    private static TSExecThread mTSExecThread = null;
    private Context mContext = null;

    private boolean mThreadRunningFlag = false;

    private final Object mlock = new Object();

    private View mRootView = null;

    private String logtag = "MIST_MC_TSExecThread";

    private enum Status {
        NOT_START,
        START
    }

    private Status mThreadStatus = Status.NOT_START;

    // TODO: may need
    private TSExecThread(Context context, View rootView) {
        super("TSExecThread");
        mContext = context;
        if(mContext != null) {
            // mvibrator = mContext.getSystemService(Vibrator.class);
            // if(mvibrator == null) {
            //     Log.e(logtag, "vibrator not available on current device");
            // }
        } else {
            Log.e(logtag, "mContext is null");
        }

        if(mRootView == null) {
//            mRootView = findViewById(android.R.id.content);
            mRootView = rootView;
            mRootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // record every event? use ACTION_MOVE for now
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {

                        // Message touchMsg = backgroundHandler.obtainMessage(2, event);
                        // backgroundHandler.sendMessage(touchMsg);
                    }
                    return true;
                }
            });
        }


    }

    public static TSExecThread getInstance(Context context, View rootView) {
        if(mTSExecThread == null) {
            mTSExecThread = new TSExecThread(context, rootView);
            mTSExecThread.init();

        }
        return mTSExecThread;

    }

    public static void deleteInstance() {
        if(mTSExecThread != null) {
            mTSExecThread.deinit();
            mTSExecThread = null;
        }

    }

    public void init() {
        if(mTSExecThread == null || mContext == null) {
            Log.e(logtag, "creation failed, so init fails");
            return;

        }
        Log.i(logtag, "thread init");
        mThreadRunningFlag = true;
        mTSExecThread.start();


    }

    // things like thread notify, flags to false, recycle resources, etc...
    // may have to use the class lock here...
    public void deinit() {
        Log.i(logtag, "thread deinit");
        while(mThreadStatus != Status.NOT_START) {
            mThreadRunningFlag = false;
            synchronized(mlock) {
                mlock.notify();
            }
            try {
                Thread.sleep(50);
            } catch(Exception e) {
                Log.e(logtag, "deinit sleep have problem, weird...");
                Log.e(logtag, e.getMessage());
            }

        }
    }

    public void oneShot() {
        if(mThreadRunningFlag && (mThreadStatus == Status.START)) {
//            notify();
            synchronized(mlock) {
                mlock.notify();
            }
        } else {
            Log.e(logtag, "thread wait have problem, weird...");
        }
    }

    public void run() {
        // add things like loop,
        Log.i(logtag, "enter thread");
        mThreadStatus = Status.START;
//        if(mContext != null) {
//            mvibrator = mContext.getSystemService(Vibrator.class);
//            if(mvibrator == null) {
//                Log.e(logtag, "vibrator not available on current device");
//            }
//        }
        while(mThreadRunningFlag) {
            try {
                synchronized(mlock) {
                    mlock.wait();
                }
//                mlock.wait();
            } catch(Exception e) {
                Log.e(logtag, "thread wait have problem, weird...");
                Log.e(logtag, e.getMessage());
            }
            // TODO: relpace by other
            vibratorMove();
        }
//        if(mvibrator != null) {
//            mvibrator.cancel();
//            mvibrator = null;
//
//        }
        Log.i(logtag, "exit thread");
        mThreadStatus = Status.NOT_START;


    }

    private void vibratorMove() {
        Log.i(logtag, "thread run one shot");
        if (mThreadRunningFlag == false) {
            Log.i(logtag, "the thread not init or exiting, will not  viberate");
            return;
        }

//        if (mvibrator != null) {
//            // long[] timings = new long[]{50, 50, 50, 50, 50, 100, 350, 250};
//            // int[] amplitudes = new int[]{77, 79, 84, 99, 143, 255, 0, 255};
//            // mvibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
//            exampleMove();
//        } else {
//            Log.e(logtag, "no vibrator, default move failed.");
//        }
    }

//    private void exampleMove() { // right from google
//        long[] timings = new long[]{50, 50, 50, 50, 50, 100, 350, 250};
//        int[] amplitudes = new int[]{77, 79, 84, 99, 143, 255, 0, 255};
//        mvibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
//    }

}