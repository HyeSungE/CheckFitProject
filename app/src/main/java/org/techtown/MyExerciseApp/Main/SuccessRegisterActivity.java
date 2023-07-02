package org.techtown.MyExerciseApp.Main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.techtown.MyExerciseApp.MyClass.BackPressHandler;
import org.techtown.MyExerciseApp.R;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class SuccessRegisterActivity extends AppCompatActivity {
    private KonfettiView konfettiView = null;
    private Shape.DrawableShape drawableShape = null;
    private Button goToLoginBt;
    private BackPressHandler backPressHandler = new BackPressHandler(this);

    @Override
    public void onBackPressed() {
        if(backPressHandler.onBackPressed()){
            android.app.AlertDialog.Builder backAlert = new android.app.AlertDialog.Builder(this);
            backAlert.setMessage("시작 화면으로 이동합니다.").setTitle("안내")
                    .setPositiveButton("이동", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(SuccessRegisterActivity.this, StartActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //nothing
                        }
                    });
            android.app.AlertDialog backAlertDialog = backAlert.create();
            backAlertDialog.show();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("SuccessRegisterActivity start");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_register);

        konfettiView = findViewById(R.id.konfettiView);
        goToLoginBt = findViewById(R.id.success_go_to_login_bt);
        final Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart);
        drawableShape = new Shape.DrawableShape(drawable, true);
        EmitterConfig emitterConfig = new Emitter(5, TimeUnit.SECONDS).perSecond(30);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.RIGHT - 45)
                        .spread(Spread.WIDE)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, drawableShape))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Position.Relative(0.0, 0.5))
                        .build(),
                new PartyFactory(emitterConfig)
                        .angle(Angle.LEFT + 45)
                        .spread(Spread.WIDE)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, drawableShape))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Position.Relative(1.0, 0.5))
                        .build(),

        new PartyFactory(emitterConfig)
                .spread(360)
                .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, drawableShape))
                .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                .setSpeedBetween(0f, 30f)
                .position(new Position.Relative(0.5, 0.3))
                .build()
        );


        goToLoginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SuccessRegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });



    }
}