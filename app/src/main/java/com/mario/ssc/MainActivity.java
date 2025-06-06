package com.mario.ssc;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
    RadioButton optionA, optionB, optionC, optionD;
    MaterialButton nextBtn;

    boolean isAnswerSubmitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.welcomeText);
        questionText = findViewById(R.id.questionText);
        resultText = findViewById(R.id.resultText);
        optionsGroup = findViewById(R.id.optionsGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        nextBtn = findViewById(R.id.nextBtn);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = prefs.getString("username", "User");
        welcomeText.setText("Welcome back, " + name + "...");
        Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_LONG).show();

        File dbFile = getDatabasePath(dbName);
        if (!dbFile.exists()) {
            downloadDB();
        } else {
            loadQuestions();
        }

        nextBtn.setOnClickListener(v -> handleAnswer());
    }

    private void handleAnswer() {
        if (!isAnswerSubmitted) {
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selected = findViewById(selectedId);
            String selectedText = selected.getText().toString();
            String correct = cursor.getString(cursor.getColumnIndex("answer"));

            if (selectedText.equals(correct)) {
                resultText.setText("✅ Correct");
            } else {
                resultText.setText("❌ Wrong. Correct: " + correct);
            }

            isAnswerSubmitted = true;
            nextBtn.setText("Next");
        } else {
            currentIndex++;
            isAnswerSubmitted = false;
            showQuestion();
        }
    }

    private void downloadDB() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Downloading DB...");
        dialog.setCancelable(false);
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
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "DB download failed", Toast.LENGTH_LONG).show());
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
            optionA.setText(cursor.getString(cursor.getColumnIndex("option_a")));
            optionB.setText(cursor.getString(cursor.getColumnIndex("option_b")));
            optionC.setText(cursor.getString(cursor.getColumnIndex("option_c")));
            optionD.setText(cursor.getString(cursor.getColumnIndex("option_d")));

            optionsGroup.clearCheck();
            resultText.setText("");
            nextBtn.setText("Submit");
        } else {
            questionText.setText("🎉 You’ve completed all questions!");
            optionsGroup.setVisibility(View.GONE);
            resultText.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
        }
    }
}