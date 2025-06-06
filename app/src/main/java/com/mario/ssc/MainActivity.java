package com.mario.ssc;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    String dbUrl = "https://raw.githubusercontent.com/jaixmario/database/main/questions.db";
    String dbName = "questions.db";
    SQLiteDatabase db;
    Cursor cursor;
    int currentIndex = 0;

    TextView welcomeText, questionText, resultText;
    RadioGroup optionsGroup;
    Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.welcomeText);
        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        nextBtn = findViewById(R.id.nextBtn);
        resultText = findViewById(R.id.resultText);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = prefs.getString("username", "User");
        welcomeText.setText("Welcome back, " + name + "...");

        File dbFile = getDatabasePath(dbName);
        if (!dbFile.exists()) {
            downloadDB();
        } else {
            loadQuestions();
        }

        nextBtn.setOnClickListener(v -> {
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selected = findViewById(selectedId);
                String selectedText = selected.getText().toString();
                String correct = cursor.getString(cursor.getColumnIndex("answer"));

                if (selectedText.equals(correct)) {
                    resultText.setText("✅ Correct");
                } else {
                    resultText.setText("❌ Wrong. Correct: " + correct);
                }

                nextBtn.setText("Next");
                nextBtn.setOnClickListener(next -> {
                    currentIndex++;
                    showQuestion();
                });
            }
        });
    }

    private void downloadDB() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Downloading DB...");
        dialog.show();

        new Thread(() -> {
            try {
                URL url = new URL(dbUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                InputStream in = conn.getInputStream();
                File outFile = getDatabasePath(dbName);
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
                    loadQuestions();
                });

            } catch (Exception e) {
                dialog.dismiss();
                e.printStackTrace();
            }
        }).start();
    }

    private void loadQuestions() {
        db = SQLiteDatabase.openDatabase(getDatabasePath(dbName).getPath(), null, SQLiteDatabase.OPEN_READONLY);
        cursor = db.rawQuery("SELECT * FROM questions", null);
        showQuestion();
    }

    private void showQuestion() {
        if (cursor.moveToPosition(currentIndex)) {
            questionText.setText(cursor.getString(cursor.getColumnIndex("question")));
            ((RadioButton) findViewById(R.id.optionA)).setText(cursor.getString(cursor.getColumnIndex("option_a")));
            ((RadioButton) findViewById(R.id.optionB)).setText(cursor.getString(cursor.getColumnIndex("option_b")));
            ((RadioButton) findViewById(R.id.optionC)).setText(cursor.getString(cursor.getColumnIndex("option_c")));
            ((RadioButton) findViewById(R.id.optionD)).setText(cursor.getString(cursor.getColumnIndex("option_d")));

            optionsGroup.clearCheck();
            resultText.setText("");
            nextBtn.setText("Submit");
        } else {
            questionText.setText("🎉 You’ve completed all questions!");
            optionsGroup.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
        }
    }
}