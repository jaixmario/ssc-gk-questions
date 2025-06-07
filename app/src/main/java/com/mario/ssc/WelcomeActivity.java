package com.mario.ssc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WelcomeActivity extends AppCompatActivity {

    EditText nameInput;
    Button startButton;
    RadioGroup mediumGroup;
    RadioButton radioHindi, radioEnglish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Skip if already set
        if (!prefs.getBoolean("firstRun", true)) {
            String name = prefs.getString("username", "User");
            Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        nameInput = findViewById(R.id.nameInput);
        startButton = findViewById(R.id.startButton);
        mediumGroup = findViewById(R.id.mediumGroup);
        radioHindi = findViewById(R.id.radioHindi);
        radioEnglish = findViewById(R.id.radioEnglish);

        startButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (!name.isEmpty()) {
                String medium = radioHindi.isChecked() ? "Hindi" : "English";
                prefs.edit()
                        .putString("username", name)
                        .putBoolean("firstRun", false)
                        .putString("medium", medium)
                        .apply();

                Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                downloadDB(medium);
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadDB(String medium) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Setting up database...");
        dialog.setCancelable(false);
        dialog.show();

        String urlStr = medium.equals("Hindi")
                ? "https://raw.githubusercontent.com/jaixmario/database/main/SSC/Hindi/Hindi.db"
                : "https://raw.githubusercontent.com/jaixmario/database/main/SSC/ENGLISH/English.db";

        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                InputStream in = conn.getInputStream();
                File outFile = getDatabasePath("questions.db");
                outFile.getParentFile().mkdirs();
                FileOutputStream out = new FileOutputStream(outFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                out.close();
                in.close();

                runOnUiThread(() -> {
                    dialog.dismiss();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });

            } catch (Exception e) {
                dialog.dismiss();
                runOnUiThread(() ->
                        Toast.makeText(this, "❌ Failed to download database", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}