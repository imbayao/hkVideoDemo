package com.elso.hkvideodemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private VideoUtil video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        Button startPlay = (Button) findViewById(R.id.startPlay_bt);
        Button stopPlay = (Button) findViewById(R.id.stopPlay_bt);
        SurfaceView playSurfaceView = (SurfaceView) findViewById(R.id.playSurfaceView_sv);
        video = new VideoUtil(this, playSurfaceView.getHolder());
        startPlay.setOnClickListener(this);
        stopPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startPlay_bt:
                video.userLogin("192.168.1.211", 8000, "admin", "12345");
                video.startRealPlay(1);
                break;
            case R.id.stopPlay_bt:
                video.stopRealPlay();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        video.freeSDK();
    }
}
