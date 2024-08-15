package com.example.vibrationbasedfingerprint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
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
        // 获取TextView用于显示触摸信息 -- debug
        touchInfoTextView = findViewById(R.id.touchInfoTextView);

        // 创建并启动HandlerThread
        handlerThread = new HandlerThread("VibrationAndTouchThread");
        handlerThread.start();

        // 在HandlerThread的Looper中创建一个Handler
        backgroundHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // 处理后台任务
                MotionEvent event = (MotionEvent) msg.obj;
                recordTouchData(event);
            }
        };

        // 获取根布局视图
        View rootView = findViewById(android.R.id.content);

        // 设置全局触摸监听器
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isVibrating && event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 将触摸数据记录任务发送到后台线程
                    Message touchMsg = backgroundHandler.obtainMessage(2, event);
                    backgroundHandler.sendMessage(touchMsg);
                }
                return true;
            }
        });
    }

    // begin vibration
    private void startVibration(int repeatable) {
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] timings = new long[] { 50, 50, 50, 50, 50, 100, 350, 250 };
            int[] amplitudes = new int[] { 77, 79, 84, 99, 143, 255, 0, 255 };
            //int repeatIndex = 1; // Do not repeat.

            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeatable));
        }
    }

    // stop vibration
    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
            isVibrating = false;
        }
    }

    // 记录触摸数据（此方法在后台线程中调用）
    private void recordTouchData(MotionEvent event) {
        float touchArea = event.getSize();
        float touchPressure = event.getPressure();
        float touchX = event.getX();
        float touchY = event.getY();

        // 将结果传回主线程以更新UI
        runOnUiThread(() -> touchInfoTextView.setText("Area: " + touchArea + "\nPressure: " + touchPressure +
                "\nX: " + touchX + "\nY: " + touchY));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVibration();

        // 停止HandlerThread
        handlerThread.quitSafely();
    }
}