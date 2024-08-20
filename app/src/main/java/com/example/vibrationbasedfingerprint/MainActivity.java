package com.example.vibrationbasedfingerprint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Vibrator vibrator;
    private Switch switchVibration;
    private Button startVibrationButton;
    private Button stopVibrationButton;
    private TextView touchInfoTextView;

    // HandlerThread for background processing
    private HandlerThread handlerThread;
    private Handler backgroundHandler;

    private boolean isVibrating = false;

    /**
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // UI component
        switchVibration = findViewById(R.id.switchVibration);
        startVibrationButton = findViewById(R.id.startVibrationButton);
        stopVibrationButton = findViewById(R.id.stopVibrationButton);

        // start event
        startVibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int repeatable = switchVibration.isChecked() ? 1 : -1;
                startVibration(repeatable);
                isVibrating = true;
            }
        });

        // stop event
        stopVibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVibration();
            }
        });

        //-----------------------------------------------
        // Get the Component of TextView: to show the touch info -- debug
        touchInfoTextView = findViewById(R.id.touchInfoTextView);

        // HandlerThread
        handlerThread = new HandlerThread("VibrationAndTouchThread");
        handlerThread.start();

        backgroundHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // handler background task
                MotionEvent event = (MotionEvent) msg.obj;
                recordTouchData(event);
            }
        };

        // get rootView
        View rootView = findViewById(android.R.id.content);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isVibrating && event.getAction() == MotionEvent.ACTION_MOVE) {
                    // record the touch event data
                    Message touchMsg = backgroundHandler.obtainMessage(2, event);
                    backgroundHandler.sendMessage(touchMsg);
                }
                return true;
            }
        });
    }

    /**
     *
     * @param repeatable
     */
    private void startVibration(int repeatable) {
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] timings = new long[] { 50, 50, 50, 50, 50, 100, 350, 250 };
            int[] amplitudes = new int[] { 77, 79, 84, 99, 143, 255, 0, 255 };
            //int repeatIndex = 1; // Do not repeat.

            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeatable));
        }
    }

    /**
     *
     */
    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
            isVibrating = false;
        }
    }

    /**
     *
     * @param event
     */
    private void recordTouchData(MotionEvent event) {
        float touchArea = event.getSize();
        float touchPressure = event.getPressure();
        float touchX = event.getX();
        float touchY = event.getY();
        long eventTime = event.getEventTime();

        // Debug --> show on Root View
        runOnUiThread(() -> touchInfoTextView.setText("\nArea: " + touchArea + "\nPressure: " + touchPressure +
                "\nX: " + touchX + "\nY: " + touchY));

        // todo--- record the event as a csv file
        saveEventDataToCsv(touchArea, touchPressure, touchX, touchY, eventTime);
    }

    /**
     * Save motion event data
     * @param area
     * @param pressure
     * @param x
     * @param y
     * @param eventTime
     */
    private void saveEventDataToCsv(float area, float pressure, float x, float y, long eventTime) {
        //File csvFile = new File(getExternalFilesDir(null), "touch_event_data.csv");
        //this.getExternalFilesDir(null).getAbsolutePath();

        // the directory:\Internal storage\Documents
        File csvFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "touch_event_data.csv");

        boolean isFileNew = !csvFile.exists();

        try (FileWriter writer = new FileWriter(csvFile, true)) { // 'true': can append

            // if file doesn't exist, insert the header line
            if (isFileNew) {
                writer.append("Timestamp,Area,Pressure,X,Y\n");
            }

            // row data
            writer.append(eventTime + ",");
            writer.append(area + ",");
            writer.append(pressure + ",");
            writer.append(x + ",");
            writer.append(y + "\n");

            // flush data to the file
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVibration();

        // Stop HandlerThread
        handlerThread.quitSafely();
    }
}