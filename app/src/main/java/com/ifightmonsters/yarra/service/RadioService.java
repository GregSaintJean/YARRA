package com.ifightmonsters.yarra.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ifightmonsters.yarra.MainApp;
import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.data.YarraContract;
import com.ifightmonsters.yarra.sync.YarraSyncAdapter;
import com.ifightmonsters.yarra.ui.activity.MainActivity;
import com.ifightmonsters.yarra.utils.ChronoUtils;
import com.ifightmonsters.yarra.utils.NetworkUtils;

import java.io.IOException;
import java.util.Date;


/**
 * Radio service responsible for playing back music from the radio reddit streaming
 * servers.
 */
public class RadioService extends Service
        implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final String LOG = "RadioService";
    private static final String LOCK = "RadioServiceLock";

    private static final long RESYNC_INTERVAL_IN_MINUTES = 4L;
    private static final long RESYNC_INTERVAL
            = RESYNC_INTERVAL_IN_MINUTES * ChronoUtils.SECONDS_PER_MINUTE;

    private static final String ACTION
            = "com.ifightmonsters.radioreddit.service.RadioService.action";
    private static final String EXTRA
            = "com.ifightmonsters.radioreddit.service.RadioService.extra";
    private static final String BROADCAST
            = "com.ifightmonsters.radioreddit.service.RadioService.broadcast";

    private static final String ACTION_SYNC = ACTION + ".sync";
    public static final String ACTION_PLAY = ACTION + ".play";
    public static final String ACTION_STOP = ACTION + ".stop";
    public static final String BROADCAST_ERROR = BROADCAST + ".error";
    public static final String BROADCAST_STATUS = BROADCAST + ".status";
    public static final String BROADCAST_KILLED = BROADCAST + ".killed";

    public static final String EXTRA_STATION_ID = EXTRA + ".station";
    public static final String EXTRA_ERROR = EXTRA + ".error";
    public static final String EXTRA_STATUS = EXTRA + ".status";

    private final String[] STATUS_PROJECTION = {
            YarraContract.Status._ID,
            YarraContract.Status.COLUMN_RELAY
    };

    private final String STATUS_SELECTION
            = YarraContract.Status._ID + " = ?";

    private static final int SYNC_REQUEST_CODE = 1;

    private static final float DUCK_VOLUME = 0.1f;
    private static final int NOTIFY_ID = 1;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_STOPPED = 3;
    private static final int STATE_ERROR = 4;
    private static final int STATE_AUDIO_FOCUS_LOST = 5;
    private static final int STATE_WAITING_FOR_SYNC_TO_FINISH = 6;

    private static final int FOCUS_FOCUSED = 0;
    private static final int FOCUS_NOT_FOCUSED_DUCK = 1;
    private static final int FOCUS_NOT_FOCUSED = 2;

    private int mCurrentAudioFocus = FOCUS_NOT_FOCUSED;
    private int mCurrentState = STATE_IDLE;
    private long mCurrentStation;

    private final BroadcastReceiver mRadioServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                return;
            }

            String action = intent.getAction();

            if (action == null) {
                return;
            }

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                Bundle extras = intent.getExtras();

                if (extras != null
                        && extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                    interruptPlayback();
                }
            }

            if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                interruptPlayback();
            }

            if (action.equals(YarraSyncAdapter.BROADCAST_SYNC_COMPLETED)) {
                if (mCurrentState == STATE_WAITING_FOR_SYNC_TO_FINISH) {
                    attemptPlayback();
                }
            }
        }
    };

    private PendingIntent mPendingSyncIntent;

    private static RadioService sInstance;
    private MediaPlayer mPlayer;
    private NotificationManager mNotificationMgr;
    private WifiManager.WifiLock mWifiLock;
    private AudioManager mAudioManager;
    private LocalBroadcastManager mLocalBroadcastMgr;
    private AlarmManager mAlarmMgr;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, LOCK);
        mNotificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAlarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        mLocalBroadcastMgr = LocalBroadcastManager.getInstance(this);
        registerReceivers();
        mPendingSyncIntent = PendingIntent.getService(
                this,
                SYNC_REQUEST_CODE,
                new Intent(ACTION_SYNC),
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        sInstance = null;
    }

    private void registerReceivers() {
        IntentFilter internalFilter = new IntentFilter(YarraSyncAdapter.BROADCAST_SYNC_COMPLETED);
        mLocalBroadcastMgr.registerReceiver(mRadioServiceReceiver, internalFilter);
        IntentFilter externalFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        externalFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mRadioServiceReceiver, externalFilter);

    }

    private void unregisterReceivers() {
        mLocalBroadcastMgr.unregisterReceiver(mRadioServiceReceiver);
        unregisterReceiver(mRadioServiceReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        Bundle extras = intent.getExtras();

        if (TextUtils.isEmpty(action)) {
            handleEmptyIntent();
            try {
                return Service.START_NOT_STICKY;
            } finally {
                stopSelf();
            }
        }

        if (action.equals(ACTION_PLAY)) {
            if (extras != null && extras.containsKey(EXTRA_STATION_ID)) {
                handlePlayAction(extras.getLong(EXTRA_STATION_ID));
            } else {
                stopSelf();
            }
        } else if (action.equals(ACTION_STOP)) {
            handleStopAction();
        } else if (action.equals(ACTION_SYNC)) {
            handleSyncAction();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mCurrentState = STATE_ERROR;

        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(LOG, "Media Player error unknown");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e(LOG, "Server died");
                break;
        }

        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                Log.e(LOG, "Media Player IO Error");
                Intent errorIntent = new Intent(BROADCAST_ERROR);
                errorIntent.putExtra(EXTRA_ERROR, "Network error, please your connection!");
                mLocalBroadcastMgr.sendBroadcast(errorIntent);
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                Log.e(LOG, "Media Player Malformed");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Log.e(LOG, "Media Player Unsupported");
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Log.e(LOG, "Media Player timed out");
                break;
        }

        releaseAudioFocus();
        if (mWifiLock.isHeld()) mWifiLock.release();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        if (mCurrentState == STATE_IDLE
                || mCurrentState == STATE_ERROR
                || mCurrentState == STATE_WAITING_FOR_SYNC_TO_FINISH) {
            mCurrentState = STATE_ERROR;
            return;
        }

        if (mCurrentState == STATE_PLAYING) {
            stopPlayer();
            releaseMediaPlayer(false);
        }

        if (mCurrentState == STATE_STOPPED) {
            releaseMediaPlayer(false);
        }

        if (mCurrentState == STATE_PREPARING) {
            mCurrentState = STATE_PLAYING;
            mp.start();
            setupPlayingStatus();
        }

    }

    //TODO Replace with preference or stored last value

    private void interruptPlayback() {
        if (mCurrentState == STATE_PLAYING) {
            killService();
            return;
        }

        stopSelf();
    }

    public static void play(Context ctx, long id) {
        Intent intent = new Intent(ctx, RadioService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_STATION_ID, id);
        ctx.startService(intent);
    }

    public static void stop(Context ctx) {
        Intent intent = new Intent(ctx, RadioService.class);
        intent.setAction(ACTION_STOP);
        ctx.startService(intent);
    }

    public static boolean isPlaying() {
        return sInstance != null && sInstance.mCurrentState == STATE_PLAYING;
    }

    private void setupPlayingStatus() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.status_playing));
        mNotificationMgr.notify(NOTIFY_ID, builder.build());
    }

    private void setupPreparingNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.status_preparing));

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        startForeground(NOTIFY_ID, builder.build());
    }

    private void setupMediaPlayer() {
        //TODO Maybe it's best to check what state everything is in
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnPreparedListener(this);
        } else {
            mPlayer.reset();
        }

        mCurrentState = STATE_IDLE;
    }

    private void prepStreamPlayback() {
        if (mCurrentState == STATE_PREPARING
                || mCurrentState == STATE_PLAYING
                || mCurrentState == STATE_WAITING_FOR_SYNC_TO_FINISH) {
            return;
        }

        if (mCurrentState == STATE_STOPPED || mCurrentState == STATE_ERROR) {
            releaseMediaPlayer(false);
        }

        setupMediaPlayer();
        mCurrentState = STATE_PREPARING;
        setupPreparingNotification();

        retrievePlaybackData();

    }

    private void retrievePlaybackData() {

        MainApp app = (MainApp) getApplicationContext();

        Date lastSyncDate = app.getLastSyncTimestamp();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        long syncInterval = Integer.valueOf(
                sharedPref.getString(
                        getString(R.string.pref_sync_interval),
                        Integer.toString(getResources().getInteger(R.integer.default_sync_interval))));

        syncInterval = syncInterval * ChronoUtils.MILLISECONDS_PER_SECOND;

        if (ChronoUtils.isDateOldEnough(lastSyncDate, syncInterval)) {
            mCurrentState = STATE_WAITING_FOR_SYNC_TO_FINISH;
            YarraSyncAdapter.syncImmediately(this);
        } else {
            attemptPlayback();
        }

    }

    private void attemptPlayback() {

        mCurrentState = STATE_PREPARING;

        if (!mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }

        if (getAudioFocus()) {

            Cursor c =
                    getContentResolver().query(
                            YarraContract.Status.CONTENT_URI,
                            STATUS_PROJECTION,
                            STATUS_SELECTION,
                            new String[]{Long.toString(mCurrentStation)},
                            null);

            if (c.getCount() > 0 && c.moveToFirst()) {

                int relay_column_id = c.getColumnIndex(YarraContract.Status.COLUMN_RELAY);

                Uri station = Uri.parse(c.getString(relay_column_id));

                c.close();

                try {
                    mPlayer.setDataSource(this, station);
                } catch (IOException e) {
                    Log.e(LOG, e.toString());
                    mCurrentState = STATE_ERROR;
                    releaseMediaPlayer(true);
                    broadcastError(R.string.error_station_uri);
                    stopSelf();
                }

                mPlayer.prepareAsync();

            } else {
                mCurrentState = STATE_ERROR;
                Log.e(LOG, "No data from cursor came back");
                broadcastError(R.string.error_no_station_data);
                releaseMediaPlayer(true);
                stopSelf();
            }

        } else {
            Log.e(LOG, "Unable to grab audio focus");
            stopSelf();
        }

    }


    private void broadcastError(int stringRes) {
        Intent intent = new Intent(BROADCAST_ERROR);
        intent.putExtra(EXTRA_ERROR, stringRes);
        mLocalBroadcastMgr.sendBroadcast(intent);
    }

    private void stopPlayer() {
        mCurrentState = STATE_STOPPED;
        mPlayer.stop();
        releaseMediaPlayer(false);

        //TODO Update notification
    }

    private void killService() {
        if (mCurrentState == STATE_ERROR
                || mCurrentState == STATE_STOPPED
                || mCurrentState == STATE_IDLE) {
            return;
        }

        mCurrentState = STATE_STOPPED;
        mPlayer.stop();
        releaseAudioFocus();
        mWifiLock.release();
        stopForeground(true);
        releaseMediaPlayer(true);
        mLocalBroadcastMgr.sendBroadcast(new Intent(RadioService.BROADCAST_KILLED));
        stopSelf();
    }

    private boolean getAudioFocus() {

        boolean focus = mAudioManager
                .requestAudioFocus(
                        this,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        if (focus) {
            mCurrentAudioFocus = FOCUS_FOCUSED;
        }

        return focus;
    }

    private boolean releaseAudioFocus() {
        boolean not_focus = mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        if (not_focus) {
            mCurrentAudioFocus = FOCUS_NOT_FOCUSED;
        }

        return not_focus;
    }

    private void releaseMediaPlayer(boolean type) {
        //TODO implement the resetting of state variables maybe
        mPlayer.reset();
        if (type) {
            mPlayer.release();
            mPlayer = null;
        } else {
            mCurrentState = STATE_IDLE;
        }

    }

    private void handleEmptyIntent() {
        broadcastError(R.string.error_empty_intent);
    }

    private void handlePlayAction(long id) {

        if (mCurrentState == STATE_WAITING_FOR_SYNC_TO_FINISH) {
            return;
        }

        if (mCurrentState == STATE_ERROR) {
            releaseMediaPlayer(false);
        }

        if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_PREPARING) {

            if (mCurrentState == STATE_PLAYING) {
                stopPlayer();
            }

            if (mCurrentState == STATE_PREPARING) {
                mCurrentState = STATE_STOPPED;
                releaseMediaPlayer(false);
            }
        }

        if (!NetworkUtils.hasNetworkConnectivity(this)) {
            broadcastError(R.string.error_connectivity);
            killService();
            return;
        }

        mCurrentStation = id;
        prepStreamPlayback();
    }

    private void handleStopAction() {
        if (mCurrentState == STATE_STOPPED || mCurrentState == STATE_IDLE) {
            return;
        }

        killService();
    }

    private void handleSyncAction() {
        YarraSyncAdapter.syncImmediately(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        if (mCurrentState == STATE_ERROR
                || mCurrentState == STATE_IDLE
                || mCurrentState == STATE_STOPPED
                || mCurrentState == STATE_WAITING_FOR_SYNC_TO_FINISH) {
            return;
        }

        if (mCurrentState == STATE_PREPARING) {
            killService();
        }

        if (mCurrentState == STATE_PLAYING) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    mPlayer.setVolume(1.0f, 1.0f);
                    mCurrentAudioFocus = FOCUS_FOCUSED;
                    mCurrentState = STATE_PLAYING;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    killService();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
                    mCurrentAudioFocus = FOCUS_NOT_FOCUSED_DUCK;
                    mCurrentState = STATE_AUDIO_FOCUS_LOST;
                    break;
                default:
                    break;
            }
        }

    }

    //TODO use these methods
    private void startSyncInterval() {
        mAlarmMgr.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + RESYNC_INTERVAL,
                RESYNC_INTERVAL,
                mPendingSyncIntent
        );
    }

    //TODO use these methods
    private void stopSyncInterval() {
        mAlarmMgr.cancel(mPendingSyncIntent);
    }

}
