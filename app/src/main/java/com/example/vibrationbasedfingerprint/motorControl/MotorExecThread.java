package com.example.vibrationbasedfingerprint.motorControl;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.lang.Thread;
import java.lang.Exception;

import android.util.Log;



// may need get the event recorder instance to record motor time stamp

// TODO: apply for vibrate permission
public class MotorExecThread extends Thread {
    // get instance
    // then start
    // notify

    private static MotorExecThread mMotorExecThread = null;
    private Context mContext = null;
    private Vibrator mvibrator = null;
    private boolean mThreadRunningFlag = false;




    private final Object mlock = new Object();

    private String logtag = "MIST_MC_MotorExecThread";

    private enum Status {
        NOT_START,
        START
    }

    private Status mThreadStatus = Status.NOT_START;

    // TODO: may need
    private MotorExecThread(Context context) {
        super("MotorExecThread");
        mContext = context;
        if(mContext != null) {
            // mvibrator = mContext.getSystemService(Vibrator.class);
            // if(mvibrator == null) {
            //     Log.e(logtag, "vibrator not available on current device");
            // }
        } else {
            Log.e(logtag, "mContext is null");
        }
    }

    public static MotorExecThread getInstance(Context context) {
        if(mMotorExecThread == null) {
            mMotorExecThread = new MotorExecThread(context);
            mMotorExecThread.init();

        }
        return mMotorExecThread;

    }

    public static void deleteInstance() {
        if(mMotorExecThread != null) {
            mMotorExecThread.deinit();
            mMotorExecThread = null;
        }

    }

    public void init() {
        if(mMotorExecThread == null || mContext == null) {
            Log.e(logtag, "creation failed, so init fails");
            return;

        }
        Log.i(logtag, "thread init");
        mThreadRunningFlag = true;
        mMotorExecThread.start();


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
        if(mContext != null) {
            mvibrator = mContext.getSystemService(Vibrator.class);
            if(mvibrator == null) {
                Log.e(logtag, "vibrator not available on current device");
            }
        }
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
        if(mvibrator != null) {
            mvibrator.cancel();
            mvibrator = null;

        }
        Log.i(logtag, "exit thread");
        mThreadStatus = Status.NOT_START;


    }

    private void vibratorMove() {
        Log.i(logtag, "thread run one shot");
        if (mThreadRunningFlag == false) {
            Log.i(logtag, "the thread not init or exiting, will not  viberate");
            return;
        }

        if (mvibrator != null) {
            // long[] timings = new long[]{50, 50, 50, 50, 50, 100, 350, 250};
            // int[] amplitudes = new int[]{77, 79, 84, 99, 143, 255, 0, 255};
            // mvibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
            exampleMove();
        } else {
            Log.e(logtag, "no vibrator, default move failed.");
        }
    }

    private void exampleMove() { // right from google
        long[] timings = new long[]{50, 50, 50, 50, 50, 100, 350, 250};
        int[] amplitudes = new int[]{77, 79, 84, 99, 143, 255, 0, 255};
        mvibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
    }

}