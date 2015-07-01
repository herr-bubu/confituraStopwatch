package com.softwaremill.confitura.stopper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StopperActivity extends Activity {

    private static final String INITIAL_STATE = "45";
    private static final String START_BTN = "Start";
    private static final String STOP_BTN = "Stop";

    private static final int TIME_LIMIT = 45;
    private Button startBtn;
    private boolean isStarted;
    private int passedSeconds = 45;
    private ScheduledFuture handler;
    private ScheduledFuture clockHandler;
    private TextView separator;
    private TextView minutes;
    private TextView seconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopper);
        separator = (TextView) findViewById(R.id.timeSeparator);
        minutes = (TextView) findViewById(R.id.minutes);
        seconds = (TextView) findViewById(R.id.seconds);

        final ScheduledExecutorService service = Executors.newScheduledThreadPool(3);

        startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isStarted) {
                    runScheduler(service);
                    isStarted = true;
                    startBtn.setText(STOP_BTN);
                } else {
                    resetHandler.sendEmptyMessage(0);
                }
            }
        });
    }

    private void runScheduler(final @NonNull ScheduledExecutorService service) {
        handler = service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                passedSeconds--;
                updateHandler.sendEmptyMessage(passedSeconds);
            }
        }, 0, 1, TimeUnit.SECONDS);

        clockHandler = service.schedule(new Runnable() {
            @Override
            public void run() {
                resetHandler.sendEmptyMessage(0);
            }
        }, TIME_LIMIT, TimeUnit.SECONDS);

    }

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if ((msg.what % 2) == 0) {
                separator.setVisibility(View.VISIBLE);
            } else {
                separator.setVisibility(View.INVISIBLE);
            }

            if ((msg.what % 5) == 0 && msg.what >= 10) {
                Log.d("Clock", "Minutes left: " + msg.what);
                minutes.setText(Integer.toString(msg.what));
            } else if (msg.what < 10 && msg.what >= 5) {
                minutes.setText(Integer.toString(msg.what));
            } else if (msg.what < 5) {
                minutes.setText("Pytania");
                minutes.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 350);
                separator.setVisibility(View.INVISIBLE);
                seconds.setVisibility(View.INVISIBLE);
            }
        }
    };

    private Handler resetHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            isStarted = false;
            passedSeconds = 45;
            handler.cancel(true);
            clockHandler.cancel(true);
            minutes.setText(INITIAL_STATE);
            startBtn.setText(START_BTN);
            minutes.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 400);
            seconds.setVisibility(View.VISIBLE);
            separator.setVisibility(View.VISIBLE);
        }
    };
}
