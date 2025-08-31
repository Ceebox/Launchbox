package com.chadderbox.launcherbox.components;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chadderbox.launcherbox.utils.NotificationListener;
import com.chadderbox.launcherbox.R;

import java.util.List;

public final class NowPlayingView {

    private final Activity mActivity;
    private MediaSessionManager mSessionManager;
    private MediaController mController;
    private LinearLayout mContainer;
    private ImageView mSongArt;
    private TextView mSongTitle, mSongArtist;
    private ImageButton mBtnPrevious, mBtnPlayPause, mBtnNext;

    private final MediaController.Callback mMediaCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            mActivity.runOnUiThread(NowPlayingView.this::updateNowPlayingUI);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            mActivity.runOnUiThread(NowPlayingView.this::updateNowPlayingUI);
        }
    };

    public NowPlayingView(Activity mainApp) {
        mActivity = mainApp;
    }

    public void initialize() {
        mContainer = mActivity.findViewById(R.id.now_playing);
        mSongArt = mActivity.findViewById(R.id.song_art);
        mSongTitle = mActivity.findViewById(R.id.song_title);
        mSongArtist = mActivity.findViewById(R.id.song_artist);
        mBtnPrevious = mActivity.findViewById(R.id.btn_prev);
        mBtnPlayPause = mActivity.findViewById(R.id.btn_play_pause);
        mBtnNext = mActivity.findViewById(R.id.btn_next);

        mContainer.setVisibility(View.GONE);
        setupMediaController();
    }

    private void setupMediaController() {

        if (!hasNotificationAccess(mActivity)) {
            Log.w("NowPlayingView", "Notification access missing.");
            requestNotificationAccess(mActivity);
            return;
        }

        try {
            mSessionManager = (MediaSessionManager) mActivity.getSystemService(Context.MEDIA_SESSION_SERVICE);

            var notificationListener = new ComponentName(mActivity, NotificationListener.class);
            mSessionManager.addOnActiveSessionsChangedListener(
                this::handleSessionsChanged,
                notificationListener
            );

            var sessions = mSessionManager.getActiveSessions(notificationListener);

            handleSessionsChanged(sessions);

        } catch (SecurityException e) {
            Log.e("NowPlayingView", "Missing notification access permission.", e);
        }
    }

    private void handleSessionsChanged(List<MediaController> controllers) {
        if (controllers == null || controllers.isEmpty()) {
            mController = null;
            mContainer.setVisibility(View.GONE);
            Log.d("NowPlayingView", "No active sessions found.");
            return;
        }

        mController = controllers.get(0);
        Log.d("NowPlayingView", "Connected to " + mController.getPackageName());

        mContainer.setVisibility(View.VISIBLE);
        updateNowPlayingUI();

        mController.unregisterCallback(mMediaCallback);
        mController.registerCallback(mMediaCallback);
    }

    private void updateNowPlayingUI() {
        if (mController == null) {
            mContainer.setVisibility(View.GONE);
            return;
        }

        var md = mController.getMetadata();

        if (md != null) {
            mContainer.setVisibility(View.VISIBLE);
            var title = md.getString(MediaMetadata.METADATA_KEY_TITLE);
            var artist = md.getString(MediaMetadata.METADATA_KEY_ARTIST);
            var art = md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

            mSongTitle.setText(title != null ? title : "");
            mSongArtist.setText(artist != null ? artist : "");
            mSongArt.setImageBitmap(art);
        } else {
            mContainer.setVisibility(View.GONE);
            mSongTitle.setText("");
            mSongArtist.setText("");
            mSongArt.setImageBitmap(null);
        }

        var state = mController.getPlaybackState();
        if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
            mBtnPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            mBtnPlayPause.setImageResource(R.drawable.ic_play_arrow);
        }

        mBtnPlayPause.setOnClickListener(v -> {
            var controllerPlaybackState = mController.getPlaybackState();
            if (controllerPlaybackState != null && controllerPlaybackState.getState() == PlaybackState.STATE_PLAYING) {
                mController.getTransportControls().pause();
            } else {
                mController.getTransportControls().play();
            }
        });

        mBtnNext.setOnClickListener(v -> {
            var controllerPlaybackState = mController.getPlaybackState();
            if (controllerPlaybackState != null &&
                (controllerPlaybackState.getActions() & PlaybackState.ACTION_SKIP_TO_NEXT) != 0) {
                mController.getTransportControls().skipToNext();
            }
        });

        mBtnPrevious.setOnClickListener(v -> {
            var controllerPlaybackState = mController.getPlaybackState();
            if (controllerPlaybackState != null &&
                (controllerPlaybackState.getActions() & PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0) {
                mController.getTransportControls().skipToPrevious();
            }
        });
    }

    private boolean hasNotificationAccess(Context context) {
        String enabledListeners = android.provider.Settings.Secure.getString(
            context.getContentResolver(),
            "enabled_notification_listeners"
        );
        return enabledListeners != null &&
            enabledListeners.contains(context.getPackageName());
    }

    private void requestNotificationAccess(Activity activity) {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        activity.startActivity(intent);
    }
}
