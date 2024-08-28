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

import com.example.vibrationbasedfingerprint.motorControl.MotorExecThread;


// screen data callback is here
public class MainActivity extends AppCompatActivity {

    private Vibrator vibrator;

    private Button startVibrationButton;

    private View mSensingAreaButton;

    private TextView touchInfoTextView;

    // HandlerThread for background processing
    private HandlerThread handlerThread;
    // private Handler backgroundHandler;

    // private boolean isVibrating = false;

    private static MotorExecThread mMotorThread = null;
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize vibrator
        mContext = getApplicationContext();
        // vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // UI component

        startVibrationButton = findViewById(R.id.startVibrationButton);

        mSensingAreaButton = findViewById(R.id.pressButton);

        mMotorThread = MotorExecThread.getInstance(mContext);

        handlerThread = new HandlerThread("VibrationAndTouchThread");
        handlerThread.start();
        // start event
        startVibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMotorThread.oneShot();
                // TODO: add a thread to record data, 
                // one shot: start new file, 
                // write for period of time, 
                // stop receiving data write the rest inside the queue
                // end writing and wait until next time to write a new one

            }
        });



        touchInfoTextView = findViewById(R.id.touchInfoTextView);
        mSensingAreaButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // if (isVibrating && event.getAction() == MotionEvent.ACTION_MOVE) {
                // if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                // JUST RECORD EVERYTHING

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    touchInfoTextView.setText("Touch the screen to get area and pressure");
                    // TODO: consider use this up/down to trigger the record

                } else {
                    float touchArea = event.getSize();
                    float touchPressure = event.getPressure();
                    float touchX = event.getX();
                    float touchY = event.getY();
                    long eventTime = event.getEventTime();
                    String dataStr = "\nArea: " + touchArea + 
                                     "\nPressure: " + touchPressure +
                                     "\nX: " + touchX + "\nY: " + touchY;
                    String dataStrRecord = "Area: " + touchArea + 
                                     "\tPressure: " + touchPressure +
                                     "\tX: " + touchX + "\tY: " + touchY;
                    // throw this str to wrin
                    touchInfoTextView.setText(dataStr);
                }
                return true;
            }
        });
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
//        stopVibration();

        // Stop HandlerThread
        // handlerThread.quitSafely();
        if(mMotorThread != null) {
            mMotorThread.deleteInstance();
        }

        // and exit of data writing thread
        
    }
}