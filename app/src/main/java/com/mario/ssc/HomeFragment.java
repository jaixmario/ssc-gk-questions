package com.mario.ssc.fragments;

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

    TextView questionText, resultText;
    RadioGroup optionsGroup;
    RadioButton optionA, optionB, optionC, optionD;
    MaterialButton nextBtn;

    SQLiteDatabase db;
    Cursor cursor;
    int currentIndex = 0;
    boolean isAnswerSubmitted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        questionText = view.findViewById(R.id.questionText);
        resultText = view.findViewById(R.id.resultText);
        optionsGroup = view.findViewById(R.id.optionsGroup);
        optionA = view.findViewById(R.id.optionA);
        optionB = view.findViewById(R.id.optionB);
        optionC = view.findViewById(R.id.optionC);
        optionD = view.findViewById(R.id.optionD);
        nextBtn = view.findViewById(R.id.nextBtn);

        File dbFile = requireContext().getDatabasePath("questions.db");
        if (dbFile.exists()) {
            db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery("SELECT * FROM questions", null);
            showQuestion();
        }

        nextBtn.setOnClickListener(v -> handleAnswer());

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

            resultText.setText(selectedText.equals(correct)
                ? "✅ Correct"
                : "❌ Wrong. Correct: " + correct);

            isAnswerSubmitted = true;
            nextBtn.setText("Next");
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
            nextBtn.setText("Submit");
        } else {
            questionText.setText("🎉 You’ve completed all questions!");
            optionsGroup.setVisibility(View.GONE);
            resultText.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
        }
    }
}