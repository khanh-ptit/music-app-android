package com.khanhleis11.musicappproject;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.chibde.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button btnplay, btnnext, btnprev, btnrepeat, btnrandom;
    TextView txtsongname, txtsstart, txtsstop;
    SeekBar seekmusic;
    BarVisualizer visualizer;
    ImageView imageView;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

// Khởi tạo ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Now Playing");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        btnprev = findViewById(R.id.btn_prev);
        btnnext = findViewById(R.id.btn_next);
        btnplay = findViewById(R.id.btn_play);
        btnrepeat = findViewById(R.id.btn_repeat);
        btnrandom = findViewById(R.id.btn_random);
        txtsongname = findViewById(R.id.txtsn);
        txtsstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);
        seekmusic = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.visualizer);
        imageView = findViewById(R.id.imageview);

//        visualizer.setColor(0xFF362E);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        position = bundle.getInt("pos", 0);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsongname.setText(sname);
        txtsongname.setSelected(true);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        setVisualizer();

        btnplay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnplay.setText("\uf04b");
            } else {
                mediaPlayer.start();
                btnplay.setText("\uf04c");
            }
        });

        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while (currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentPosition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekmusic.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        txtsstop.setText(createTime(mediaPlayer.getDuration()));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                txtsstart.setText(createTime(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(this, 1000);
            }
        }, 1000);

        mediaPlayer.setOnCompletionListener(mp -> btnnext.performClick());

        btnnext.setOnClickListener(v -> {
            changeSong((position + 1) % mySongs.size());
        });

        btnprev.setOnClickListener(v -> {
            int newPos = (position - 1 < 0) ? (mySongs.size() - 1) : (position - 1);
            changeSong(newPos);
        });
    }

    private void changeSong(int newPosition) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (visualizer != null) {
            visualizer.release();
        }

        position = newPosition;
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsongname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        btnplay.setText("\uf04c");

        setVisualizer();  // <-- đã sửa: luôn gọi lại sau khi tạo MediaPlayer mới

        seekmusic.setMax(mediaPlayer.getDuration());

        txtsstop.setText(createTime(mediaPlayer.getDuration()));

        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while (currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentPosition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        updateSeekBar.start();

        mediaPlayer.setOnCompletionListener(mp -> btnnext.performClick());

        startAnimation(imageView);
    }

    private void setVisualizer() {
        if (visualizer != null && mediaPlayer != null) {
            try {
                visualizer.release(); // Giải phóng nếu visualizer cũ còn giữ session
            } catch (Exception e) {
                e.printStackTrace();
            }

            int audioSessionId = mediaPlayer.getAudioSessionId();
            if (audioSessionId != -1) {
                visualizer.setPlayer(audioSessionId);

                int color = ContextCompat.getColor(this, R.color.red);
                visualizer.setColor(color);
            }
        }
    }

    public void startAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator.setDuration(10000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        return String.format("%d:%02d", min, sec);
    }
}
