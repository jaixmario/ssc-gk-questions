package com.mario.ssc.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.mario.ssc.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsFragment extends Fragment {

    String dbName = "questions.db";
    SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        prefs = requireContext().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);

        MaterialButton updateDbBtn = view.findViewById(R.id.updateDbBtn);
        MaterialButton changeMediumBtn = view.findViewById(R.id.changeMediumBtn);

        updateDbBtn.setOnClickListener(v -> {
            String medium = prefs.getString("medium", "English");
            downloadDB(medium);
        });

        changeMediumBtn.setOnClickListener(v -> showMediumChangeDialog());

        return view;
    }

    private void showMediumChangeDialog() {
        String[] options = {"हिंदी", "English"};

        new AlertDialog.Builder(getContext())
            .setTitle("Select Medium")
            .setItems(options, (dialog, which) -> {
                String selectedMedium = (which == 0) ? "Hindi" : "English";
                prefs.edit().putString("medium", selectedMedium).apply();
                downloadDB(selectedMedium);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void downloadDB(String medium) {
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Downloading DB (" + medium + ")...");
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
                File outFile = requireContext().getDatabasePath(dbName);
                outFile.getParentFile().mkdirs();
                FileOutputStream out = new FileOutputStream(outFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                out.close();
                in.close();

                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "✅ Database updated to " + medium, Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                dialog.dismiss();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "❌ Failed to update DB", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}