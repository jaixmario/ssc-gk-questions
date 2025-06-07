package com.mario.ssc.fragments;

import android.app.ProgressDialog;
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

    String dbUrl = "https://raw.githubusercontent.com/jaixmario/database/main/questions.db";
    String dbName = "questions.db";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        MaterialButton updateButton = view.findViewById(R.id.updateDbBtn);
        updateButton.setOnClickListener(v -> downloadDB());

        return view;
    }

    private void downloadDB() {
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Downloading DB...");
        dialog.setCancelable(false);
        dialog.show();

        new Thread(() -> {
            try {
                URL url = new URL(dbUrl);
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
                    Toast.makeText(getContext(), "✅ DB Updated Successfully", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                dialog.dismiss();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "❌ Failed to update DB", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}