package com.example.homeworksem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
    };

    private EditText etBlacklistNumber, etWhitelistNumber;
    private ListView lvBlacklist, lvWhitelist;
    private ArrayAdapter<String> blacklistAdapter, whitelistAdapter;
    private List<String> blacklist, whitelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionsIfNeeded();

        etBlacklistNumber = findViewById(R.id.etBlacklistNumber);
        etWhitelistNumber = findViewById(R.id.etWhitelistNumber);
        lvBlacklist       = findViewById(R.id.lvBlacklist);
        lvWhitelist       = findViewById(R.id.lvWhitelist);

        // Load lists from files
        blacklist = readFromFile("blacklist.txt");
        whitelist = readFromFile("whitelist.txt");

        // Setup list display
        blacklistAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, blacklist) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                ((android.widget.TextView) view).setTextColor(android.graphics.Color.parseColor("#BDBDBD"));
                ((android.widget.TextView) view).setTextSize(15);
                return view;
            }
        };
        whitelistAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, whitelist) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                ((android.widget.TextView) view).setTextColor(android.graphics.Color.parseColor("#BDBDBD"));
                ((android.widget.TextView) view).setTextSize(15);
                return view;
            }
        };
        lvBlacklist.setAdapter(blacklistAdapter);
        lvWhitelist.setAdapter(whitelistAdapter);

        // Add to blacklist
        findViewById(R.id.btnAddBlacklist).setOnClickListener(v -> {
            String number = etBlacklistNumber.getText().toString().trim();
            if (number.isEmpty()) {
                Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
                return;
            }
            if (blacklist.contains(number)) {
                Toast.makeText(this, "Already in blacklist", Toast.LENGTH_SHORT).show();
                return;
            }
            blacklist.add(number);
            saveToFile("blacklist.txt", blacklist);
            blacklistAdapter.notifyDataSetChanged();
            etBlacklistNumber.setText("");
            Toast.makeText(this, "Added to blacklist", Toast.LENGTH_SHORT).show();
        });

        // Add to whitelist
        findViewById(R.id.btnAddWhitelist).setOnClickListener(v -> {
            String number = etWhitelistNumber.getText().toString().trim();
            if (number.isEmpty()) {
                Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
                return;
            }
            if (whitelist.contains(number)) {
                Toast.makeText(this, "Already in whitelist", Toast.LENGTH_SHORT).show();
                return;
            }
            whitelist.add(number);
            saveToFile("whitelist.txt", whitelist);
            whitelistAdapter.notifyDataSetChanged();
            etWhitelistNumber.setText("");
            Toast.makeText(this, "Added to whitelist", Toast.LENGTH_SHORT).show();
        });

        // Long press to delete from blacklist
        lvBlacklist.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove from Blacklist")
                    .setMessage("Remove " + blacklist.get(position) + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        blacklist.remove(position);
                        saveToFile("blacklist.txt", blacklist);
                        blacklistAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        // Long press to delete from whitelist
        lvWhitelist.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove from Whitelist")
                    .setMessage("Remove " + whitelist.get(position) + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        whitelist.remove(position);
                        saveToFile("whitelist.txt", whitelist);
                        whitelistAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        // Stop service button (Person 1's feature - keep it)
        Button btnStop = findViewById(R.id.btnStopService);
        btnStop.setOnClickListener(v -> stopService(new Intent(this, MyService.class)));
    }

    private List<String> readFromFile(String filename) {
        List<String> list = new ArrayList<>();
        try {
            File file = new File(getFilesDir(), filename);
            if (!file.exists()) return list;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) list.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void saveToFile(String filename, List<String> list) {
        try {
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            for (String number : list) {
                fos.write((number + "\n").getBytes());
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestPermissionsIfNeeded() {
        boolean allGranted = true;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            boolean shouldShowRationale = false;
            
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        shouldShowRationale = true;
                    }
                }
            }
            
            if (!allGranted) {
                if (shouldShowRationale) {
                    new AlertDialog.Builder(this)
                        .setTitle("Permissions Required")
                        .setMessage("These permissions are required for the app to function properly. Please grant them.")
                        .setPositiveButton("OK", (dialog, which) -> requestPermissionsIfNeeded())
                        .setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show())
                        .show();
                } else {
                    new AlertDialog.Builder(this)
                        .setTitle("Permissions Required")
                        .setMessage("Permissions have been permanently denied. Please enable them in app settings to use all features.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            android.net.Uri uri = android.net.Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(this, "Permissions permanently denied.", Toast.LENGTH_SHORT).show())
                        .show();
                }
            }
        }
    }
}