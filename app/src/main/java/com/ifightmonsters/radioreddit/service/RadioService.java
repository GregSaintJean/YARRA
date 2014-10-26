package com.ifightmonsters.radioreddit.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ifightmonsters.radioreddit.R;
import com.ifightmonsters.radioreddit.network.RadioReddit;
import com.ifightmonsters.radioreddit.sync.RadioRedditSyncAdapter;
import com.ifightmonsters.radioreddit.ui.activity.MainActivity;
import com.ifightmonsters.radioreddit.utils.ChronoUtils;
import com.ifightmonsters.radioreddit.utils.NetworkUtils;

import java.io.IOException;

/**
 * Created by Gregory on 10/4/2014.
 */
public class RadioService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener{

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

    public static final String EXTRA_STATION = EXTRA + ".station";
    public static final String EXTRA_ERROR = EXTRA + ".error";
    public static final String EXTRA_STATUS = EXTRA + ".status";

    public static final String EXTRA_HEADSET_STATE = "state";
    public static final String EXTRA_HEADSET_NAME = "name";
    public static final String EXTRA_HEADSET_MICROPHONE = "microphone";

    private static final int SYNC_REQUEST_CODE = 1;

    private static final float DUCK_VOLUME = 0.1f;
    private static final int NOTIFY_ID = 1;

    public static final int STATION_MAIN = 0;
    public static final int STATION_ELECTRONIC = 1;
    public static final int STATION_INDIE = 2;
    public static final int STATION_HIPHOP = 3;
    public static final int STATION_ROCK = 4;
    public static final int STATION_METAL = 5;
    public static final int STATION_TALK = 6;
    public static final int STATION_RANDOM = 7;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_STOPPED = 3;
    private static final int STATE_ERROR = 4;
    private static final int STATE_AUDIO_FOCUS_LOST = 5;
    private static final int STATE_RETRIEVING = 6;

    private static final int FOCUS_FOCUSED = 0;
    private static final int FOCUS_NOT_FOCUSED_DUCK = 1;
    private static final int FOCUS_NOT_FOCUSED = 2;

    private int mCurrentAudioFocus = FOCUS_NOT_FOCUSED;
    private int mCurrentState = STATE_IDLE;

    private final BroadcastReceiver mRadioServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(context == null || intent == null){
                return;
            }

            String action = intent.getAction();

            if(action == null){
                return;
            }

            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){

                Bundle extras = intent.getExtras();

                if(extras != null
                        && extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)){
                    interruptPlayback();
                }
            }

            if(action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
                interruptPlayback();
            }
        }
    };

    private final PendingIntent mPendingSyncIntent =
            PendingIntent.getService(
                    this,
                    SYNC_REQUEST_CODE,
                    new Intent(ACTION_SYNC),
                    PendingIntent.FLAG_CANCEL_CURRENT);

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
        mAlarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        mLocalBroadcastMgr = LocalBroadcastManager.getInstance(this);
        registerReceivers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        sInstance = null;
    }

    private void registerReceivers(){
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mRadioServiceReceiver, filter);
    }

    private void unregisterReceivers(){
        unregisterReceiver(mRadioServiceReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        Bundle extras = intent.getExtras();

        if(TextUtils.isEmpty(action)){
            Log.d(LOG, "Empty intent");
            handleEmptyIntent();
            try{
                return Service.START_NOT_STICKY;
            } finally{
                stopSelf();
            }
        }

        if(action.equals(ACTION_PLAY)){
            Log.d(LOG, "Received play action");
            if(extras != null && extras.containsKey(EXTRA_STATION)){
                handlePlayAction(extras.getInt(EXTRA_STATION));
            } else {
                handlePlayAction(null);
            }
        } else if(action.equals(ACTION_STOP)){
            Log.d(LOG, "Received stop action");
            handleStopAction();
        } else if(action.equals(ACTION_SYNC)){
            Log.d(LOG, "Received sync action");
            handleSyncAction();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG, "onCompletion called");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mCurrentState = STATE_ERROR;
        Log.d(LOG, "onError called. what= " + String.valueOf(what) + " extra= " + String.valueOf(extra));

        switch(what){
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(LOG, "Media Player error unknown");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e(LOG, "Server died");
                break;
        }

        switch(extra){
            case MediaPlayer.MEDIA_ERROR_IO:
                Log.e(LOG, "Media Player IO Error");
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
        if(mWifiLock.isHeld()) mWifiLock.release();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG, "onPrepared called");

        if(mCurrentState == STATE_IDLE
                || mCurrentState == STATE_ERROR){
            Log.d(LOG, "RadioService not in correct state. returning....");
            return;
        }

        if(mCurrentState == STATE_PLAYING){
            stopPlayer();
            releaseMediaPlayer(false);
        }

        if(mCurrentState == STATE_STOPPED){
            releaseMediaPlayer(false);
        }

        if(mCurrentState == STATE_PREPARING){
            mCurrentState = STATE_PLAYING;
            mp.start();
            setupPlayingStatus();
        }

    }

    //TODO Replace with preference or stored last value
    private int mCurrentStation = STATION_MAIN;

    private void interruptPlayback(){
        if(mCurrentState == STATE_PLAYING){
            killService();
            return;
        }

        stopSelf();
    }

    public static void play(Context ctx){
        play(ctx, null);
    }

    public static void play(Context ctx, Integer station){
        Intent intent = new Intent(ctx, RadioService.class);
        intent.setAction(ACTION_PLAY);

        if(station != null){
            intent.putExtra(EXTRA_STATION, station);
        }

        ctx.startService(intent);
    }

    public static void stop(Context ctx){
        Intent intent = new Intent(ctx, RadioService.class);
        intent.setAction(ACTION_STOP);
        ctx.startService(intent);
    }

    public static boolean isPlaying(){
        return sInstance != null && sInstance.mCurrentState == STATE_PLAYING;
    }

    private void setupPlayingStatus(){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.status_playing));
        mNotificationMgr.notify(NOTIFY_ID, builder.build());
    }

    private void setupPreparingNotification(){
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

    private void setupMediaPlayer(){
        //TODO Maybe it's best to check what state everything is in
        Log.d(LOG, "setupMediaPlayer called");
        if(mPlayer == null){
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

    private void playStream(){
        Log.d(LOG, "playStream called");
        if(mCurrentState == STATE_PREPARING || mCurrentState == STATE_PLAYING){
            return;
        }

        if(mCurrentState == STATE_STOPPED || mCurrentState == STATE_ERROR){
            releaseMediaPlayer(false);
        }

        setupMediaPlayer();
        mCurrentState = STATE_PREPARING;
        setupPreparingNotification();

        if(!mWifiLock.isHeld()){
            mWifiLock.acquire();
        }

        if(getAudioFocus()){
            Uri station;
            station = RadioReddit.BACKUP_STATIONS[mCurrentStation];
            try{
                mPlayer.setDataSource(this, station);
            } catch(IOException e){
                //TODO Maybe reset state variables
                Log.e(LOG, e.toString());
                mCurrentState = STATE_ERROR;
                releaseMediaPlayer(true);
                broadcastError(R.string.error_station_uri);
                stopSelf();
            }

            mPlayer.prepareAsync();
        } else {
            Log.e(LOG, "Unable to grab audio focus");
            stopSelf();
        }

    }

    private void broadcastError(int stringRes){
        Intent intent = new Intent(BROADCAST_ERROR);
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_ERROR, stringRes);
        intent.putExtras(extras);
        mLocalBroadcastMgr.sendBroadcast(intent);
    }

    private void stopPlayer(){
        Log.d(LOG, "stopPlayer called");
        mCurrentState = STATE_STOPPED;
        mPlayer.stop();
        releaseMediaPlayer(false);

        //TODO Update notification
    }

    private void killService(){
        Log.d(LOG, "killService called");
        if(mCurrentState == STATE_ERROR
                || mCurrentState == STATE_STOPPED
                || mCurrentState == STATE_IDLE){
            return;
        }

        mCurrentState = STATE_STOPPED;
        mPlayer.stop();
        releaseAudioFocus();
        mWifiLock.release();
        stopForeground(true);
        releaseMediaPlayer(true);
        stopSelf();
    }

    private boolean getAudioFocus(){

        boolean focus = mAudioManager
                .requestAudioFocus(
                        this,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        if(focus){
            mCurrentAudioFocus = FOCUS_FOCUSED;
        }

        return focus;
    }

    private boolean releaseAudioFocus(){
        boolean not_focus = mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        if(not_focus){
            mCurrentAudioFocus = FOCUS_NOT_FOCUSED;
        }

        return not_focus;
    }

    private void releaseMediaPlayer(boolean type){
        Log.d(LOG, "releaseMediaPlayer called");
        //TODO implement the resetting of state variables maybe
        mPlayer.reset();
        if(type){
            mPlayer.release();
            mPlayer = null;
        } else {
            mCurrentState = STATE_IDLE;
        }

    }

    private void handleEmptyIntent(){
        Log.d(LOG, "handleEmptyAction called");
        broadcastError(R.string.error_empty_intent);
    }

    private void handlePlayAction(Integer station){

        if(mCurrentState == STATE_ERROR){
            releaseMediaPlayer(false);
        }

        if(mCurrentState == STATE_PLAYING || mCurrentState == STATE_PREPARING){
            if(station == null || mCurrentStation == station.intValue()){
                return;
            }

            if(mCurrentState == STATE_PLAYING){
                stopPlayer();
            }

            if(mCurrentState == STATE_PREPARING){
                mCurrentState = STATE_STOPPED;
                releaseMediaPlayer(false);
            }
        }

        if(!NetworkUtils.hasNetworkConnectivity(this)){
            Intent errorIntent = new Intent(BROADCAST_ERROR);
            Bundle args = new Bundle();
            args.putString(EXTRA_ERROR, "No network connectivity");
            errorIntent.putExtras(args);
            mLocalBroadcastMgr.sendBroadcast(errorIntent);
            killService();
            return;
        }

        mCurrentStation = station.intValue();

        //TODO Here you might actually want to acquire the stream data to stream
        playStream();
    }
    private void handleStopAction(){
        Log.d(LOG, "handleStopAction called");
        if(mCurrentState == STATE_STOPPED || mCurrentState == STATE_IDLE){
            return;
        }

        killService();
    }

    private void handleSyncAction(){
        RadioRedditSyncAdapter.syncImmediately(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        if(mCurrentState == STATE_ERROR
                || mCurrentState == STATE_IDLE
                || mCurrentState == STATE_STOPPED){
            return;
        }

        if(mCurrentState == STATE_PREPARING){
            killService();
        }

        if(mCurrentState == STATE_PLAYING){
            switch(focusChange){
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
    private void startSyncInterval(){
        mAlarmMgr.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + RESYNC_INTERVAL,
                RESYNC_INTERVAL,
                mPendingSyncIntent
        );
    }

    //TODO use these methods
    private void stopSyncInterval(){
        mAlarmMgr.cancel(mPendingSyncIntent);
    }
}
