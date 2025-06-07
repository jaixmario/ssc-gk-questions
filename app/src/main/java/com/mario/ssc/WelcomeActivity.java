package com.mario.ssc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

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

        startButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (!name.isEmpty()) {
                prefs.edit()
                        .putString("username", name)
                        .putBoolean("firstRun", false)
                        .apply();

                Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                downloadDB(); // ⬅️ Download DB before going to MainActivity
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadDB() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Setting up database...");
        dialog.setCancelable(false);
        dialog.show();

        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/jaixmario/database/main/questions.db");
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