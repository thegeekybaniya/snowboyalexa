package ai.kitt.snowboy.audio;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.List;

import ai.kitt.snowboy.Demo;
import ai.kitt.snowboy.MsgEnum;
import ai.kitt.snowboy.demo.R;


public class RecordingService extends Service {
    public static final String CHANNEL_ID = RecordingService.class.getSimpleName() + "Channel";
    private RecordingThread recordingThread;
    AudioDataReceivedListener listener;

    public static final String TAG = "RecordingService";

    public Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MsgEnum message = MsgEnum.getMsgEnum(msg.what);
            switch(message) {
                case MSG_ACTIVE:
                    Toast.makeText(RecordingService.this, "Detected", Toast.LENGTH_SHORT).show();
                    if(!isAppOnForeground(RecordingService.this)) {
                    Intent i = new Intent(RecordingService.this, Demo.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);

                }

                    break;
                case MSG_INFO:
                    break;
                case MSG_VAD_SPEECH:
                    break;
                case MSG_VAD_NOSPEECH:
                    break;
                case MSG_ERROR:
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private void sendEvent(String s, String msg_active) {
        Toast.makeText(getApplicationContext(), msg_active, Toast.LENGTH_SHORT).show();

    }


    public RecordingService() {
        this.listener = new AudioDataSaver();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        recordingThread = new RecordingThread(handle, listener);
        startRecording();
        showToast("Recording started ");
        Log.d(TAG, "onStartCommand: ");

        return START_STICKY;
    }



    public void startRecording() {
        recordingThread.startRecording();
        showToast("Recording Started");
        Log.d(TAG, "startRecording: ");
    }

    public void stopRecording() {
        recordingThread.stopRecording();
        showToast("Recording Stopped");
        Log.d(TAG, "stopRecording: ");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();

    }

    public void createNotification() {
        createNotificationChannel();

        // Intent notificationIntent = new Intent(this, MainActivity.class);
        // PendingIntent pendingIntent = PendingIntent.getActivity(this,
        //         0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Level is Listening")
                .setContentText("Level is Listening for Hotwords")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);
    }


    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Level Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    @Override
    public void onDestroy() {
        showToast("Recording Stopped");
        super.onDestroy();
//        stopRecording();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        showToast("onTask Removed");
        Log.d(TAG, "onTaskRemoved: Removed");

        sendBroadcast(new Intent("ai.kitt.snowboy.audio.RecordingService"));
        super.onTaskRemoved(rootIntent);
    }

    public void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    public class LocalBinder extends Binder {
        public RecordingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RecordingService.this;
        }
    }

     final IBinder mBinder = new LocalBinder();
    boolean bound = false;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        bound = true;

        return mBinder;
    }



    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

}