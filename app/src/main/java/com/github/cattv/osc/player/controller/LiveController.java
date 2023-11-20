package com.github.cattv.osc.player.controller;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.cattv.osc.R;
import com.github.cattv.osc.base.App;
import com.github.cattv.osc.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 直播控制器
 */

public class LiveController extends BaseController {
    protected ProgressBar mLoading;
    private LinearLayout mProgressRoot;
    private TextView mProgressText;
    private ImageView mProgressIcon;
    private int minFlingDistance = 100;             //最小识别距离
    private int minFlingVelocity = 10;              //最小识别速度

    public LiveController(@NotNull Context context) {
        super(context);
        mHandlerCallback = new HandlerCallback() {
            @Override
            public void callback(Message msg) {
                switch (msg.what) {
                    case 1000: { // seek 刷新
                        mProgressRoot.setVisibility(VISIBLE);
                        break;
                    }
                    case 1001: { // seek 关闭
                        mProgressRoot.setVisibility(GONE);
                        break;
                    }
                }
            }
        };
    }

    @Override
    protected int getLayoutId() {
        return R.layout.player_live_control_view;
    }

    @Override
    protected void initView() {
        super.initView();
        mLoading = findViewById(R.id.loading);
        mProgressRoot = findViewById(R.id.tv_progress_container);
        mProgressIcon = findViewById(R.id.tv_progress_icon);
        mProgressText = findViewById(R.id.tv_progress_text);
    }

    public interface LiveControlListener {
        boolean singleTap();

        void longPress();

        void playStateChanged(int playState);

        void changeSource(int direction);

        void onPlayNextOrPrevious(int direction);

        void onShowChannelList();

        void onShowSettingGroup();

        boolean getListOrSettingLayoutVisible();

    }

    private LiveController.LiveControlListener listener = null;

    public void setListener(LiveController.LiveControlListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (listener.singleTap())
            return true;
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        listener.longPress();
        super.onLongPress(e);
    }

    @Override
    public void hideProgressLayout() {
        mHandler.removeMessages(1001);
        mHandler.sendEmptyMessage(1001);
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        listener.playStateChanged(playState);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > minFlingDistance && Math.abs(velocityX) > minFlingVelocity) {
            listener.changeSource(-1);          //左滑
        } else if (e2.getX() - e1.getX() > minFlingDistance && Math.abs(velocityX) > minFlingVelocity) {
            listener.changeSource(1);           //右滑
        } else if (e1.getY() - e2.getY() > minFlingDistance && Math.abs(velocityY) > minFlingVelocity) {
        } else if (e2.getY() - e1.getY() > minFlingDistance && Math.abs(velocityY) > minFlingVelocity) {
        }
        return false;
    }

    private boolean simSlideStart = false;
    private int simSeekPosition = 0;
    private long simSlideOffset = 0;

    public void tvSlideStart(int dir) {
        int duration = (int) mControlWrapper.getDuration();
        if (duration <= 0)
            return;
        if (!simSlideStart) {
            simSlideStart = true;
        }
        // 每次10秒
        simSlideOffset += (10000.0f * dir);
        int currentPosition = (int) mControlWrapper.getCurrentPosition();
        int position = (int) (simSlideOffset + currentPosition);
        if (position > duration) position = duration;
        if (position < 0) position = 0;
        updateSeekUI(currentPosition, position, duration);
        simSeekPosition = position;
    }

    public void tvSlideStop() {
        if (!simSlideStart)
            return;
        mControlWrapper.seekTo(simSeekPosition);
        if (!mControlWrapper.isPlaying())
            mControlWrapper.start();
        simSlideStart = false;
        simSeekPosition = 0;
        simSlideOffset = 0;
    }

    @Override
    protected void updateSeekUI(int curr, int seekTo, int duration) {
        super.updateSeekUI(curr, seekTo, duration);
        if (seekTo > curr) {
            mProgressIcon.setImageResource(R.drawable.icon_pre);
        } else {
            mProgressIcon.setImageResource(R.drawable.icon_back);
        }
        mProgressText.setText(PlayerUtils.stringForTime(seekTo) + " / " + PlayerUtils.stringForTime(duration));
        mHandler.sendEmptyMessage(1000);
        hideLoadingLayout();
        mHandler.removeMessages(1001);
        mHandler.sendEmptyMessageDelayed(1001, 1000);
    }

    private boolean isKeepDown = false;
    private long firstDownTime;

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean isInPlayback = isInPlaybackState();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!isKeepDown) {
                firstDownTime = System.currentTimeMillis();
                isKeepDown = true;
            }
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - firstDownTime > 1000) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    tvSlideStart(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1);
                    return true;
                }
            }

            if (keyCode == KeyEvent.KEYCODE_MENU) {
                listener.onShowSettingGroup();
                return true;
            } else if (!listener.getListOrSettingLayoutVisible()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false))
                            listener.onPlayNextOrPrevious(1);
                        else {
                            listener.onPlayNextOrPrevious(-1);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false))
                            listener.onPlayNextOrPrevious(-1);
                        else {
                            listener.onPlayNextOrPrevious(1);
                        }
                        break;
                }
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            isKeepDown = false;
            long currentTimeMillis = System.currentTimeMillis();

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (currentTimeMillis - firstDownTime > 1000) {
                    if (isInPlayback) {
                        tvSlideStop();
                    }
                } else {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        listener.changeSource(1);
                    } else {
                        listener.changeSource(-1);
                    }
                }
                return true;
            }
            if (keyCode==KeyEvent.KEYCODE_DPAD_CENTER||keyCode==KeyEvent.KEYCODE_ENTER||keyCode==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE){
                if (currentTimeMillis - firstDownTime > 1000) {
                    listener.onShowSettingGroup();
                    return true;
                } else {
                    listener.onShowChannelList();
                    return false;
                }

            }
        }
        return super.dispatchKeyEvent(event);
    }

}
