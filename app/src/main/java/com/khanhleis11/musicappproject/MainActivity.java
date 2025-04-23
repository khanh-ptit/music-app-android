package com.khanhleis11.musicappproject;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listViewSong);
        runtimePermission();
    }

    public void runtimePermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            displaySongs();
                        } else {
                            // Thông báo nếu quyền không được cấp
                            Toast.makeText(MainActivity.this, "Permissions not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();

        try {
            File[] files = file.listFiles();
            if (files != null) {
                for (File singlefile : files) {
                    if (singlefile.isDirectory() && !singlefile.isHidden()) {
                        arrayList.addAll(findSong(singlefile));
                    } else {
                        if (singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")) {
                            arrayList.add(singlefile);
                        }
                    }
                }
            } else {
                Log.e("FileAccess", "Không thể đọc file từ: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e("Error", "Lỗi khi đọc file: " + e.getMessage());
        }

        return arrayList;
    }

    void displaySongs() {
        try {
            File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            final ArrayList<File> mySongs = findSong(musicDir);
            items = new String[mySongs.size()];

            for (int i = 0; i < mySongs.size(); i++) {
                items[i] = mySongs.get(i).getName().replace(".mp3", "").replace(".wav", "");
            }

            Log.d("MyApp", "Tìm thấy " + mySongs.size() + " bài hát");

            customAdapter customAdapter = new customAdapter();
            listView.setAdapter(customAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String songName = (String) listView.getItemAtPosition(position);
                    startActivity(new Intent(getApplicationContext(), PlayerActivity.class).putExtra("songs", mySongs).putExtra("songname", songName).putExtra("pos", position));
                }
            });
        } catch (Exception e) {
            Log.e("displaySongs", "Lỗi khi hiển thị bài hát: " + e.getMessage());
        }
    }

    class customAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView txtSongName = myView.findViewById(R.id.txtsongname);
            txtSongName.setSelected(true);
            txtSongName.setText(items[position]);
            return myView;
        }
    }
}
