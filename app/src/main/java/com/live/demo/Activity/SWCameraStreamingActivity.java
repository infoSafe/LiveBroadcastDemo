package com.live.demo.Activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.live.demo.R;
import com.live.demo.Ui.CameraPreviewFrameView;
import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.http.DnspodFree;
import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.Resolver;
import com.qiniu.pili.droid.streaming.CameraStreamingSetting;
import com.qiniu.pili.droid.streaming.MediaStreamingManager;
import com.qiniu.pili.droid.streaming.StreamingProfile;
import com.qiniu.pili.droid.streaming.StreamingState;
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;

import static com.qiniu.pili.droid.streaming.AVCodecType.SW_VIDEO_WITH_SW_AUDIO_CODEC;

public class SWCameraStreamingActivity extends Activity implements StreamingStateChangedListener,View.OnClickListener{
    private static final String TAG = "LiveAcivity";
    private MediaStreamingManager mMediaStreamingManager;
    private StreamingProfile mProfile;
    private  CameraPreviewFrameView cameraPreviewFrameView;
    private TextView tv_show_name;

    private ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swcamera_streaming);
        initView();
    }

    public void initView(){
        tv_show_name = findViewById(R.id.tv_show_name);

        cameraPreviewFrameView = findViewById(R.id.cameraPreview_surfaceView);

        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);

        String publishURLFromServer = getIntent().getStringExtra("stream_publish_url");
        String name = getIntent().getStringExtra("name");
        tv_show_name.setText(name);
        Log.d(TAG, "initView: ===>"+publishURLFromServer);
            mProfile = new StreamingProfile();
        try {
            mProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_HIGH1)
                    .setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM2)
                    .setQuicEnable(false)//RMPT or QUIC
                    .setVideoQuality(StreamingProfile.VIDEO_QUALITY_MEDIUM1)
                    .setEncodingOrientation(StreamingProfile.ENCODING_ORIENTATION.LAND )//横竖屏
                    .setEncodingSizeLevel(StreamingProfile.VIDEO_ENCODING_HEIGHT_720)
                    .setBitrateAdjustMode(StreamingProfile.BitrateAdjustMode.Auto)//自适应码率
                    .setEncoderRCMode(StreamingProfile.EncoderRCModes.QUALITY_PRIORITY)
                    .setDnsManager(getMyDnsManager())
                    .setStreamStatusConfig(new StreamingProfile.StreamStatusConfig(3))
                    .setSendingBufferProfile(new StreamingProfile.SendingBufferProfile(0.2f, 0.8f, 3.0f, 20 * 1000))
                    .setPublishUrl(publishURLFromServer);//设置推流地址

                     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        CameraStreamingSetting setting = new CameraStreamingSetting();
            setting.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT) // 摄像头切换
                    .setContinuousFocusModeEnabled(true)//开启对焦
                    .setFocusMode(CameraStreamingSetting.FOCUS_MODE_CONTINUOUS_VIDEO)//自动对焦
                    .setFaceBeautySetting(new CameraStreamingSetting.FaceBeautySetting(1.0f, 1.0f, 1.0f))//美颜 磨皮，美白，红润 取值范围为[0.0f, 1.0f]
                    .setVideoFilter(CameraStreamingSetting.VIDEO_FILTER_TYPE.VIDEO_FILTER_BEAUTY)
                    .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM)
                    .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9);
            mMediaStreamingManager = new MediaStreamingManager(this, cameraPreviewFrameView, SW_VIDEO_WITH_SW_AUDIO_CODEC);
            mMediaStreamingManager.prepare(setting, mProfile);
            mMediaStreamingManager.setStreamingStateListener(this);

    }

    @Override
    public void onStateChanged(StreamingState streamingState, Object o) {
        switch (streamingState) {
            case PREPARING:
                    Log.d(TAG, "onStateChanged: ===>"+"准备");
                break;
            case READY:
                startStreaming();
                break;
            case CONNECTING:
                Log.d(TAG, "onStateChanged: ===>"+"连接");
                break;
            case STREAMING:
                Log.d(TAG, "onStateChanged: ===>"+"已发送");
                break;
            case SHUTDOWN:
                Log.d(TAG, "onStateChanged: ===>"+"推流结束");
                break;
            case IOERROR:
                Log.d(TAG, "onStateChanged: ===>"+"IO异常");
                break;
            case SENDING_BUFFER_EMPTY:
                break;
            case SENDING_BUFFER_FULL:
                break;
            case AUDIO_RECORDING_FAIL:
                break;
            case OPEN_CAMERA_FAIL:
                break;
            case DISCONNECTED:
                Log.d(TAG, "onStateChanged: ===>"+"断开连接");
                break;
        }
    }

    /**
     * @author:
     * @create at: 2018/11/14  15:32
     * @Description: 开始推流
     */
    protected void startStreaming() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMediaStreamingManager.startStreaming();
                Log.d(TAG, "run: ===>"+"推流");
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaStreamingManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaStreamingManager.pause();
    }

    /**
     * @author:
     * @create at: 2018/11/14  10:57
     * @Description: 防止 Dns 被劫持
     */
    private static DnsManager getMyDnsManager() {
        IResolver r0 = null;
        IResolver r1 = new DnspodFree();
        IResolver r2 = AndroidDnsServer.defaultResolver();
        try {
            r0 = new Resolver(InetAddress.getByName("119.29.29.29"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new DnsManager(NetworkInfo.normal, new IResolver[]{r0, r1, r2});
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }
}
