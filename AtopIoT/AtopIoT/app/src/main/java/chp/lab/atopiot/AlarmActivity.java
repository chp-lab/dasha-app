package chp.lab.atopiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

public class AlarmActivity extends AppCompatActivity {

    private static final String TAG = "DB0x09";
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        final TextView alarm_textView = findViewById(R.id.alarm_textView);
        final String alarmMessage = getIntent().getExtras().getString("alarmMessage");
        final VideoView videoView = findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });
        videoView.start();

        Log.d(TAG, alarmMessage);

        alarm_textView.setText(alarmMessage);

        mediaPlayer = MediaPlayer.create(this, R.raw.alrmsound);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();


        final Button alarm_btn = findViewById(R.id.alarm_btn);

        alarm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "stop btn clicked");
                mediaPlayer.pause();
                startActivity(new Intent(AlarmActivity.this, HomeActivity.class));
                finish();
            }
        });
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if(mediaPlayer != null)
        {
            Log.d(TAG, "destroying media player");
            mediaPlayer.release();
        }
    }
}
