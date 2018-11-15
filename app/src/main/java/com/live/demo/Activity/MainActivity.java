package com.live.demo.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.live.demo.R;
import com.live.demo.Util.Util;

import java.util.UUID;

public class MainActivity extends Activity  implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private static final String GENERATE_STREAM_TEXT = "http://api-demo.qnsdk.com/v1/live/stream/";

    private TextView tv_start;

    private EditText et_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    public void initView(){
        tv_start = findViewById(R.id.tv_start);
        tv_start.setOnClickListener(this);
        et_name  = findViewById(R.id.et_name);
    }


    @Override
    public void onClick(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
              final String tlpath =   genPublishURL();
//                video_Audio
                String name = et_name.getText().toString().trim();
                Intent intent = new Intent(MainActivity.this, SWCameraStreamingActivity.class);
//                传推流地址
                intent.putExtra("stream_publish_url", tlpath);
                intent.putExtra("name", name);
                startActivity(intent);

            }
        }).start();
    }


    /**
     * @author:
     * @create at: 2018/11/13  13:01
     * @Description: 获取推流地址
     */
    private String genPublishURL() {
        String publishUrl = Util.syncRequest(GENERATE_STREAM_TEXT + UUID.randomUUID());
        return publishUrl;
    }
}
