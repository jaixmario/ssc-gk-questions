package com.mario.ssc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    String dbUrl = "https://raw.githubusercontent.com/jaixmario/database/main/questions.db";
    String dbName = "questions.db";
    SQLiteDatabase db;
    Cursor cursor;
    int currentIndex = 0;

    TextView questionText;
    RadioGroup optionsGroup;
    Button nextBtn;
    TextView resultText;

    boolean isAnswerSubmitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_item);

        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        nextBtn = findViewById(R.id.nextBtn);
        resultText = findViewById(R.id.resultText);

        File dbFile = getDatabasePath(dbName);
        if (!dbFile.exists()) {
            downloadDB();
        } else {
            loadQuestions();
        }

        nextBtn.setOnClickListener(v -> handleButtonClick());
    }

    private void handleButtonClick() {
        if (!isAnswerSubmitted) {
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

                isAnswerSubmitted = true;
                nextBtn.setText("Next");
            } else {
                Toast.makeText(this, "Please select an option!", Toast.LENGTH_SHORT).show();
            }
        } else {
            currentIndex++;
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
                Log.e("DownloadError", e.getMessage());
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Failed to download DB", Toast.LENGTH_LONG).show();
                });
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
            isAnswerSubmitted = false;

        } else {
            questionText.setText("🎉 You’ve completed all questions!");
            optionsGroup.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
            resultText.setText("");
        }
    }
}