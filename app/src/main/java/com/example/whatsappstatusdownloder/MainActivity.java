package com.example.whatsappstatusdownloder;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String[] CANDIDATE_DIRS = new String[]{
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/WhatsApp/Media/.Statuses" // legacy
    };

    private static final int REQ_READ_MEDIA = 2001;

    RecyclerView recyclerView;
    StatusAdapter adapter;
    ArrayList<File> files = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.statusRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 1) Ask runtime permission, then load
        if (hasStoragePermission()) {
            loadStatuses();
        } else {
            requestStoragePermission();
        }
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) - Use MANAGE_EXTERNAL_STORAGE for accessing all files
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 10 and below
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Request MANAGE_EXTERNAL_STORAGE permission for Android 11+
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                startActivityForResult(intent, REQ_READ_MEDIA);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQ_READ_MEDIA);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                    },
                    REQ_READ_MEDIA);
        } else {
            // Android 10 and below
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQ_READ_MEDIA);
        }
    }
    private void loadStatuses() {
        files.clear();

        // Run this on a background thread to avoid blocking the UI
        new Thread(() -> {
            File foundDir = null;
            for (String path : CANDIDATE_DIRS) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    foundDir = dir;
                    break;
                }
            }

            if (foundDir != null) {
                File[] statusFiles = foundDir.listFiles();
                if (statusFiles != null) {
                    for (File file : statusFiles) {
                        String name = file.getName().toLowerCase();
                        if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                                name.endsWith(".png") || name.endsWith(".mp4")) {
                            files.add(file);
                        }
                    }
                }
            }

            // Update UI on main thread
            runOnUiThread(() -> {
                if (adapter == null) {
                    adapter = new StatusAdapter(files, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.update(files);
                }

                if (files.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No status files found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_READ_MEDIA) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    loadStatuses();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot access files.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);
        if (requestCode == REQ_READ_MEDIA) {
            if (hasStoragePermission()) {
                loadStatuses();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access files.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}