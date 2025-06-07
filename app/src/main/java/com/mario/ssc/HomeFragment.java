package com.mario.ssc.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.mario.ssc.R;

import java.io.File;

public class HomeFragment extends Fragment {

    TextView questionText, resultText, explanationText, progressText, scoreText;
    RadioGroup optionsGroup;
    RadioButton optionA, optionB, optionC, optionD;
    MaterialButton nextBtn, resetBtn;

    SQLiteDatabase db;
    Cursor cursor;
    int currentIndex = 0;
    boolean isAnswerSubmitted = false;
    int correctCount = 0;
    int wrongCount = 0;
    int totalQuestions = 0;

    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        questionText = view.findViewById(R.id.questionText);
        resultText = view.findViewById(R.id.resultText);
        explanationText = view.findViewById(R.id.explanationText);
        progressText = view.findViewById(R.id.progressText);
        scoreText = view.findViewById(R.id.scoreText);
        optionsGroup = view.findViewById(R.id.optionsGroup);
        optionA = view.findViewById(R.id.optionA);
        optionB = view.findViewById(R.id.optionB);
        optionC = view.findViewById(R.id.optionC);
        optionD = view.findViewById(R.id.optionD);
        nextBtn = view.findViewById(R.id.nextBtn);
        resetBtn = view.findViewById(R.id.resetBtn);

        prefs = requireContext().getSharedPreferences("QuizPrefs", 0);
        currentIndex = prefs.getInt("currentIndex", 0);
        correctCount = prefs.getInt("correctCount", 0);
        wrongCount = prefs.getInt("wrongCount", 0);

        File dbFile = requireContext().getDatabasePath("questions.db");
        if (dbFile.exists()) {
            db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery("SELECT * FROM questions", null);
            totalQuestions = cursor.getCount();
            showQuestion();
        }

        nextBtn.setOnClickListener(v -> handleAnswer());

        resetBtn.setOnClickListener(v -> {
            currentIndex = 0;
            correctCount = 0;
            wrongCount = 0;
            prefs.edit().clear().apply();
            isAnswerSubmitted = false;
            showQuestion();
        });

        return view;
    }

    private void handleAnswer() {
        if (!isAnswerSubmitted) {
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getContext(), "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selected = requireView().findViewById(selectedId);
            String selectedText = selected.getText().toString();
            String correct = cursor.getString(cursor.getColumnIndex("answer"));
            String explanation = cursor.getString(cursor.getColumnIndex("explanation"));

            if (selectedText.equals(correct)) {
                resultText.setText("✅ Correct");
                correctCount++;
            } else {
                resultText.setText("❌ Wrong. Correct: " + correct);
                wrongCount++;
            }

            explanationText.setText("Explanation: " + explanation);
            scoreText.setText("Correct: " + correctCount + " | Wrong: " + wrongCount);
            isAnswerSubmitted = true;
            nextBtn.setText("Next");

            prefs.edit()
                .putInt("currentIndex", currentIndex)
                .putInt("correctCount", correctCount)
                .putInt("wrongCount", wrongCount)
                .apply();

        } else {
            currentIndex++;
            isAnswerSubmitted = false;
            showQuestion();
        }
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
            explanationText.setText("");
            nextBtn.setText("Submit");
            progressText.setText("Progress: " + (currentIndex + 1) + " / " + totalQuestions);
            scoreText.setText("Correct: " + correctCount + " | Wrong: " + wrongCount);
        } else {
            questionText.setText("🎉 You’ve completed all " + totalQuestions + " questions!");
            progressText.setText("Final Score - Correct: " + correctCount + ", Wrong: " + wrongCount);
            optionsGroup.setVisibility(View.GONE);
            resultText.setVisibility(View.GONE);
            explanationText.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
            resetBtn.setVisibility(View.VISIBLE);
        }
    }
}
