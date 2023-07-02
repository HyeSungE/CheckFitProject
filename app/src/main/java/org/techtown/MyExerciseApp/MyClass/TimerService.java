package org.techtown.MyExerciseApp.MyClass;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

import androidx.annotation.Nullable;

public class TimerService extends Service {

    private static final String TAG = "TimerService";

    private Chronometer chronometer;
    private long timeElapsed;
    private boolean isTimerRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        isTimerRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("start_timer")) {
                    startTimer();
                } else if (action.equals("stop_timer")) {
                    stopTimer();
                }
            }
        }

        // START_STICKY: 서비스가 종료되었을 때 시스템이 서비스를 재생성하고 onStartCommand()를 호출합니다.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        stopTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // 서비스에서 바인딩을 지원하지 않음
        return null;
    }

    private void startTimer() {
        if (!isTimerRunning) {
            if (chronometer == null) {
                chronometer = new Chronometer(this);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        timeElapsed = SystemClock.elapsedRealtime() - chronometer.getBase();
                        Log.d(TAG, "Time elapsed: " + timeElapsed);
                    }
                });
            }
            chronometer.start();
            isTimerRunning = true;
            Log.d(TAG, "Timer started");
        }
    }

    private void stopTimer() {
        if (isTimerRunning) {
            chronometer.stop();
            timeElapsed = SystemClock.elapsedRealtime() - chronometer.getBase();
            isTimerRunning = false;
            Log.d(TAG, "Timer stopped");
        }
    }
}
