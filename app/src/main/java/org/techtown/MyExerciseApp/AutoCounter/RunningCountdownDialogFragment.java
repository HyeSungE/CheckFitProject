package org.techtown.MyExerciseApp.AutoCounter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.techtown.MyExerciseApp.R;

public class RunningCountdownDialogFragment extends DialogFragment implements SensorEventListener {

    private static final int REQUEST_LOCATION_PERMISSION = 03271;
    private static final long MIN_TIME_INTERVAL = 3000;
    private static final float MIN_DISTANCE_INTERVAL = 1.0f;
    private static final float MIN_DISTANCE_THRESHOLD = 2.0f;
    private static final float MIN_ACC_THRESHOLD = 20.0f;

    private TextView countdown_time,countdown_distance,countDownTextView;
    private TableRow tableRow;
    private CountDownTimer countDownTimer;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Location previousLocation; // 이전 위치 정보 저장 변수
    private float totalDistance = 0; // 누적 거리 변수 (미터 단위)
    private long startTime;
    private boolean doDistanceUpdate = false;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;

    private int elapsedTime = 0; // 경과 시간 변수 (초 단위)
    private Handler handler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedTime++;
            int minutes = elapsedTime / 60;
            int seconds = elapsedTime % 60;
            String timeText = String.format("%02d분 %02d초", minutes, seconds);
            countdown_time.setText(timeText);
            handler.postDelayed(this, 1000); // 1초마다 업데이트
        }
    };



    private RunningCountdownDialogFragment.OnCountdownCompleteListener listener;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double acceleration = Math.sqrt(x * x + y * y + z * z);

            if (acceleration > MIN_ACC_THRESHOLD) {
                Log.d("acceleration", acceleration+"");
                doDistanceUpdate = true;
            } else{
                doDistanceUpdate = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public interface OnCountdownCompleteListener {
        void onCountdownComplete(String time,float disatance);
    }
    public void setOnCountdownCompleteListener(RunningCountdownDialogFragment.OnCountdownCompleteListener onCountdownCompleteListener) {
        this.listener = onCountdownCompleteListener;
    }


    public static RunningCountdownDialogFragment newInstance(TableRow tableRow) {
        RunningCountdownDialogFragment fragment = new RunningCountdownDialogFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        locationRequest = new LocationRequest();
    }


    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (hasLocationPermission()) {
            // 위치 권한이 있는 경우 위치 업데이트 요청
            requestLocationUpdates();
        } else {
            // 위치 권한이 없는 경우 권한 요청
            requestLocationPermission();
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            // 위치 권한이 없을 경우 처리
            Toast.makeText(requireContext(), "위치 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        startTime = System.currentTimeMillis();
        totalDistance = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        Dialog dialog = getDialog();
        if (dialog != null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; // 또는 TYPE_SYSTEM_OVERLAY
            params.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(params);
        }
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_run_counter, null);
        builder.setView(dialogView);
        countdown_time = dialogView.findViewById(R.id.countdown_time);
        countdown_distance = dialogView.findViewById(R.id.countdown_distance);
        countDownTextView = dialogView.findViewById(R.id.countDownTextView);
        Button stopButton = dialogView.findViewById(R.id.stop_button);
        Button stopButton2 = dialogView.findViewById(R.id.stop_button2);
        ImageButton helpButton = dialogView.findViewById(R.id.help_button);
        LinearLayout count_layout = dialogView.findViewById(R.id.count_layout);

        stopButton.setEnabled(false);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCountdownComplete(countdown_time.getText().toString(),
                            Float.parseFloat(countdown_distance.getText().toString().split("m")[0]));
                }
                fusedLocationClient.removeLocationUpdates(locationCallback);
                dismiss();
            }
        });
        stopButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
                dismiss();
            }
        });
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireActivity().getApplicationContext(),
                        "핸드폰을 들고 뛰어주세요 !", Toast.LENGTH_SHORT).show();
            }
        });

        // 카운트다운 시작 (3초 동안)
        countDownTimer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                countDownTextView.setText(String.valueOf(seconds));
            }

            @Override
            public void onFinish() {
                countDownTextView.setVisibility(View.GONE);
                count_layout.setVisibility(View.VISIBLE);
                stopButton.setEnabled(true);
                handler.post(timerRunnable);
            }
        }.start();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity().getApplicationContext());

        locationRequest.setInterval(1000); // 위치 업데이트 간격 (밀리초)
        locationRequest.setFastestInterval(500); // 가장 빠른 위치 업데이트 간격 (밀리초)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 위치 업데이트 우선순위



        return builder.create();
    }

//    @Override
//    public void onLocationChanged(@NonNull Location location) {
//        Log.d("getLatitude",location.getLatitude()+"  "+location.getLongitude()+"");
//        if (previousLocation != null) {
//            // 이전 위치와 현재 위치 간의 거리 계산
//            float[] results = new float[1];
//            Location.distanceBetween(
//                    previousLocation.getLatitude(), previousLocation.getLongitude(),
//                    location.getLatitude(), location.getLongitude(),
//                    results
//            );
//            float distance = results[0];
//            Log.d("distance",distance+"");
//            if (distance > MIN_DISTANCE_THRESHOLD) {
//                totalDistance += distance; // 누적 거리 업데이트
//            }
//        }
//
//        // 현재 위치를 이전 위치로 설정
//        previousLocation = location;
//        updateDistance(totalDistance);
//    }

    private void updateDistance(double distance) {
        countdown_distance.setText(String.format("%.2f m", distance));
    }

    private boolean hasLocationPermission() {
        int fineLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // 위치 권한에 대한 설명이 필요한 경우 여기에 처리할 로직을 추가하세요.

            // 권한 요청
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // 위치 권한 요청 결과 처리
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용된 경우 위치 업데이트 요청
                requestLocationUpdates();
            } else {
                // 위치 권한이 거부된 경우 처리할 로직을 추가하세요.
            }
        }
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            // 위치 권한이 없을 경우 처리
            Toast.makeText(requireContext(), "위치 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
        }
    }


    private LocationCallback locationCallback = new LocationCallback() {
        private Location previousLocation;
        private float totalDistance = 0;

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                System.out.println("Latitude: " + latitude + " Longitude: " + longitude);

                if (previousLocation != null  ) {
                    float distance = previousLocation.distanceTo(location);
                    if (distance >= MIN_DISTANCE_THRESHOLD && doDistanceUpdate) {
                        totalDistance += distance; // 누적 거리 업데이트
                        updateDistance(totalDistance);
                    }
                }
                previousLocation = location;


            }
        }
    };



}
