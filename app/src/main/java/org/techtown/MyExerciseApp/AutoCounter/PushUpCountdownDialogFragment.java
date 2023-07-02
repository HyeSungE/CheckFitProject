package org.techtown.MyExerciseApp.AutoCounter;

import static android.hardware.Sensor.TYPE_LIGHT;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import org.techtown.MyExerciseApp.R;

public class PushUpCountdownDialogFragment extends DialogFragment implements SensorEventListener {
    private static final float MINIMUM_LIGHT_VALUE =15.0f;
    private static final int COUNTDOWN_SECONDS = 4;
    private static final int COUNTDOWN_INTERVAL = 1000;
    private CountDownTimer countDownTimer;
    private boolean isCounting;
    private int count;

    private TextView countdownTextView;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private OnCountdownCompleteListener listener;

    private TableRow tableRow;

    public interface OnCountdownCompleteListener {
        void onCountdownComplete(int totalCount);
    }
    public void setOnCountdownCompleteListener(OnCountdownCompleteListener onCountdownCompleteListener) {
        this.listener = onCountdownCompleteListener;
    }

    public static PushUpCountdownDialogFragment newInstance(TableRow tableRow, int count) {
        PushUpCountdownDialogFragment fragment = new PushUpCountdownDialogFragment();
        Bundle args = new Bundle();
        args.putInt("count", count);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        count = getArguments().getInt("count");
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(TYPE_LIGHT);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCountdown();
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelCountdown();
        sensorManager.unregisterListener(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        Dialog dialog = getDialog();
        if (dialog != null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            params.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(params);
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_push_up_counter, null);
        countdownTextView = dialogView.findViewById(R.id.countdown_textview);
        Button stopButton = dialogView.findViewById(R.id.stop_button);
        Button stopButton2 = dialogView.findViewById(R.id.stop_button2);
        ImageButton help_button= dialogView.findViewById(R.id.help_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCountAndDismiss(getArguments().getInt("count") - count);
            }
        });
        stopButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             dismiss();
            }
        });
        help_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireActivity().getApplicationContext(), "핸드폰을 바닥에 두고 전면 센서를 머리로 가려주세요 !", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(dialogView);

        return builder.create();
    }

    private void performPushUp() {
        count--;
        if (count <= 0) {
            setCountAndDismiss(getArguments().getInt("count"));
        } else {
            updateMessage();
        }
    }

    private void updateMessage() {
        countdownTextView.setText(String.valueOf(count));
    }

    private void setCountAndDismiss(int totalCount) {
        if (listener != null) {
            listener.onCountdownComplete(totalCount);
        }
        Log.d("setCountAndDismiss","setCountAndDismiss");
        dismiss();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightValue = event.values[0];
            if (lightValue < MINIMUM_LIGHT_VALUE && !isCounting) {
                performPushUp();
                isCounting=true;
            } else if (lightValue >= MINIMUM_LIGHT_VALUE && isCounting) {
                isCounting = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    private void startCountdown() {
        countDownTimer = new CountDownTimer(COUNTDOWN_SECONDS * 1000, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                updateMessage(String.valueOf(seconds));
            }

            @Override
            public void onFinish() {
                updateMessage();
            }
        };

        countDownTimer.start();
    }

    private void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    private void updateMessage(String message) {
        countdownTextView.setText(message);
    }
}
